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
package org.apache.hadoop.mapreduce.jobhistory;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.mapreduce.TaskType;

/**
 * Event to record the normalized map/reduce requirements.
 *
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
public class NormalizedResourceEvent implements HistoryEvent {
    private int memory;
    private TaskType taskType;

    /**
     * Normalized request when sent to the Resource Manager.
     * @param taskType the tasktype of the request.
     * @param memory the normalized memory requirements.
     */
    public NormalizedResourceEvent(TaskType taskType, int memory) {
        this.memory = memory;
        this.taskType = taskType;
    }

    /**
     * the tasktype for the event.
     * @return the tasktype for the event.
     */
    public TaskType getTaskType() {
        return this.taskType;
    }

    /**
     * the normalized memory
     * @return the normalized memory
     */
    public int getMemory() {
        return this.memory;
    }

    @Override
    public EventType getEventType() {
        return EventType.NORMALIZED_RESOURCE;
    }

    @Override
    public Object getDatum() {
        throw new UnsupportedOperationException("Not a seriable object");
    }

    @Override
    public void setDatum(Object datum) {
        throw new UnsupportedOperationException("Not a seriable object");
    }
}