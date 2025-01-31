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
package org.apache.hadoop.yarn.server.applicationhistoryservice.webapp;

import static org.apache.hadoop.yarn.webapp.YarnWebParams.CONTAINER_ID;
import static org.apache.hadoop.yarn.webapp.YarnWebParams.ENTITY_STRING;

import org.apache.hadoop.yarn.webapp.SubView;
import org.apache.hadoop.yarn.webapp.log.AggregatedLogsBlock;

public class AHSLogsPage extends AHSView {
    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.hadoop.yarn.server.applicationhistoryservice.webapp.AHSView#
     * preHead(org.apache.hadoop .yarn.webapp.hamlet.Hamlet.HTML)
     */
    @Override
    protected void preHead(Page.HTML<_> html) {
        String logEntity = $(ENTITY_STRING);
        if (logEntity == null || logEntity.isEmpty()) {
            logEntity = $(CONTAINER_ID);
        }
        if (logEntity == null || logEntity.isEmpty()) {
            logEntity = "UNKNOWN";
        }
        commonPreHead(html);
    }

    /**
     * The content of this page is the AggregatedLogsBlock
     *
     * @return AggregatedLogsBlock.class
     */
    @Override
    protected Class<? extends SubView> content() {
        return AggregatedLogsBlock.class;
    }
}
