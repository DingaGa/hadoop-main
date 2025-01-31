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

package org.apache.hadoop.hdfs.server.namenode;

import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_NAMENODE_AUDIT_LOGGERS_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.web.resources.GetOpParam;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.authorize.ProxyServers;
import org.apache.hadoop.security.authorize.ProxyUsers;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link AuditLogger} custom audit logging interface.
 */
public class TestAuditLogger {

    private static final short TEST_PERMISSION = (short) 0654;

    @Before
    public void setup() {
        DummyAuditLogger.initialized = false;
        DummyAuditLogger.logCount = 0;
        DummyAuditLogger.remoteAddr = null;

        Configuration conf = new HdfsConfiguration();
        ProxyUsers.refreshSuperUserGroupsConfiguration(conf);
    }

    /**
     * Tests that AuditLogger works as expected.
     */
    @Test
    public void testAuditLogger() throws IOException {
        Configuration conf = new HdfsConfiguration();
        conf.set(DFS_NAMENODE_AUDIT_LOGGERS_KEY,
                DummyAuditLogger.class.getName());
        MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).build();

        try {
            cluster.waitClusterUp();
            assertTrue(DummyAuditLogger.initialized);
            DummyAuditLogger.resetLogCount();

            FileSystem fs = cluster.getFileSystem();
            long time = System.currentTimeMillis();
            fs.setTimes(new Path("/"), time, time);
            assertEquals(1, DummyAuditLogger.logCount);
        } finally {
            cluster.shutdown();
        }
    }

    @Test
    public void testWebHdfsAuditLogger() throws IOException, URISyntaxException {
        Configuration conf = new HdfsConfiguration();
        conf.set(DFS_NAMENODE_AUDIT_LOGGERS_KEY,
                DummyAuditLogger.class.getName());
        MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).build();

        GetOpParam.Op op = GetOpParam.Op.GETFILESTATUS;
        try {
            cluster.waitClusterUp();
            assertTrue(DummyAuditLogger.initialized);
            URI uri = new URI(
                    "http",
                    NetUtils.getHostPortString(cluster.getNameNode().getHttpAddress()),
                    "/webhdfs/v1/", op.toQueryString(), null);

            // non-proxy request
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod(op.getType().toString());
            conn.connect();
            assertEquals(200, conn.getResponseCode());
            conn.disconnect();
            assertEquals(1, DummyAuditLogger.logCount);
            assertEquals("127.0.0.1", DummyAuditLogger.remoteAddr);

            // non-trusted proxied request
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod(op.getType().toString());
            conn.setRequestProperty("X-Forwarded-For", "1.1.1.1");
            conn.connect();
            assertEquals(200, conn.getResponseCode());
            conn.disconnect();
            assertEquals(2, DummyAuditLogger.logCount);
            assertEquals("127.0.0.1", DummyAuditLogger.remoteAddr);

            // trusted proxied request
            conf.set(ProxyServers.CONF_HADOOP_PROXYSERVERS, "127.0.0.1");
            ProxyUsers.refreshSuperUserGroupsConfiguration(conf);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod(op.getType().toString());
            conn.setRequestProperty("X-Forwarded-For", "1.1.1.1");
            conn.connect();
            assertEquals(200, conn.getResponseCode());
            conn.disconnect();
            assertEquals(3, DummyAuditLogger.logCount);
            assertEquals("1.1.1.1", DummyAuditLogger.remoteAddr);
        } finally {
            cluster.shutdown();
        }
    }

    /**
     * Minor test related to HADOOP-9155. Verify that during a
     * FileSystem.setPermission() operation, the stat passed in during the
     * logAuditEvent() call returns the new permission rather than the old
     * permission.
     */
    @Test
    public void testAuditLoggerWithSetPermission() throws IOException {
        Configuration conf = new HdfsConfiguration();
        conf.set(DFS_NAMENODE_AUDIT_LOGGERS_KEY,
                DummyAuditLogger.class.getName());
        MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).build();

        try {
            cluster.waitClusterUp();
            assertTrue(DummyAuditLogger.initialized);
            DummyAuditLogger.resetLogCount();

            FileSystem fs = cluster.getFileSystem();
            long time = System.currentTimeMillis();
            final Path p = new Path("/");
            fs.setTimes(p, time, time);
            fs.setPermission(p, new FsPermission(TEST_PERMISSION));
            assertEquals(TEST_PERMISSION, DummyAuditLogger.foundPermission);
            assertEquals(2, DummyAuditLogger.logCount);
        } finally {
            cluster.shutdown();
        }
    }

    /**
     * Tests that a broken audit logger causes requests to fail.
     */
    @Test
    public void testBrokenLogger() throws IOException {
        Configuration conf = new HdfsConfiguration();
        conf.set(DFS_NAMENODE_AUDIT_LOGGERS_KEY,
                BrokenAuditLogger.class.getName());
        MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).build();

        try {
            cluster.waitClusterUp();

            FileSystem fs = cluster.getFileSystem();
            long time = System.currentTimeMillis();
            fs.setTimes(new Path("/"), time, time);
            fail("Expected exception due to broken audit logger.");
        } catch (RemoteException re) {
            // Expected.
        } finally {
            cluster.shutdown();
        }
    }

    public static class DummyAuditLogger implements AuditLogger {

        static boolean initialized;
        static int logCount;
        static short foundPermission;
        static String remoteAddr;

        public void initialize(Configuration conf) {
            initialized = true;
        }

        public static void resetLogCount() {
            logCount = 0;
        }

        public void logAuditEvent(boolean succeeded, String userName,
                                  InetAddress addr, String cmd, String src, String dst,
                                  FileStatus stat) {
            remoteAddr = addr.getHostAddress();
            logCount++;
            if (stat != null) {
                foundPermission = stat.getPermission().toShort();
            }
        }

    }

    public static class BrokenAuditLogger implements AuditLogger {

        public void initialize(Configuration conf) {
            // No op.
        }

        public void logAuditEvent(boolean succeeded, String userName,
                                  InetAddress addr, String cmd, String src, String dst,
                                  FileStatus stat) {
            throw new RuntimeException("uh oh");
        }

    }

}
