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

package org.apache.hadoop.io;

import java.io.*;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * Singleton Writable with no data.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class NullWritable implements WritableComparable<NullWritable> {

    private static final NullWritable THIS = new NullWritable();

    private NullWritable() {
    }                       // no public ctor

    /**
     * Returns the single instance of this class.
     */
    public static NullWritable get() {
        return THIS;
    }

    @Override
    public String toString() {
        return "(null)";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(NullWritable other) {
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NullWritable;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
    }

    @Override
    public void write(DataOutput out) throws IOException {
    }

    /**
     * A Comparator &quot;optimized&quot; for NullWritable.
     */
    public static class Comparator extends WritableComparator {
        public Comparator() {
            super(NullWritable.class);
        }

        /**
         * Compare the buffers in serialized form.
         */
        @Override
        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            assert 0 == l1;
            assert 0 == l2;
            return 0;
        }
    }

    static {                                        // register this comparator
        WritableComparator.define(NullWritable.class, new Comparator());
    }
}

