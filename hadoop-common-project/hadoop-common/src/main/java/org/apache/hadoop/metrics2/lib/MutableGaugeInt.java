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

package org.apache.hadoop.metrics2.lib;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.metrics2.MetricsInfo;
import org.apache.hadoop.metrics2.MetricsRecordBuilder;

/**
 * A mutable int gauge
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class MutableGaugeInt extends MutableGauge {

    private volatile int value;

    MutableGaugeInt(MetricsInfo info, int initValue) {
        super(info);
        this.value = initValue;
    }

    public int value() {
        return value;
    }

    @Override
    public synchronized void incr() {
        ++value;
        setChanged();
    }

    /**
     * Increment by delta
     *
     * @param delta of the increment
     */
    public synchronized void incr(int delta) {
        value += delta;
        setChanged();
    }

    @Override
    public synchronized void decr() {
        --value;
        setChanged();
    }

    /**
     * decrement by delta
     *
     * @param delta of the decrement
     */
    public synchronized void decr(int delta) {
        value -= delta;
        setChanged();
    }

    /**
     * Set the value of the metric
     *
     * @param value to set
     */
    public void set(int value) {
        this.value = value;
        setChanged();
    }

    @Override
    public void snapshot(MetricsRecordBuilder builder, boolean all) {
        if (all || changed()) {
            builder.addGauge(info(), value);
            clearChanged();
        }
    }
}
