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
package org.apache.hadoop.fs.viewfs;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;


/**
 * This class is needed to address the  problem described in
 * {@link ViewFileSystem#getFileStatus(org.apache.hadoop.fs.Path)} and
 * {@link ViewFs#getFileStatus(org.apache.hadoop.fs.Path)}
 */
class ViewFsFileStatus extends FileStatus {
    final FileStatus myFs;
    Path modifiedPath;

    ViewFsFileStatus(FileStatus fs, Path newPath) {
        myFs = fs;
        modifiedPath = newPath;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public long getLen() {
        return myFs.getLen();
    }

    @Override
    public boolean isFile() {
        return myFs.isFile();
    }

    @Override
    public boolean isDirectory() {
        return myFs.isDirectory();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isDir() {
        return myFs.isDirectory();
    }

    @Override
    public boolean isSymlink() {
        return myFs.isSymlink();
    }

    @Override
    public long getBlockSize() {
        return myFs.getBlockSize();
    }

    @Override
    public short getReplication() {
        return myFs.getReplication();
    }

    @Override
    public long getModificationTime() {
        return myFs.getModificationTime();
    }

    @Override
    public long getAccessTime() {
        return myFs.getAccessTime();
    }

    @Override
    public FsPermission getPermission() {
        return myFs.getPermission();
    }

    @Override
    public String getOwner() {
        return myFs.getOwner();
    }

    @Override
    public String getGroup() {
        return myFs.getGroup();
    }

    @Override
    public Path getPath() {
        return modifiedPath;
    }

    @Override
    public void setPath(final Path p) {
        modifiedPath = p;
    }

    @Override
    public Path getSymlink() throws IOException {
        return myFs.getSymlink();
    }
}

