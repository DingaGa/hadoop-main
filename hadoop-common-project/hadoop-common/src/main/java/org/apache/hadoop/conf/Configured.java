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

package org.apache.hadoop.conf;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * Base class for things that may be configured with a {@link Configuration}.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class Configured implements Configurable {

    private Configuration conf;

    /**
     * Construct a Configured.
     */
    public Configured() {
        this(null);
    }

    /**
     * Construct a Configured.
     */
    public Configured(Configuration conf) {
        setConf(conf);
    }

    // inherit javadoc
    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    // inherit javadoc
    @Override
    public Configuration getConf() {
        return conf;
    }

}
