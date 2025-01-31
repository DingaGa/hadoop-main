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
package org.apache.hadoop.nfs.nfs3;

import org.junit.Assert;

import org.apache.hadoop.nfs.nfs3.FileHandle;
import org.apache.hadoop.oncrpc.XDR;
import org.junit.Test;

public class TestFileHandle {
    @Test
    public void testConstructor() {
        FileHandle handle = new FileHandle(1024);
        XDR xdr = new XDR();
        handle.serialize(xdr);
        Assert.assertEquals(handle.getFileId(), 1024);

        // Deserialize it back
        FileHandle handle2 = new FileHandle();
        handle2.deserialize(xdr.asReadOnlyWrap());
        Assert.assertEquals("Failed: Assert 1024 is id ", 1024,
                handle.getFileId());
    }
}
