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
package org.apache.hadoop.mapreduce.v2.hs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.v2.api.MRDelegationTokenIdentifier;
import org.apache.hadoop.security.token.delegation.DelegationKey;

/**
 * A state store backed by memory for unit tests
 */
class HistoryServerMemStateStoreService
        extends HistoryServerStateStoreService {

    HistoryServerState state;

    @Override
    protected void initStorage(Configuration conf) throws IOException {
    }

    @Override
    protected void startStorage() throws IOException {
        state = new HistoryServerState();
    }

    @Override
    protected void closeStorage() throws IOException {
        state = null;
    }

    @Override
    public HistoryServerState loadState() throws IOException {
        HistoryServerState result = new HistoryServerState();
        result.tokenState.putAll(state.tokenState);
        result.tokenMasterKeyState.addAll(state.tokenMasterKeyState);
        return result;
    }

    @Override
    public void storeToken(MRDelegationTokenIdentifier tokenId, Long renewDate)
            throws IOException {
        if (state.tokenState.containsKey(tokenId)) {
            throw new IOException("token " + tokenId + " was stored twice");
        }
        state.tokenState.put(tokenId, renewDate);
    }

    @Override
    public void updateToken(MRDelegationTokenIdentifier tokenId, Long renewDate)
            throws IOException {
        if (!state.tokenState.containsKey(tokenId)) {
            throw new IOException("token " + tokenId + " not in store");
        }
        state.tokenState.put(tokenId, renewDate);
    }

    @Override
    public void removeToken(MRDelegationTokenIdentifier tokenId)
            throws IOException {
        state.tokenState.remove(tokenId);
    }

    @Override
    public void storeTokenMasterKey(DelegationKey key) throws IOException {
        if (state.tokenMasterKeyState.contains(key)) {
            throw new IOException("token master key " + key + " was stored twice");
        }
        state.tokenMasterKeyState.add(key);
    }

    @Override
    public void removeTokenMasterKey(DelegationKey key) throws IOException {
        state.tokenMasterKeyState.remove(key);
    }
}
