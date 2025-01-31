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

package org.apache.hadoop.mapred.lib.aggregate;

import java.util.Map.Entry;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;

/**
 * This class implements the common functionalities of 
 * the subclasses of ValueAggregatorDescriptor class.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class ValueAggregatorBaseDescriptor extends org.apache.hadoop.mapreduce.
        lib.aggregate.ValueAggregatorBaseDescriptor
        implements ValueAggregatorDescriptor {

    static public final String UNIQ_VALUE_COUNT = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.UNIQ_VALUE_COUNT;

    static public final String LONG_VALUE_SUM = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.LONG_VALUE_SUM;

    static public final String DOUBLE_VALUE_SUM = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.DOUBLE_VALUE_SUM;

    static public final String VALUE_HISTOGRAM = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.VALUE_HISTOGRAM;

    static public final String LONG_VALUE_MAX = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.LONG_VALUE_MAX;

    static public final String LONG_VALUE_MIN = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.LONG_VALUE_MIN;

    static public final String STRING_VALUE_MAX = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.STRING_VALUE_MAX;

    static public final String STRING_VALUE_MIN = org.apache.hadoop.mapreduce.
            lib.aggregate.ValueAggregatorBaseDescriptor.STRING_VALUE_MIN;

    private static long maxNumItems = Long.MAX_VALUE;

    /**
     *
     * @param type the aggregation type
     * @param id the aggregation id
     * @param val the val associated with the id to be aggregated
     * @return an Entry whose key is the aggregation id prefixed with
     * the aggregation type.
     */
    public static Entry<Text, Text> generateEntry(String type, String id, Text val) {
        return org.apache.hadoop.mapreduce.lib.aggregate.
                ValueAggregatorBaseDescriptor.generateEntry(type, id, val);
    }

    /**
     *
     * @param type the aggregation type
     * @return a value aggregator of the given type.
     */
    static public ValueAggregator generateValueAggregator(String type) {
        ValueAggregator retv = null;
        if (type.compareToIgnoreCase(LONG_VALUE_SUM) == 0) {
            retv = new LongValueSum();
        }
        if (type.compareToIgnoreCase(LONG_VALUE_MAX) == 0) {
            retv = new LongValueMax();
        } else if (type.compareToIgnoreCase(LONG_VALUE_MIN) == 0) {
            retv = new LongValueMin();
        } else if (type.compareToIgnoreCase(STRING_VALUE_MAX) == 0) {
            retv = new StringValueMax();
        } else if (type.compareToIgnoreCase(STRING_VALUE_MIN) == 0) {
            retv = new StringValueMin();
        } else if (type.compareToIgnoreCase(DOUBLE_VALUE_SUM) == 0) {
            retv = new DoubleValueSum();
        } else if (type.compareToIgnoreCase(UNIQ_VALUE_COUNT) == 0) {
            retv = new UniqValueCount(maxNumItems);
        } else if (type.compareToIgnoreCase(VALUE_HISTOGRAM) == 0) {
            retv = new ValueHistogram();
        }
        return retv;
    }

    /**
     * get the input file name.
     *
     * @param job a job configuration object
     */
    public void configure(JobConf job) {
        super.configure(job);
        maxNumItems = job.getLong("aggregate.max.num.unique.values",
                Long.MAX_VALUE);
    }
}
