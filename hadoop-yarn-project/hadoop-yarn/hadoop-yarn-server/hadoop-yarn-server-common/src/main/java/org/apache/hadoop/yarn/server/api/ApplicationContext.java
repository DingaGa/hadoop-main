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

package org.apache.hadoop.yarn.server.api;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerReport;

@Public
@Unstable
public interface ApplicationContext {
    /**
     * This method returns Application {@link ApplicationReport} for the specified
     * {@link ApplicationId}.
     *
     * @param appId
     *
     * @return {@link ApplicationReport} for the ApplicationId.
     * @throws IOException
     */
    @Public
    @Unstable
    ApplicationReport getApplication(ApplicationId appId) throws IOException;

    /**
     * This method returns all Application {@link ApplicationReport}s
     *
     * @return map of {@link ApplicationId} to {@link ApplicationReport}s.
     * @throws IOException
     */
    @Public
    @Unstable
    Map<ApplicationId, ApplicationReport> getAllApplications() throws IOException;

    /**
     * Application can have multiple application attempts
     * {@link ApplicationAttemptReport}. This method returns the all
     * {@link ApplicationAttemptReport}s for the Application.
     *
     * @param appId
     *
     * @return all {@link ApplicationAttemptReport}s for the Application.
     * @throws IOException
     */
    @Public
    @Unstable
    Map<ApplicationAttemptId, ApplicationAttemptReport> getApplicationAttempts(
            ApplicationId appId) throws IOException;

    /**
     * This method returns {@link ApplicationAttemptReport} for specified
     * {@link ApplicationId}.
     *
     * @param appAttemptId
     *          {@link ApplicationAttemptId}
     * @return {@link ApplicationAttemptReport} for ApplicationAttemptId
     * @throws IOException
     */
    @Public
    @Unstable
    ApplicationAttemptReport getApplicationAttempt(
            ApplicationAttemptId appAttemptId) throws IOException;

    /**
     * This method returns {@link ContainerReport} for specified
     * {@link ContainerId}.
     *
     * @param containerId
     *          {@link ContainerId}
     * @return {@link ContainerReport} for ContainerId
     * @throws IOException
     */
    @Public
    @Unstable
    ContainerReport getContainer(ContainerId containerId) throws IOException;

    /**
     * This method returns {@link ContainerReport} for specified
     * {@link ApplicationAttemptId}.
     *
     * @param appAttemptId
     *          {@link ApplicationAttemptId}
     * @return {@link ContainerReport} for ApplicationAttemptId
     * @throws IOException
     */
    @Public
    @Unstable
    ContainerReport getAMContainer(ApplicationAttemptId appAttemptId)
            throws IOException;

    /**
     * This method returns Map of {@link ContainerId} to {@link ContainerReport}
     * for specified {@link ApplicationAttemptId}.
     *
     * @param appAttemptId
     *          {@link ApplicationAttemptId}
     * @return Map of {@link ContainerId} to {@link ContainerReport} for
     *         ApplicationAttemptId
     * @throws IOException
     */
    @Public
    @Unstable
    Map<ContainerId, ContainerReport> getContainers(
            ApplicationAttemptId appAttemptId) throws IOException;
}
