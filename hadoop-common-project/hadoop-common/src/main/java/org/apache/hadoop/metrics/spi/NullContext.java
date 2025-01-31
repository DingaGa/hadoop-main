/*
 * NullContext.java
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metrics.spi;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * Null metrics context: a metrics context which does nothing.  Used as the
 * default context, so that no performance data is emitted if no configuration
 * data is found.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class NullContext extends AbstractMetricsContext {

    /**
     * Creates a new instance of NullContext
     */
    @InterfaceAudience.Private
    public NullContext() {
    }

    /**
     * Do-nothing version of startMonitoring
     */
    @Override
    @InterfaceAudience.Private
    public void startMonitoring() {
    }

    /**
     * Do-nothing version of emitRecord
     */
    @Override
    @InterfaceAudience.Private
    protected void emitRecord(String contextName, String recordName,
                              OutputRecord outRec) {
    }

    /**
     * Do-nothing version of update
     */
    @Override
    @InterfaceAudience.Private
    protected void update(MetricsRecordImpl record) {
    }

    /**
     * Do-nothing version of remove
     */
    @Override
    @InterfaceAudience.Private
    protected void remove(MetricsRecordImpl record) {
    }
}
