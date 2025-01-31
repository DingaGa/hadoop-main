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

package org.apache.hadoop.mapreduce;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.LocalJobRunner;
import org.apache.hadoop.mapreduce.server.jobtracker.JTConfig;
import org.junit.Test;

public class TestClientProtocolProviderImpls extends TestCase {

    @Test
    public void testClusterWithLocalClientProvider() throws Exception {

        Configuration conf = new Configuration();

        try {
            conf.set(MRConfig.FRAMEWORK_NAME, "incorrect");
            new Cluster(conf);
            fail("Cluster should not be initialized with incorrect framework name");
        } catch (IOException e) {

        }

        conf.set(MRConfig.FRAMEWORK_NAME, "local");
        Cluster cluster = new Cluster(conf);
        assertTrue(cluster.getClient() instanceof LocalJobRunner);
        cluster.close();
    }

    @Test
    public void testClusterWithJTClientProvider() throws Exception {

        Configuration conf = new Configuration();
        try {
            conf.set(MRConfig.FRAMEWORK_NAME, "incorrect");
            new Cluster(conf);
            fail("Cluster should not be initialized with incorrect framework name");

        } catch (IOException e) {

        }

        try {
            conf.set(MRConfig.FRAMEWORK_NAME, "classic");
            conf.set(JTConfig.JT_IPC_ADDRESS, "local");
            new Cluster(conf);
            fail("Cluster with classic Framework name shouldnot use local JT address");

        } catch (IOException e) {

        }

        try {
            conf = new Configuration();
            conf.set(MRConfig.FRAMEWORK_NAME, "classic");
            conf.set(JTConfig.JT_IPC_ADDRESS, "127.0.0.1:0");
            Cluster cluster = new Cluster(conf);
            cluster.close();
        } catch (IOException e) {

        }
    }

    @Test
    public void testClusterException() {

        Configuration conf = new Configuration();
        conf.set(MRConfig.FRAMEWORK_NAME, MRConfig.CLASSIC_FRAMEWORK_NAME);
        conf.set(JTConfig.JT_IPC_ADDRESS, "local");

        // initializing a cluster with this conf should throw an error.
        // However the exception thrown should not be specific to either
        // the job tracker client provider or the local provider
        boolean errorThrown = false;
        try {
            Cluster cluster = new Cluster(conf);
            cluster.close();
            fail("Not expected - cluster init should have failed");
        } catch (IOException e) {
            errorThrown = true;
            assert (e.getMessage().contains("Cannot initialize Cluster. Please check"));
        }
        assert (errorThrown);
    }
}
