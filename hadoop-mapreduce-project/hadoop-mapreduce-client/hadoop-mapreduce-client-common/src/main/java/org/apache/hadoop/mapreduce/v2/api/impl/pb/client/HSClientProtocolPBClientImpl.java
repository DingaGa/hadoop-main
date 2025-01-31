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

package org.apache.hadoop.mapreduce.v2.api.impl.pb.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.ProtobufRpcEngine;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.mapreduce.v2.api.HSClientProtocol;
import org.apache.hadoop.mapreduce.v2.api.HSClientProtocolPB;

public class HSClientProtocolPBClientImpl extends MRClientProtocolPBClientImpl
        implements HSClientProtocol {

    public HSClientProtocolPBClientImpl(long clientVersion,
                                        InetSocketAddress addr, Configuration conf) throws IOException {
        super();
        RPC.setProtocolEngine(conf, HSClientProtocolPB.class,
                ProtobufRpcEngine.class);
        proxy = (HSClientProtocolPB) RPC.getProxy(
                HSClientProtocolPB.class, clientVersion, addr, conf);
    }
}