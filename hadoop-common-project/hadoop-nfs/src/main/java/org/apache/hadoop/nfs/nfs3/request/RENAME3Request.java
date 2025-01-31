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
package org.apache.hadoop.nfs.nfs3.request;

import java.io.IOException;

import org.apache.hadoop.nfs.nfs3.FileHandle;
import org.apache.hadoop.oncrpc.XDR;

/**
 * RENAME3 Request
 */
public class RENAME3Request {
    private final FileHandle fromDirHandle;
    private final String fromName;
    private final FileHandle toDirHandle;
    private final String toName;

    public RENAME3Request(XDR xdr) throws IOException {
        fromDirHandle = new FileHandle();
        if (!fromDirHandle.deserialize(xdr)) {
            throw new IOException("can't deserialize file handle");
        }
        fromName = xdr.readString();
        toDirHandle = new FileHandle();
        if (!toDirHandle.deserialize(xdr)) {
            throw new IOException("can't deserialize file handle");
        }
        toName = xdr.readString();
    }

    public FileHandle getFromDirHandle() {
        return fromDirHandle;
    }

    public String getFromName() {
        return fromName;
    }

    public FileHandle getToDirHandle() {
        return toDirHandle;
    }

    public String getToName() {
        return toName;
    }
}