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
import org.apache.hadoop.nfs.nfs3.Nfs3Constant;
import org.apache.hadoop.oncrpc.XDR;

/**
 * CREATE3 Request
 */
public class CREATE3Request extends RequestWithHandle {
    private final String name;
    private final int mode;
    private SetAttr3 objAttr = null;
    private long verf;

    public CREATE3Request(FileHandle handle, String name, int mode,
                          SetAttr3 objAttr, long verf) {
        super(handle);
        this.name = name;
        this.mode = mode;
        this.objAttr = objAttr;
        this.verf = verf;
    }

    public CREATE3Request(XDR xdr) throws IOException {
        super(xdr);
        name = xdr.readString();
        mode = xdr.readInt();

        objAttr = new SetAttr3();
        if ((mode == Nfs3Constant.CREATE_UNCHECKED)
                || (mode == Nfs3Constant.CREATE_GUARDED)) {
            objAttr.deserialize(xdr);
        } else if (mode == Nfs3Constant.CREATE_EXCLUSIVE) {
            verf = xdr.readHyper();
        } else {
            throw new IOException("Wrong create mode:" + mode);
        }
    }

    public String getName() {
        return name;
    }

    public int getMode() {
        return mode;
    }

    public SetAttr3 getObjAttr() {
        return objAttr;
    }

    public long getVerf() {
        return verf;
    }

    @Override
    public void serialize(XDR xdr) {
        handle.serialize(xdr);
        xdr.writeInt(name.length());
        xdr.writeFixedOpaque(name.getBytes(), name.length());
        xdr.writeInt(mode);
        objAttr.serialize(xdr);
    }
}