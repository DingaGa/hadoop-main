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

package org.apache.hadoop.yarn.api.protocolrecords;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ApplicationClientProtocol;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>The response sent by the <code>ResourceManager</code> to a client
 * requesting cluster metrics.<p>
 *
 * @see YarnClusterMetrics
 * @see ApplicationClientProtocol#getClusterMetrics(GetClusterMetricsRequest)
 */
@Public
@Stable
public abstract class GetClusterMetricsResponse {

    @Private
    @Unstable
    public static GetClusterMetricsResponse
    newInstance(YarnClusterMetrics metrics) {
        GetClusterMetricsResponse response =
                Records.newRecord(GetClusterMetricsResponse.class);
        response.setClusterMetrics(metrics);
        return response;
    }

    /**
     * Get the <code>YarnClusterMetrics</code> for the cluster.
     * @return <code>YarnClusterMetrics</code> for the cluster
     */
    @Public
    @Stable
    public abstract YarnClusterMetrics getClusterMetrics();

    @Private
    @Unstable
    public abstract void setClusterMetrics(YarnClusterMetrics metrics);
}
