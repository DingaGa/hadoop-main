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

package org.apache.hadoop.fs.azure;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.DelegateToFileSystem;


/**
 * WASB implementation of AbstractFileSystem.
 * This impl delegates to the old FileSystem
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class Wasb extends DelegateToFileSystem {

    Wasb(final URI theUri, final Configuration conf) throws IOException,
            URISyntaxException {
        super(theUri, new NativeAzureFileSystem(), conf, "wasb", false);
    }

    @Override
    public int getUriDefaultPort() {
        return -1;
    }
}
