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
package org.apache.hadoop.fs.permission;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * Specifies the scope or intended usage of an ACL entry.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public enum AclEntryScope {
    /**
     * An ACL entry that is inspected during permission checks to enforce
     * permissions.
     */
    ACCESS,

    /**
     * An ACL entry to be applied to a directory's children that do not otherwise
     * have their own ACL defined.  Unlike an access ACL entry, a default ACL
     * entry is not inspected as part of permission enforcement on the directory
     * that owns it.
     */
    DEFAULT;
}
