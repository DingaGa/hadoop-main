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
package org.apache.hadoop.yarn.server.nodemanager.containermanager.localizer.event;

import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.localizer.LocalResourceRequest;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.localizer.LocalizerContext;

public class ResourceRequestEvent extends ResourceEvent {

    private final LocalizerContext context;
    private final LocalResourceVisibility vis;

    public ResourceRequestEvent(LocalResourceRequest resource,
                                LocalResourceVisibility vis, LocalizerContext context) {
        super(resource, ResourceEventType.REQUEST);
        this.vis = vis;
        this.context = context;
    }

    public LocalizerContext getContext() {
        return context;
    }

    public LocalResourceVisibility getVisibility() {
        return vis;
    }

}
