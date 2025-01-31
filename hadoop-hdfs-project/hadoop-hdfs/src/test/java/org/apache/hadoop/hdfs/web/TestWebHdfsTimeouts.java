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

package org.apache.hadoop.hdfs.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.authentication.client.ConnectionConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test suite checks that WebHdfsFileSystem sets connection timeouts and
 * read timeouts on its sockets, thus preventing threads from hanging
 * indefinitely on an undefined/infinite timeout.  The tests work by starting a
 * bogus server on the namenode HTTP port, which is rigged to not accept new
 * connections or to accept connections but not send responses.
 */
public class TestWebHdfsTimeouts {

    private static final Log LOG = LogFactory.getLog(TestWebHdfsTimeouts.class);

    private static final int CLIENTS_TO_CONSUME_BACKLOG = 100;
    private static final int CONNECTION_BACKLOG = 1;
    private static final int SHORT_SOCKET_TIMEOUT = 5;
    private static final int TEST_TIMEOUT = 10000;

    private List<SocketChannel> clients;
    private WebHdfsFileSystem fs;
    private InetSocketAddress nnHttpAddress;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private final URLConnectionFactory connectionFactory = new URLConnectionFactory(new ConnectionConfigurator() {
        @Override
        public HttpURLConnection configure(HttpURLConnection conn) throws IOException {
            conn.setReadTimeout(SHORT_SOCKET_TIMEOUT);
            conn.setConnectTimeout(SHORT_SOCKET_TIMEOUT);
            return conn;
        }
    });

    @Before
    public void setUp() throws Exception {
        Configuration conf = WebHdfsTestUtil.createConf();
        serverSocket = new ServerSocket(0, CONNECTION_BACKLOG);
        nnHttpAddress = new InetSocketAddress("localhost", serverSocket.getLocalPort());
        conf.set(DFSConfigKeys.DFS_NAMENODE_HTTP_ADDRESS_KEY, "localhost:" + serverSocket.getLocalPort());
        fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsFileSystem.SCHEME);
        fs.connectionFactory = connectionFactory;
        clients = new ArrayList<SocketChannel>();
        serverThread = null;
    }

    @After
    public void tearDown() throws Exception {
        IOUtils.cleanup(LOG, clients.toArray(new SocketChannel[clients.size()]));
        IOUtils.cleanup(LOG, fs);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.debug("Exception in closing " + serverSocket, e);
            }
        }
        if (serverThread != null) {
            serverThread.join();
        }
    }

    /**
     * Expect connect timeout, because the connection backlog is consumed.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testConnectTimeout() throws Exception {
        consumeConnectionBacklog();
        try {
            fs.listFiles(new Path("/"), false);
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("connect timed out", e.getMessage());
        }
    }

    /**
     * Expect read timeout, because the bogus server never sends a reply.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testReadTimeout() throws Exception {
        try {
            fs.listFiles(new Path("/"), false);
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("Read timed out", e.getMessage());
        }
    }

    /**
     * Expect connect timeout on a URL that requires auth, because the connection
     * backlog is consumed.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testAuthUrlConnectTimeout() throws Exception {
        consumeConnectionBacklog();
        try {
            fs.getDelegationToken("renewer");
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("connect timed out", e.getMessage());
        }
    }

    /**
     * Expect read timeout on a URL that requires auth, because the bogus server
     * never sends a reply.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testAuthUrlReadTimeout() throws Exception {
        try {
            fs.getDelegationToken("renewer");
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("Read timed out", e.getMessage());
        }
    }

    /**
     * After a redirect, expect connect timeout accessing the redirect location,
     * because the connection backlog is consumed.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testRedirectConnectTimeout() throws Exception {
        startSingleTemporaryRedirectResponseThread(true);
        try {
            fs.getFileChecksum(new Path("/file"));
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("connect timed out", e.getMessage());
        }
    }

    /**
     * After a redirect, expect read timeout accessing the redirect location,
     * because the bogus server never sends a reply.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testRedirectReadTimeout() throws Exception {
        startSingleTemporaryRedirectResponseThread(false);
        try {
            fs.getFileChecksum(new Path("/file"));
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("Read timed out", e.getMessage());
        }
    }

    /**
     * On the second step of two-step write, expect connect timeout accessing the
     * redirect location, because the connection backlog is consumed.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testTwoStepWriteConnectTimeout() throws Exception {
        startSingleTemporaryRedirectResponseThread(true);
        OutputStream os = null;
        try {
            os = fs.create(new Path("/file"));
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("connect timed out", e.getMessage());
        } finally {
            IOUtils.cleanup(LOG, os);
        }
    }

    /**
     * On the second step of two-step write, expect read timeout accessing the
     * redirect location, because the bogus server never sends a reply.
     */
    @Test(timeout = TEST_TIMEOUT)
    public void testTwoStepWriteReadTimeout() throws Exception {
        startSingleTemporaryRedirectResponseThread(false);
        OutputStream os = null;
        try {
            os = fs.create(new Path("/file"));
            os.close(); // must close stream to force reading the HTTP response
            os = null;
            fail("expected timeout");
        } catch (SocketTimeoutException e) {
            assertEquals("Read timed out", e.getMessage());
        } finally {
            IOUtils.cleanup(LOG, os);
        }
    }

    /**
     * Starts a background thread that accepts one and only one client connection
     * on the server socket, sends an HTTP 307 Temporary Redirect response, and
     * then exits.  This is useful for testing timeouts on the second step of
     * methods that issue 2 HTTP requests (request 1, redirect, request 2).
     *
     * For handling the first request, this method sets socket timeout to use the
     * initial values defined in URLUtils.  Afterwards, it guarantees that the
     * second request will use a very short timeout.
     *
     * Optionally, the thread may consume the connection backlog immediately after
     * receiving its one and only client connection.  This is useful for forcing a
     * connection timeout on the second request.
     *
     * On tearDown, open client connections are closed, and the thread is joined.
     *
     * @param consumeConnectionBacklog boolean whether or not to consume connection
     *   backlog and thus force a connection timeout on the second request
     */
    private void startSingleTemporaryRedirectResponseThread(
            final boolean consumeConnectionBacklog) {
        fs.connectionFactory = URLConnectionFactory.DEFAULT_SYSTEM_CONNECTION_FACTORY;
        serverThread = new Thread() {
            @Override
            public void run() {
                Socket clientSocket = null;
                OutputStream out = null;
                InputStream in = null;
                InputStreamReader isr = null;
                BufferedReader br = null;
                try {
                    // Accept one and only one client connection.
                    clientSocket = serverSocket.accept();

                    // Immediately setup conditions for subsequent connections.
                    fs.connectionFactory = connectionFactory;
                    if (consumeConnectionBacklog) {
                        consumeConnectionBacklog();
                    }

                    // Consume client's HTTP request by reading until EOF or empty line.
                    in = clientSocket.getInputStream();
                    isr = new InputStreamReader(in);
                    br = new BufferedReader(isr);
                    for (; ; ) {
                        String line = br.readLine();
                        if (line == null || line.isEmpty()) {
                            break;
                        }
                    }

                    // Write response.
                    out = clientSocket.getOutputStream();
                    out.write(temporaryRedirect().getBytes("UTF-8"));
                } catch (IOException e) {
                    // Fail the test on any I/O error in the server thread.
                    LOG.error("unexpected IOException in server thread", e);
                    fail("unexpected IOException in server thread: " + e);
                } finally {
                    // Clean it all up.
                    IOUtils.cleanup(LOG, br, isr, in, out);
                    IOUtils.closeSocket(clientSocket);
                }
            }
        };
        serverThread.start();
    }

    /**
     * Consumes the test server's connection backlog by spamming non-blocking
     * SocketChannel client connections.  We never do anything with these sockets
     * beyond just initiaing the connections.  The method saves a reference to each
     * new SocketChannel so that it can be closed during tearDown.  We define a
     * very small connection backlog, but the OS may silently enforce a larger
     * minimum backlog than requested.  To work around this, we create far more
     * client connections than our defined backlog.
     *
     * @throws IOException thrown for any I/O error
     */
    private void consumeConnectionBacklog() throws IOException {
        for (int i = 0; i < CLIENTS_TO_CONSUME_BACKLOG; ++i) {
            SocketChannel client = SocketChannel.open();
            client.configureBlocking(false);
            client.connect(nnHttpAddress);
            clients.add(client);
        }
    }

    /**
     * Creates an HTTP 307 response with the redirect location set back to the
     * test server's address.  HTTP is supposed to terminate newlines with CRLF, so
     * we hard-code that instead of using the line separator property.
     *
     * @return String HTTP 307 response
     */
    private String temporaryRedirect() {
        return "HTTP/1.1 307 Temporary Redirect\r\n" +
                "Location: http://" + NetUtils.getHostPortString(nnHttpAddress) + "\r\n" +
                "\r\n";
    }
}
