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

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ApplicationHistoryProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationAttemptReportRequest;

/**
 * This exception is thrown on
 * {@link ApplicationHistoryProtocol#getApplicationAttemptReport (GetApplicationAttemptReportRequest)}
 * API when the Application Attempt doesn't exist in Application History Server
 */
@Public
@Unstable
public class ApplicationAttemptNotFoundException extends YarnException {

    private static final long serialVersionUID = 8694508L;

    public ApplicationAttemptNotFoundException(Throwable cause) {
        super(cause);
    }

    public ApplicationAttemptNotFoundException(String message) {
        super(message);
    }

    public ApplicationAttemptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
