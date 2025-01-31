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
package org.apache.hadoop.fs;

/**
 * Exception corresponding to File Exists - EEXISTS
 */
public class PathExistsException extends PathIOException {
    static final long serialVersionUID = 0L;

    /**
     * @param path for the exception
     */
    public PathExistsException(String path) {
        super(path, "File exists");
    }

    protected PathExistsException(String path, String error) {
        super(path, error);
    }
}