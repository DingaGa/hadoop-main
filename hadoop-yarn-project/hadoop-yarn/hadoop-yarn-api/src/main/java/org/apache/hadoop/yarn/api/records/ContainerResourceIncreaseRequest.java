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

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.yarn.util.Records;

/**
 * Used by Application Master, send a container resource increase request to
 * Resource Manager
 */
@Public
public abstract class ContainerResourceIncreaseRequest {
    @Public
    public static ContainerResourceIncreaseRequest newInstance(
            ContainerId existingContainerId, Resource targetCapability) {
        ContainerResourceIncreaseRequest context = Records
                .newRecord(ContainerResourceIncreaseRequest.class);
        context.setContainerId(existingContainerId);
        context.setCapability(targetCapability);
        return context;
    }

    @Public
    public abstract ContainerId getContainerId();

    @Public
    public abstract void setContainerId(ContainerId containerId);

    @Public
    public abstract Resource getCapability();

    @Public
    public abstract void setCapability(Resource capability);

    @Override
    public int hashCode() {
        return getCapability().hashCode() + getContainerId().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ContainerResourceIncreaseRequest) {
            ContainerResourceIncreaseRequest ctx =
                    (ContainerResourceIncreaseRequest) other;

            if (getContainerId() == null && ctx.getContainerId() != null) {
                return false;
            } else if (!getContainerId().equals(ctx.getContainerId())) {
                return false;
            }

            if (getCapability() == null && ctx.getCapability() != null) {
                return false;
            } else if (!getCapability().equals(ctx.getCapability())) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}
