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

import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.mapreduce.JobID;

import org.apache.avro.util.Utf8;

/**
 * Event to record the change of priority of a job
 *
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
public class JobPriorityChangeEvent implements HistoryEvent {
    private JobPriorityChange datum = new JobPriorityChange();

    /** Generate an event to record changes in Job priority
     * @param id Job Id
     * @param priority The new priority of the job
     */
    public JobPriorityChangeEvent(JobID id, JobPriority priority) {
        datum.jobid = new Utf8(id.toString());
        datum.priority = new Utf8(priority.name());
    }

    JobPriorityChangeEvent() {
    }

    public Object getDatum() {
        return datum;
    }

    public void setDatum(Object datum) {
        this.datum = (JobPriorityChange) datum;
    }

    /** Get the Job ID */
    public JobID getJobId() {
        return JobID.forName(datum.jobid.toString());
    }

    /** Get the job priority */
    public JobPriority getPriority() {
        return JobPriority.valueOf(datum.priority.toString());
    }

    /** Get the event type */
    public EventType getEventType() {
        return EventType.JOB_PRIORITY_CHANGED;
    }

}
