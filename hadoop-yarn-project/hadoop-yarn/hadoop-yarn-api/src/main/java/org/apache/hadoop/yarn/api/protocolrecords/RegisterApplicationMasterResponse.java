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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ApplicationMasterProtocol;
import org.apache.hadoop.yarn.api.records.ApplicationAccessType;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.NMToken;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.util.Records;

/**
 * <p>The response sent by the <code>ResourceManager</code> to a new 
 * <code>ApplicationMaster</code> on registration.</p>
 *
 * <p>The response contains critical details such as:
 * <ul>
 *   <li>Maximum capability for allocated resources in the cluster.</li>
 *   <li><code>ApplicationACL</code>s for the application.</li>
 *   <li>ClientToAMToken master key.</li>
 * </ul>
 * </p>
 *
 * @see ApplicationMasterProtocol#registerApplicationMaster(RegisterApplicationMasterRequest)
 */
@Public
@Stable
public abstract class RegisterApplicationMasterResponse {

    @Private
    @Unstable
    public static RegisterApplicationMasterResponse newInstance(
            Resource minCapability, Resource maxCapability,
            Map<ApplicationAccessType, String> acls, ByteBuffer key,
            List<Container> containersFromPreviousAttempt, String queue,
            List<NMToken> nmTokensFromPreviousAttempts) {
        RegisterApplicationMasterResponse response =
                Records.newRecord(RegisterApplicationMasterResponse.class);
        response.setMaximumResourceCapability(maxCapability);
        response.setApplicationACLs(acls);
        response.setClientToAMTokenMasterKey(key);
        response.setContainersFromPreviousAttempts(containersFromPreviousAttempt);
        response.setNMTokensFromPreviousAttempts(nmTokensFromPreviousAttempts);
        response.setQueue(queue);
        return response;
    }

    /**
     * Get the maximum capability for any {@link Resource} allocated by the
     * <code>ResourceManager</code> in the cluster.
     * @return maximum capability of allocated resources in the cluster
     */
    @Public
    @Stable
    public abstract Resource getMaximumResourceCapability();

    @Private
    @Unstable
    public abstract void setMaximumResourceCapability(Resource capability);

    /**
     * Get the <code>ApplicationACL</code>s for the application.
     * @return all the <code>ApplicationACL</code>s
     */
    @Public
    @Stable
    public abstract Map<ApplicationAccessType, String> getApplicationACLs();

    /**
     * Set the <code>ApplicationACL</code>s for the application.
     * @param acls
     */
    @Private
    @Unstable
    public abstract void setApplicationACLs(Map<ApplicationAccessType, String> acls);

    /**
     * <p>Get ClientToAMToken master key.</p>
     * <p>The ClientToAMToken master key is sent to <code>ApplicationMaster</code>
     * by <code>ResourceManager</code> via {@link RegisterApplicationMasterResponse}
     * , used to verify corresponding ClientToAMToken.</p>
     */
    @Public
    @Stable
    public abstract ByteBuffer getClientToAMTokenMasterKey();

    /**
     * Set ClientToAMToken master key.
     */
    @Public
    @Stable
    public abstract void setClientToAMTokenMasterKey(ByteBuffer key);

    /**
     * <p>Get the queue that the application was placed in.<p>
     */
    @Public
    @Stable
    public abstract String getQueue();

    /**
     * <p>Set the queue that the application was placed in.<p>
     */
    @Public
    @Stable
    public abstract void setQueue(String queue);

    /**
     * <p>
     * Get the list of running containers as viewed by
     * <code>ResourceManager</code> from previous application attempts.
     * </p>
     *
     * @return the list of running containers as viewed by
     *         <code>ResourceManager</code> from previous application attempts
     * @see RegisterApplicationMasterResponse#getNMTokensFromPreviousAttempts()
     */
    @Public
    @Unstable
    public abstract List<Container> getContainersFromPreviousAttempts();

    /**
     * Set the list of running containers as viewed by
     * <code>ResourceManager</code> from previous application attempts.
     *
     * @param containersFromPreviousAttempt
     *          the list of running containers as viewed by
     *          <code>ResourceManager</code> from previous application attempts.
     */
    @Private
    @Unstable
    public abstract void setContainersFromPreviousAttempts(
            List<Container> containersFromPreviousAttempt);

    /**
     * Get the list of NMTokens for communicating with the NMs where the
     * containers of previous application attempts are running.
     *
     * @return the list of NMTokens for communicating with the NMs where the
     *         containers of previous application attempts are running.
     *
     * @see RegisterApplicationMasterResponse#getContainersFromPreviousAttempts()
     */
    @Public
    @Stable
    public abstract List<NMToken> getNMTokensFromPreviousAttempts();

    /**
     * Set the list of NMTokens for communicating with the NMs where the the
     * containers of previous application attempts are running.
     *
     * @param nmTokens
     *          the list of NMTokens for communicating with the NMs where the
     *          containers of previous application attempts are running.
     */
    @Private
    @Unstable
    public abstract void setNMTokensFromPreviousAttempts(List<NMToken> nmTokens);
}
