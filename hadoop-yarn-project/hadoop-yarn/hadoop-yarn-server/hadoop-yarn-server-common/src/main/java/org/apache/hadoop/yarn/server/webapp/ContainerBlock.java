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
package org.apache.hadoop.yarn.server.webapp;

import static org.apache.hadoop.yarn.util.StringHelper.join;
import static org.apache.hadoop.yarn.webapp.YarnWebParams.CONTAINER_ID;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.server.api.ApplicationContext;
import org.apache.hadoop.yarn.server.webapp.dao.ContainerInfo;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Times;
import org.apache.hadoop.yarn.webapp.view.HtmlBlock;
import org.apache.hadoop.yarn.webapp.view.InfoBlock;

import com.google.inject.Inject;

public class ContainerBlock extends HtmlBlock {

    private static final Log LOG = LogFactory.getLog(ContainerBlock.class);
    private final ApplicationContext appContext;

    @Inject
    public ContainerBlock(ApplicationContext appContext, ViewContext ctx) {
        super(ctx);
        this.appContext = appContext;
    }

    @Override
    protected void render(Block html) {
        String containerid = $(CONTAINER_ID);
        if (containerid.isEmpty()) {
            puts("Bad request: requires container ID");
            return;
        }

        ContainerId containerId = null;
        try {
            containerId = ConverterUtils.toContainerId(containerid);
        } catch (IllegalArgumentException e) {
            puts("Invalid container ID: " + containerid);
            return;
        }

        ContainerReport containerReport;
        try {
            containerReport = appContext.getContainer(containerId);
        } catch (IOException e) {
            String message = "Failed to read the container " + containerid + ".";
            LOG.error(message, e);
            html.p()._(message)._();
            return;
        }
        if (containerReport == null) {
            puts("Container not found: " + containerid);
            return;
        }

        ContainerInfo container = new ContainerInfo(containerReport);
        setTitle(join("Container ", containerid));

        info("Container Overview")
                ._("State:", container.getContainerState())
                ._("Exit Status:", container.getContainerExitStatus())
                ._("Node:", container.getAssignedNodeId())
                ._("Priority:", container.getPriority())
                ._("Started:", Times.format(container.getStartedTime()))
                ._(
                        "Elapsed:",
                        StringUtils.formatTime(Times.elapsed(container.getStartedTime(),
                                container.getFinishedTime())))
                ._(
                        "Resource:",
                        container.getAllocatedMB() + " Memory, "
                                + container.getAllocatedVCores() + " VCores")
                ._("Logs:", container.getLogUrl() == null ? "#" : container.getLogUrl(),
                        container.getLogUrl() == null ? "N/A" : "Logs")
                ._("Diagnostics:", container.getDiagnosticsInfo());

        html._(InfoBlock.class);
    }
}