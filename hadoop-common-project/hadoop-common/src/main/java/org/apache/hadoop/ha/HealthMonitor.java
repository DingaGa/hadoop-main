/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.ha;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import static org.apache.hadoop.fs.CommonConfigurationKeys.*;

import org.apache.hadoop.ha.HAServiceProtocol;
import org.apache.hadoop.ha.HAServiceProtocol.HAServiceState;
import org.apache.hadoop.ha.HealthCheckFailedException;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.util.Daemon;

import com.google.common.base.Preconditions;

/**
 * This class is a daemon which runs in a loop, periodically heartbeating
 * with an HA service. It is responsible for keeping track of that service's
 * health and exposing callbacks to the failover controller when the health
 * status changes.
 * <p/>
 * Classes which need callbacks should implement the {@link Callback}
 * interface.
 */
@InterfaceAudience.Private
public class HealthMonitor {
    private static final Log LOG = LogFactory.getLog(
            HealthMonitor.class);

    private Daemon daemon;
    private long connectRetryInterval;
    private long checkIntervalMillis;
    private long sleepAfterDisconnectMillis;

    private int rpcTimeout;

    private volatile boolean shouldRun = true;

    /**
     * The connected proxy
     */
    private HAServiceProtocol proxy;

    /**
     * The HA service to monitor
     */
    private final HAServiceTarget targetToMonitor;

    private final Configuration conf;

    private State state = State.INITIALIZING;

    /**
     * Listeners for state changes
     */
    private List<Callback> callbacks = Collections.synchronizedList(
            new LinkedList<Callback>());

    private List<ServiceStateCallback> serviceStateCallbacks = Collections
            .synchronizedList(new LinkedList<ServiceStateCallback>());

    private HAServiceStatus lastServiceState = new HAServiceStatus(
            HAServiceState.INITIALIZING);

    @InterfaceAudience.Private
    public enum State {
        /**
         * The health monitor is still starting up.
         */
        INITIALIZING,

        /**
         * The service is not responding to health check RPCs.
         */
        SERVICE_NOT_RESPONDING,

        /**
         * The service is connected and healthy.
         */
        SERVICE_HEALTHY,

        /**
         * The service is running but unhealthy.
         */
        SERVICE_UNHEALTHY,

        /**
         * The health monitor itself failed unrecoverably and can
         * no longer provide accurate information.
         */
        HEALTH_MONITOR_FAILED;
    }


    HealthMonitor(Configuration conf, HAServiceTarget target) {
        this.targetToMonitor = target;
        this.conf = conf;

        this.sleepAfterDisconnectMillis = conf.getLong(
                HA_HM_SLEEP_AFTER_DISCONNECT_KEY,
                HA_HM_SLEEP_AFTER_DISCONNECT_DEFAULT);
        this.checkIntervalMillis = conf.getLong(
                HA_HM_CHECK_INTERVAL_KEY,
                HA_HM_CHECK_INTERVAL_DEFAULT);
        this.connectRetryInterval = conf.getLong(
                HA_HM_CONNECT_RETRY_INTERVAL_KEY,
                HA_HM_CONNECT_RETRY_INTERVAL_DEFAULT);
        this.rpcTimeout = conf.getInt(
                HA_HM_RPC_TIMEOUT_KEY,
                HA_HM_RPC_TIMEOUT_DEFAULT);

        this.daemon = new MonitorDaemon();
    }

    public void addCallback(Callback cb) {
        this.callbacks.add(cb);
    }

    public void removeCallback(Callback cb) {
        callbacks.remove(cb);
    }

    public synchronized void addServiceStateCallback(ServiceStateCallback cb) {
        this.serviceStateCallbacks.add(cb);
    }

    public synchronized void removeServiceStateCallback(ServiceStateCallback cb) {
        serviceStateCallbacks.remove(cb);
    }

    public void shutdown() {
        LOG.info("Stopping HealthMonitor thread");
        shouldRun = false;
        daemon.interrupt();
    }

    /**
     * @return the current proxy object to the underlying service.
     * Note that this may return null in the case that the service
     * is not responding. Also note that, even if the last indicated
     * state is healthy, the service may have gone down in the meantime.
     */
    public synchronized HAServiceProtocol getProxy() {
        return proxy;
    }

    private void loopUntilConnected() throws InterruptedException {
        tryConnect();
        while (proxy == null) {
            Thread.sleep(connectRetryInterval);
            tryConnect();
        }
        assert proxy != null;
    }

    private void tryConnect() {
        Preconditions.checkState(proxy == null);

        try {
            synchronized (this) {
                proxy = createProxy();
            }
        } catch (IOException e) {
            LOG.warn("Could not connect to local service at " + targetToMonitor +
                    ": " + e.getMessage());
            proxy = null;
            enterState(State.SERVICE_NOT_RESPONDING);
        }
    }

    /**
     * Connect to the service to be monitored. Stubbed out for easier testing.
     */
    protected HAServiceProtocol createProxy() throws IOException {
        return targetToMonitor.getProxy(conf, rpcTimeout);
    }

    private void doHealthChecks() throws InterruptedException {
        while (shouldRun) {
            HAServiceStatus status = null;
            boolean healthy = false;
            try {
                status = proxy.getServiceStatus();
                proxy.monitorHealth();
                healthy = true;
            } catch (HealthCheckFailedException e) {
                LOG.warn("Service health check failed for " + targetToMonitor
                        + ": " + e.getMessage());
                enterState(State.SERVICE_UNHEALTHY);
            } catch (Throwable t) {
                LOG.warn("Transport-level exception trying to monitor health of " +
                        targetToMonitor + ": " + t.getLocalizedMessage());
                RPC.stopProxy(proxy);
                proxy = null;
                enterState(State.SERVICE_NOT_RESPONDING);
                Thread.sleep(sleepAfterDisconnectMillis);
                return;
            }

            if (status != null) {
                setLastServiceStatus(status);
            }
            if (healthy) {
                enterState(State.SERVICE_HEALTHY);
            }

            Thread.sleep(checkIntervalMillis);
        }
    }

    private synchronized void setLastServiceStatus(HAServiceStatus status) {
        this.lastServiceState = status;
        for (ServiceStateCallback cb : serviceStateCallbacks) {
            cb.reportServiceStatus(lastServiceState);
        }
    }

    private synchronized void enterState(State newState) {
        if (newState != state) {
            LOG.info("Entering state " + newState);
            state = newState;
            synchronized (callbacks) {
                for (Callback cb : callbacks) {
                    cb.enteredState(newState);
                }
            }
        }
    }

    synchronized State getHealthState() {
        return state;
    }

    synchronized HAServiceStatus getLastServiceStatus() {
        return lastServiceState;
    }

    boolean isAlive() {
        return daemon.isAlive();
    }

    void join() throws InterruptedException {
        daemon.join();
    }

    void start() {
        daemon.start();
    }

    private class MonitorDaemon extends Daemon {
        private MonitorDaemon() {
            super();
            setName("Health Monitor for " + targetToMonitor);
            setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.fatal("Health monitor failed", e);
                    enterState(HealthMonitor.State.HEALTH_MONITOR_FAILED);
                }
            });
        }

        @Override
        public void run() {
            while (shouldRun) {
                try {
                    loopUntilConnected();
                    doHealthChecks();
                } catch (InterruptedException ie) {
                    Preconditions.checkState(!shouldRun,
                            "Interrupted but still supposed to run");
                }
            }
        }
    }

    /**
     * Callback interface for state change events.
     * <p/>
     * This interface is called from a single thread which also performs
     * the health monitoring. If the callback processing takes a long time,
     * no further health checks will be made during this period, nor will
     * other registered callbacks be called.
     * <p/>
     * If the callback itself throws an unchecked exception, no other
     * callbacks following it will be called, and the health monitor
     * will terminate, entering HEALTH_MONITOR_FAILED state.
     */
    static interface Callback {
        void enteredState(State newState);
    }

    /**
     * Callback interface for service states.
     */
    static interface ServiceStateCallback {
        void reportServiceStatus(HAServiceStatus status);
    }
}
