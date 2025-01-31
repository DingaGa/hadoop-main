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
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.yarn.api.ApplicationClientProtocol;

/**
 * <p>
 * <code>QueueACL</code> enumerates the various ACLs for queues.
 * </p>
 *
 * <p>
 * The ACL is one of:
 * <ul>
 * <li>{@link #SUBMIT_APPLICATIONS} - ACL to submit applications to the
 * queue.</li>
 * <li>{@link #ADMINISTER_QUEUE} - ACL to administer the queue.</li>
 * </ul>
 * </p>
 *
 * @see QueueInfo
 * @see ApplicationClientProtocol#getQueueUserAcls(org.apache.hadoop.yarn.api.protocolrecords.GetQueueUserAclsInfoRequest)
 */
@Public
@Stable
public enum QueueACL {
    /**
     * ACL to submit applications to the queue.
     */
    SUBMIT_APPLICATIONS,

    /**
     * ACL to administer the queue.
     */
    ADMINISTER_QUEUE,
}