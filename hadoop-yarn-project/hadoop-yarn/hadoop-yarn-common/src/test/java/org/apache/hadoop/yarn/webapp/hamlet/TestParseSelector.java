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

package org.apache.hadoop.yarn.webapp.hamlet;

import org.junit.Test;

import static org.junit.Assert.*;

import org.apache.hadoop.yarn.webapp.WebAppException;

import static org.apache.hadoop.yarn.webapp.hamlet.HamletImpl.*;

public class TestParseSelector {

    @Test
    public void testNormal() {
        String[] res = parseSelector("#id.class");
        assertEquals("id", res[S_ID]);
        assertEquals("class", res[S_CLASS]);
    }

    @Test
    public void testMultiClass() {
        String[] res = parseSelector("#id.class1.class2");
        assertEquals("id", res[S_ID]);
        assertEquals("class1 class2", res[S_CLASS]);
    }

    @Test
    public void testMissingId() {
        String[] res = parseSelector(".class");
        assertNull(res[S_ID]);
        assertEquals("class", res[S_CLASS]);
    }

    @Test
    public void testMissingClass() {
        String[] res = parseSelector("#id");
        assertEquals("id", res[S_ID]);
        assertNull(res[S_CLASS]);
    }

    @Test(expected = WebAppException.class)
    public void testMissingAll() {
        parseSelector("");
    }
}
