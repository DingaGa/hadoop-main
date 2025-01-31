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

package org.apache.hadoop.yarn.exceptions;

import org.apache.hadoop.yarn.api.ContainerManagementProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.StartContainersRequest;

/**
 * This exception is thrown by a NodeManager that is rejecting start-container
 * requests via
 * {@link ContainerManagementProtocol#startContainers(StartContainersRequest)}
 * for containers allocated by a previous instance of the RM.
 */
public class InvalidContainerException extends YarnException {

    private static final long serialVersionUID = 1L;

    public InvalidContainerException(String msg) {
        super(msg);
    }
}
