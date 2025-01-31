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


import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileSystemTestHelper;
import org.apache.hadoop.fs.FsConstants;
import org.apache.hadoop.fs.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Test the ViewFsBaseTest using a viewfs with authority: 
 *    viewfs://mountTableName/
 *    ie the authority is used to load a mount table.
 *    The authority name used is "default"
 *
 */
public class TestViewFileSystemWithAuthorityLocalFileSystem extends ViewFileSystemBaseTest {
    URI schemeWithAuthority;

    @Override
    @Before
    public void setUp() throws Exception {
        // create the test root on local_fs
        fsTarget = FileSystem.getLocal(new Configuration());
        super.setUp(); // this sets up conf (and fcView which we replace)

        // Now create a viewfs using a mount table called "default"
        // hence viewfs://default/
        schemeWithAuthority =
                new URI(FsConstants.VIEWFS_SCHEME, "default", "/", null, null);
        fsView = FileSystem.get(schemeWithAuthority, conf);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        fsTarget.delete(fileSystemTestHelper.getTestRootPath(fsTarget), true);
        super.tearDown();
    }

    @Override
    @Test
    public void testBasicPaths() {
        Assert.assertEquals(schemeWithAuthority,
                fsView.getUri());
        Assert.assertEquals(fsView.makeQualified(
                        new Path("/user/" + System.getProperty("user.name"))),
                fsView.getWorkingDirectory());
        Assert.assertEquals(fsView.makeQualified(
                        new Path("/user/" + System.getProperty("user.name"))),
                fsView.getHomeDirectory());
        Assert.assertEquals(
                new Path("/foo/bar").makeQualified(schemeWithAuthority, null),
                fsView.makeQualified(new Path("/foo/bar")));
    }
}
