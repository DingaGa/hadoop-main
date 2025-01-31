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

package org.apache.hadoop.metrics2;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * The metrics collector interface
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public interface MetricsCollector {
    /**
     * Add a metrics record
     *
     * @param name of the record
     * @return a {@link MetricsRecordBuilder} for the record {@code name}
     */
    public MetricsRecordBuilder addRecord(String name);

    /**
     * Add a metrics record
     *
     * @param info of the record
     * @return a {@link MetricsRecordBuilder} for metrics {@code info}
     */
    public MetricsRecordBuilder addRecord(MetricsInfo info);
}
