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

package org.apache.hadoop.yarn.security.client;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;
import org.apache.hadoop.security.token.TokenSelector;

@Public
@Stable
public class RMDelegationTokenSelector implements
        TokenSelector<RMDelegationTokenIdentifier> {

    private static final Log LOG = LogFactory
            .getLog(RMDelegationTokenSelector.class);

    private boolean checkService(Text service,
                                 Token<? extends TokenIdentifier> token) {
        if (service == null || token.getService() == null) {
            return false;
        }
        return token.getService().toString().contains(service.toString());
    }

    @SuppressWarnings("unchecked")
    public Token<RMDelegationTokenIdentifier> selectToken(Text service,
                                                          Collection<Token<? extends TokenIdentifier>> tokens) {
        if (service == null) {
            return null;
        }
        LOG.debug("Looking for a token with service " + service.toString());
        for (Token<? extends TokenIdentifier> token : tokens) {
            LOG.debug("Token kind is " + token.getKind().toString()
                    + " and the token's service name is " + token.getService());
            if (RMDelegationTokenIdentifier.KIND_NAME.equals(token.getKind())
                    && checkService(service, token)) {
                return (Token<RMDelegationTokenIdentifier>) token;
            }
        }
        return null;
    }

}
