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
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.records.Token;
import org.apache.hadoop.yarn.util.Records;

/**
 * The request issued by the client to renew a delegation token from
 * the {@code ResourceManager}.
 */
@Private
@Unstable
public abstract class RenewDelegationTokenRequest {
    @Private
    @Unstable
    public static RenewDelegationTokenRequest newInstance(Token dToken) {
        RenewDelegationTokenRequest request =
                Records.newRecord(RenewDelegationTokenRequest.class);
        request.setDelegationToken(dToken);
        return request;
    }

    /**
     * Get the delegation token requested to be renewed by the client.
     * @return the delegation token requested to be renewed by the client.
     */
    @Private
    @Unstable
    public abstract Token getDelegationToken();

    @Private
    @Unstable
    public abstract void setDelegationToken(Token dToken);
}
