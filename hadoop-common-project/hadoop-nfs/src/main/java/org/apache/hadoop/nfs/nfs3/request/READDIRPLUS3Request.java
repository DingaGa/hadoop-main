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

import org.apache.hadoop.oncrpc.XDR;

/**
 * READDIRPLUS3 Request
 */
public class READDIRPLUS3Request extends RequestWithHandle {
    private final long cookie;
    private final long cookieVerf;
    private final int dirCount;
    private final int maxCount;

    public READDIRPLUS3Request(XDR xdr) throws IOException {
        super(xdr);
        cookie = xdr.readHyper();
        cookieVerf = xdr.readHyper();
        dirCount = xdr.readInt();
        maxCount = xdr.readInt();
    }

    public long getCookie() {
        return this.cookie;
    }

    public long getCookieVerf() {
        return this.cookieVerf;
    }

    public int getDirCount() {
        return dirCount;
    }

    public int getMaxCount() {
        return maxCount;
    }
}