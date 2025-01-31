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
package org.apache.hadoop.util;

import junit.framework.TestCase;

import org.apache.hadoop.util.ExitUtil.ExitException;
import org.junit.Test;

public class TestNativeLibraryChecker extends TestCase {
    private void expectExit(String[] args) {
        try {
            // should throw exit exception
            NativeLibraryChecker.main(args);
            fail("should call exit");
        } catch (ExitException e) {
            // pass
            ExitUtil.resetFirstExitException();
        }
    }

    @Test
    public void testNativeLibraryChecker() {
        ExitUtil.disableSystemExit();
        // help should return normally
        NativeLibraryChecker.main(new String[]{"-h"});
        // illegal argmuments should exit
        expectExit(new String[]{"-a", "-h"});
        expectExit(new String[]{"aaa"});
        if (NativeCodeLoader.isNativeCodeLoaded()) {
            // no argument should return normally
            NativeLibraryChecker.main(new String[0]);
        } else {
            // no argument should exit
            expectExit(new String[0]);
        }
    }

}
