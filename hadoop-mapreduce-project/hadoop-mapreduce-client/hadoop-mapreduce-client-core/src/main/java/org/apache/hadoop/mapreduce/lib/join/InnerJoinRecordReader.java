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

package org.apache.hadoop.mapreduce.lib.join;

import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Full inner join.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class InnerJoinRecordReader<K extends WritableComparable<?>>
        extends JoinRecordReader<K> {

    InnerJoinRecordReader(int id, Configuration conf, int capacity,
                          Class<? extends WritableComparator> cmpcl) throws IOException {
        super(id, conf, capacity, cmpcl);
    }

    /**
     * Return true iff the tuple is full (all data sources contain this key).
     */
    protected boolean combine(Object[] srcs, TupleWritable dst) {
        assert srcs.length == dst.size();
        for (int i = 0; i < srcs.length; ++i) {
            if (!dst.has(i)) {
                return false;
            }
        }
        return true;
    }
}
