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

package org.apache.hadoop.service;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;

/**
 * Interface to notify state changes of a service.
 */
@Public
@Stable
public interface ServiceStateChangeListener {

    /**
     * Callback to notify of a state change. The service will already
     * have changed state before this callback is invoked.
     * <p/>
     * This operation is invoked on the thread that initiated the state change,
     * while the service itself in in a sychronized section.
     * <ol>
     * <li>Any long-lived operation here will prevent the service state
     * change from completing in a timely manner.</li>
     * <li>If another thread is somehow invoked from the listener, and
     * that thread invokes the methods of the service (including
     * subclass-specific methods), there is a risk of a deadlock.</li>
     * </ol>
     *
     * @param service the service that has changed.
     */
    void stateChanged(Service service);

}
