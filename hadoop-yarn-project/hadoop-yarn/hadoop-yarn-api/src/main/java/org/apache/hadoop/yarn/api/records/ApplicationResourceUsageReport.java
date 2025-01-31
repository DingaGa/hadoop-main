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

package org.apache.hadoop.yarn.api.records;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.util.Records;

/**
 * Contains various scheduling metrics to be reported by UI and CLI.
 */
@Public
@Stable
public abstract class ApplicationResourceUsageReport {

    @Private
    @Unstable
    public static ApplicationResourceUsageReport newInstance(
            int numUsedContainers, int numReservedContainers, Resource usedResources,
            Resource reservedResources, Resource neededResources) {
        ApplicationResourceUsageReport report =
                Records.newRecord(ApplicationResourceUsageReport.class);
        report.setNumUsedContainers(numUsedContainers);
        report.setNumReservedContainers(numReservedContainers);
        report.setUsedResources(usedResources);
        report.setReservedResources(reservedResources);
        report.setNeededResources(neededResources);
        return report;
    }

    /**
     * Get the number of used containers.  -1 for invalid/inaccessible reports.
     * @return the number of used containers
     */
    @Public
    @Stable
    public abstract int getNumUsedContainers();

    /**
     * Set the number of used containers
     * @param num_containers the number of used containers
     */
    @Private
    @Unstable
    public abstract void setNumUsedContainers(int num_containers);

    /**
     * Get the number of reserved containers.  -1 for invalid/inaccessible reports.
     * @return the number of reserved containers
     */
    @Private
    @Unstable
    public abstract int getNumReservedContainers();

    /**
     * Set the number of reserved containers
     * @param num_reserved_containers the number of reserved containers
     */
    @Private
    @Unstable
    public abstract void setNumReservedContainers(int num_reserved_containers);

    /**
     * Get the used <code>Resource</code>.  -1 for invalid/inaccessible reports.
     * @return the used <code>Resource</code>
     */
    @Public
    @Stable
    public abstract Resource getUsedResources();

    @Private
    @Unstable
    public abstract void setUsedResources(Resource resources);

    /**
     * Get the reserved <code>Resource</code>.  -1 for invalid/inaccessible reports.
     * @return the reserved <code>Resource</code>
     */
    @Public
    @Stable
    public abstract Resource getReservedResources();

    @Private
    @Unstable
    public abstract void setReservedResources(Resource reserved_resources);

    /**
     * Get the needed <code>Resource</code>.  -1 for invalid/inaccessible reports.
     * @return the needed <code>Resource</code>
     */
    @Public
    @Stable
    public abstract Resource getNeededResources();

    @Private
    @Unstable
    public abstract void setNeededResources(Resource needed_resources);
}
