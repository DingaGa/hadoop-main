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
package org.apache.hadoop.io.serializer;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Level;

public class TestSerializationFactory {

    static {
        ((Log4JLogger) SerializationFactory.LOG).getLogger().setLevel(Level.ALL);
    }

    static Configuration conf;
    static SerializationFactory factory;

    @BeforeClass
    public static void setup() throws Exception {
        conf = new Configuration();
        factory = new SerializationFactory(conf);
    }

    @Test
    public void testSerializationKeyIsEmpty() {
        Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeys.IO_SERIALIZATIONS_KEY, "");
        SerializationFactory factory = new SerializationFactory(conf);
    }

    @Test
    public void testSerializationKeyIsInvalid() {
        Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeys.IO_SERIALIZATIONS_KEY, "INVALID_KEY_XXX");
        SerializationFactory factory = new SerializationFactory(conf);
    }

    @Test
    public void testGetSerializer() {
        // Test that a valid serializer class is returned when its present
        assertNotNull("A valid class must be returned for default Writable SerDe",
                factory.getSerializer(Writable.class));
        // Test that a null is returned when none can be found.
        assertNull("A null should be returned if there are no serializers found.",
                factory.getSerializer(TestSerializationFactory.class));
    }

    @Test
    public void testGetDeserializer() {
        // Test that a valid serializer class is returned when its present
        assertNotNull("A valid class must be returned for default Writable SerDe",
                factory.getDeserializer(Writable.class));
        // Test that a null is returned when none can be found.
        assertNull("A null should be returned if there are no deserializers found",
                factory.getDeserializer(TestSerializationFactory.class));
    }
}
