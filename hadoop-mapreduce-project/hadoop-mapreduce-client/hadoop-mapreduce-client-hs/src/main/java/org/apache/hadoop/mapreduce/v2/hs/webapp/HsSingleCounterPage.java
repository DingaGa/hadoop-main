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

package org.apache.hadoop.mapreduce.v2.hs.webapp;

import static org.apache.hadoop.mapreduce.v2.app.webapp.AMParams.TASK_ID;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.*;

import org.apache.hadoop.mapreduce.v2.app.webapp.SingleCounterBlock;
import org.apache.hadoop.yarn.webapp.SubView;

/**
 * Render the counters page
 */
public class HsSingleCounterPage extends HsView {

    /*
     * (non-Javadoc)
     * @see org.apache.hadoop.mapreduce.v2.hs.webapp.HsView#preHead(org.apache.hadoop.yarn.webapp.hamlet.Hamlet.HTML)
     */
    @Override
    protected void preHead(Page.HTML<_> html) {
        commonPreHead(html);
        String tid = $(TASK_ID);
        String activeNav = "2";
        if (tid == null || tid.isEmpty()) {
            activeNav = "1";
        }
        set(initID(ACCORDION, "nav"), "{autoHeight:false, active:" + activeNav + "}");
        set(DATATABLES_ID, "singleCounter");
        set(initID(DATATABLES, "singleCounter"), counterTableInit());
        setTableStyles(html, "singleCounter");
    }

    /**
     * @return The end of a javascript map that is the jquery datatable
     * configuration for the jobs table.  the Jobs table is assumed to be
     * rendered by the class returned from {@link #content()}
     */
    private String counterTableInit() {
        return tableInit().
                append(", aoColumnDefs:[").
                append("{'sType':'title-numeric', 'aTargets': [ 1 ] }").
                append("]}").
                toString();
    }

    /**
     * The content of this page is the CountersBlock now.
     * @return CountersBlock.class
     */
    @Override
    protected Class<? extends SubView> content() {
        return SingleCounterBlock.class;
    }
}
