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
 * A WritableComparable for a single byte.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class ByteWritable implements WritableComparable<ByteWritable> {
    private byte value;

    public ByteWritable() {
    }

    public ByteWritable(byte value) {
        set(value);
    }

    /**
     * Set the value of this ByteWritable.
     */
    public void set(byte value) {
        this.value = value;
    }

    /**
     * Return the value of this ByteWritable.
     */
    public byte get() {
        return value;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        value = in.readByte();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeByte(value);
    }

    /**
     * Returns true iff <code>o</code> is a ByteWritable with the same value.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ByteWritable)) {
            return false;
        }
        ByteWritable other = (ByteWritable) o;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    /**
     * Compares two ByteWritables.
     */
    @Override
    public int compareTo(ByteWritable o) {
        int thisValue = this.value;
        int thatValue = o.value;
        return (thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1));
    }

    @Override
    public String toString() {
        return Byte.toString(value);
    }

    /**
     * A Comparator optimized for ByteWritable.
     */
    public static class Comparator extends WritableComparator {
        public Comparator() {
            super(ByteWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            byte thisValue = b1[s1];
            byte thatValue = b2[s2];
            return (thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1));
        }
    }

    static {                                        // register this comparator
        WritableComparator.define(ByteWritable.class, new Comparator());
    }
}

