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

import org.apache.hadoop.yarn.api.ApplicationMasterProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.FinishApplicationMasterRequest;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterRequest;

/**
 * This exception is thrown when an Application Master tries to unregister by calling
 * {@link ApplicationMasterProtocol#finishApplicationMaster(FinishApplicationMasterRequest)}
 * API without first registering by calling
 * {@link ApplicationMasterProtocol#registerApplicationMaster(RegisterApplicationMasterRequest)}
 * or after an RM restart. The ApplicationMaster is expected to call
 * {@link ApplicationMasterProtocol#registerApplicationMaster(RegisterApplicationMasterRequest)}
 * and retry.
 */

public class ApplicationMasterNotRegisteredException extends YarnException {

    private static final long serialVersionUID = 13498238L;

    public ApplicationMasterNotRegisteredException(Throwable cause) {
        super(cause);
    }

    public ApplicationMasterNotRegisteredException(String message) {
        super(message);
    }

    public ApplicationMasterNotRegisteredException(String message, Throwable
            cause) {
        super(message, cause);
    }
}
