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
package org.apache.hadoop.ipc;

import static org.junit.Assert.*;

import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;


public class TestRPCCallBenchmark {

    @Test(timeout = 20000)
    public void testBenchmarkWithWritable() throws Exception {
        int rc = ToolRunner.run(new RPCCallBenchmark(),
                new String[]{
                        "--clientThreads", "30",
                        "--serverThreads", "30",
                        "--time", "5",
                        "--serverReaderThreads", "4",
                        "--messageSize", "1024",
                        "--engine", "writable"});
        assertEquals(0, rc);
    }

    @Test(timeout = 20000)
    public void testBenchmarkWithProto() throws Exception {
        int rc = ToolRunner.run(new RPCCallBenchmark(),
                new String[]{
                        "--clientThreads", "30",
                        "--serverThreads", "30",
                        "--time", "5",
                        "--serverReaderThreads", "4",
                        "--messageSize", "1024",
                        "--engine", "protobuf"});
        assertEquals(0, rc);
    }
}
