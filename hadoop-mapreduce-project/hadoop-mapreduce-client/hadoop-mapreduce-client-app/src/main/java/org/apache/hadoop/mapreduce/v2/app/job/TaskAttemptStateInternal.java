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

package org.apache.hadoop.mapreduce.v2.app.job;

import org.apache.hadoop.classification.InterfaceAudience.Private;

/**
 * TaskAttemptImpl internal state machine states.
 *
 */
@Private
public enum TaskAttemptStateInternal {
    NEW,
    UNASSIGNED,
    ASSIGNED,
    RUNNING,
    COMMIT_PENDING,
    SUCCESS_CONTAINER_CLEANUP,
    SUCCEEDED,
    FAIL_CONTAINER_CLEANUP,
    FAIL_TASK_CLEANUP,
    FAILED,
    KILL_CONTAINER_CLEANUP,
    KILL_TASK_CLEANUP,
    KILLED,
}