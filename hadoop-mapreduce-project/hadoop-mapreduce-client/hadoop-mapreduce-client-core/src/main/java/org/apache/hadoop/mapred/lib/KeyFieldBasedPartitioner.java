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

package org.apache.hadoop.mapred.lib;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

/**
 *  Defines a way to partition keys based on certain key fields (also see
 *  {@link KeyFieldBasedComparator}.
 *  The key specification supported is of the form -k pos1[,pos2], where,
 *  pos is of the form f[.c][opts], where f is the number
 *  of the key field to use, and c is the number of the first character from
 *  the beginning of the field. Fields and character posns are numbered
 *  starting with 1; a character position of zero in pos2 indicates the
 *  field's last character. If '.c' is omitted from pos1, it defaults to 1
 *  (the beginning of the field); if omitted from pos2, it defaults to 0
 *  (the end of the field).
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class KeyFieldBasedPartitioner<K2, V2> extends
        org.apache.hadoop.mapreduce.lib.partition.KeyFieldBasedPartitioner<K2, V2>
        implements Partitioner<K2, V2> {

    public void configure(JobConf job) {
        super.setConf(job);
    }
}
