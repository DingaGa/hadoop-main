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
package org.apache.hadoop.mapred.gridmix;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.tools.rumen.JobStory;
import org.apache.hadoop.tools.rumen.JobStoryProducer;
import org.apache.hadoop.util.ExitUtil;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

public class TestGridmixSubmission extends CommonJobTest {
    private static File inSpace = new File("src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "data");


    static {
        ((Log4JLogger) LogFactory.getLog("org.apache.hadoop.mapred.gridmix"))
                .getLogger().setLevel(Level.DEBUG);
    }


    @BeforeClass
    public static void init() throws IOException {
        GridmixTestUtils.initCluster(TestGridmixSubmission.class);

        System.setProperty("src.test.data", inSpace.getAbsolutePath());
    }

    @AfterClass
    public static void shutDown() throws IOException {
        GridmixTestUtils.shutdownCluster();
    }

    /**
     * Verifies that the given {@code JobStory} corresponds to the checked-in
     * WordCount {@code JobStory}. The verification is effected via JUnit
     * assertions.
     *
     * @param js the candidate JobStory.
     */
    private void verifyWordCountJobStory(JobStory js) {
        assertNotNull("Null JobStory", js);
        String expectedJobStory = "WordCount:johndoe:default:1285322645148:3:1";
        String actualJobStory = js.getName() + ":" + js.getUser() + ":"
                + js.getQueueName() + ":" + js.getSubmissionTime() + ":"
                + js.getNumberMaps() + ":" + js.getNumberReduces();
        assertEquals("Unexpected JobStory", expectedJobStory, actualJobStory);
    }

    /**
     * Expands a file compressed using {@code gzip}.
     *
     * @param fs  the {@code FileSystem} corresponding to the given file.
     * @param in  the path to the compressed file.
     * @param out the path to the uncompressed output.
     * @throws Exception if there was an error during the operation.
     */
    private void expandGzippedTrace(FileSystem fs, Path in, Path out)
            throws Exception {
        byte[] buff = new byte[4096];
        GZIPInputStream gis = new GZIPInputStream(fs.open(in));
        FSDataOutputStream fsdOs = fs.create(out);
        int numRead;
        while ((numRead = gis.read(buff, 0, buff.length)) != -1) {
            fsdOs.write(buff, 0, numRead);
        }
        gis.close();
        fsdOs.close();
    }

    /**
     * Tests the reading of traces in GridMix3. These traces are generated by
     * Rumen and are in the JSON format. The traces can optionally be compressed
     * and uncompressed traces can also be passed to GridMix3 via its standard
     * input stream. The testing is effected via JUnit assertions.
     *
     * @throws Exception if there was an error.
     */
    @Test(timeout = 20000)
    public void testTraceReader() throws Exception {
        Configuration conf = new Configuration();
        FileSystem lfs = FileSystem.getLocal(conf);
        Path rootInputDir = new Path(System.getProperty("src.test.data"));
        rootInputDir = rootInputDir.makeQualified(lfs.getUri(),
                lfs.getWorkingDirectory());
        Path rootTempDir = new Path(System.getProperty("test.build.data",
                System.getProperty("java.io.tmpdir")), "testTraceReader");
        rootTempDir = rootTempDir.makeQualified(lfs.getUri(),
                lfs.getWorkingDirectory());
        Path inputFile = new Path(rootInputDir, "wordcount.json.gz");
        Path tempFile = new Path(rootTempDir, "gridmix3-wc.json");

        InputStream origStdIn = System.in;
        InputStream tmpIs = null;
        try {
            DebugGridmix dgm = new DebugGridmix();
            JobStoryProducer jsp = dgm.createJobStoryProducer(inputFile.toString(),
                    conf);

            LOG.info("Verifying JobStory from compressed trace...");
            verifyWordCountJobStory(jsp.getNextJob());

            expandGzippedTrace(lfs, inputFile, tempFile);
            jsp = dgm.createJobStoryProducer(tempFile.toString(), conf);
            LOG.info("Verifying JobStory from uncompressed trace...");
            verifyWordCountJobStory(jsp.getNextJob());

            tmpIs = lfs.open(tempFile);
            System.setIn(tmpIs);
            LOG.info("Verifying JobStory from trace in standard input...");
            jsp = dgm.createJobStoryProducer("-", conf);
            verifyWordCountJobStory(jsp.getNextJob());
        } finally {
            System.setIn(origStdIn);
            if (tmpIs != null) {
                tmpIs.close();
            }
            lfs.delete(rootTempDir, true);
        }
    }

    @Test(timeout = 500000)
    public void testReplaySubmit() throws Exception {
        policy = GridmixJobSubmissionPolicy.REPLAY;
        LOG.info(" Replay started at " + System.currentTimeMillis());
        doSubmission(null, false);
        LOG.info(" Replay ended at " + System.currentTimeMillis());

    }

    @Test(timeout = 500000)
    public void testStressSubmit() throws Exception {
        policy = GridmixJobSubmissionPolicy.STRESS;
        LOG.info(" Stress started at " + System.currentTimeMillis());
        doSubmission(null, false);
        LOG.info(" Stress ended at " + System.currentTimeMillis());
    }

    // test empty request should be hint message
    @Test(timeout = 100000)
    public void testMain() throws Exception {

        SecurityManager securityManager = System.getSecurityManager();

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(bytes);
        final PrintStream oldOut = System.out;
        System.setErr(out);
        ExitUtil.disableSystemExit();
        try {
            String[] argv = new String[0];
            DebugGridmix.main(argv);

        } catch (ExitUtil.ExitException e) {
            assertEquals("ExitException", e.getMessage());
            ExitUtil.resetFirstExitException();
        } finally {
            System.setErr(oldOut);
            System.setSecurityManager(securityManager);
        }
        String print = bytes.toString();
        // should be printed tip in std error stream
        assertTrue(print
                .contains("Usage: gridmix [-generate <MiB>] [-users URI] [-Dname=value ...] <iopath> <trace>"));
        assertTrue(print.contains("e.g. gridmix -generate 100m foo -"));
    }


}
