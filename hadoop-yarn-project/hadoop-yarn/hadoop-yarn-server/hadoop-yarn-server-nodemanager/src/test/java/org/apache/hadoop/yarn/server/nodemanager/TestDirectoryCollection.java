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

package org.apache.hadoop.yarn.server.nodemanager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDirectoryCollection {

    private static final File testDir = new File("target",
            TestDirectoryCollection.class.getName()).getAbsoluteFile();
    private static final File testFile = new File(testDir, "testfile");

    @BeforeClass
    public static void setup() throws IOException {
        testDir.mkdirs();
        testFile.createNewFile();
    }

    @AfterClass
    public static void teardown() {
        FileUtil.fullyDelete(testDir);
    }

    @Test
    public void testConcurrentAccess() throws IOException {
        // Initialize DirectoryCollection with a file instead of a directory
        Configuration conf = new Configuration();
        String[] dirs = {testFile.getPath()};
        DirectoryCollection dc = new DirectoryCollection(dirs,
                conf.getFloat(YarnConfiguration.NM_MAX_PER_DISK_UTILIZATION_PERCENTAGE,
                        YarnConfiguration.DEFAULT_NM_MAX_PER_DISK_UTILIZATION_PERCENTAGE));

        // Create an iterator before checkDirs is called to reliable test case
        List<String> list = dc.getGoodDirs();
        ListIterator<String> li = list.listIterator();

        // DiskErrorException will invalidate iterator of non-concurrent
        // collections. ConcurrentModificationException will be thrown upon next
        // use of the iterator.
        Assert.assertTrue("checkDirs did not remove test file from directory list",
                dc.checkDirs());

        // Verify no ConcurrentModification is thrown
        li.next();
    }

    @Test
    public void testCreateDirectories() throws IOException {
        Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeys.FS_PERMISSIONS_UMASK_KEY, "077");
        FileContext localFs = FileContext.getLocalFSFileContext(conf);

        String dirA = new File(testDir, "dirA").getPath();
        String dirB = new File(dirA, "dirB").getPath();
        String dirC = new File(testDir, "dirC").getPath();
        Path pathC = new Path(dirC);
        FsPermission permDirC = new FsPermission((short) 0710);

        localFs.mkdir(pathC, null, true);
        localFs.setPermission(pathC, permDirC);

        String[] dirs = {dirA, dirB, dirC};
        DirectoryCollection dc = new DirectoryCollection(dirs,
                conf.getFloat(YarnConfiguration.NM_MAX_PER_DISK_UTILIZATION_PERCENTAGE,
                        YarnConfiguration.DEFAULT_NM_MAX_PER_DISK_UTILIZATION_PERCENTAGE));
        FsPermission defaultPerm = FsPermission.getDefault()
                .applyUMask(new FsPermission((short) FsPermission.DEFAULT_UMASK));
        boolean createResult = dc.createNonExistentDirs(localFs, defaultPerm);
        Assert.assertTrue(createResult);

        FileStatus status = localFs.getFileStatus(new Path(dirA));
        Assert.assertEquals("local dir parent not created with proper permissions",
                defaultPerm, status.getPermission());
        status = localFs.getFileStatus(new Path(dirB));
        Assert.assertEquals("local dir not created with proper permissions",
                defaultPerm, status.getPermission());
        status = localFs.getFileStatus(pathC);
        Assert.assertEquals("existing local directory permissions modified",
                permDirC, status.getPermission());
    }

    @Test
    public void testDiskSpaceUtilizationLimit() throws IOException {

        String dirA = new File(testDir, "dirA").getPath();
        String[] dirs = {dirA};
        DirectoryCollection dc = new DirectoryCollection(dirs, 0.0F);
        dc.checkDirs();
        Assert.assertEquals(0, dc.getGoodDirs().size());
        Assert.assertEquals(1, dc.getFailedDirs().size());

        dc = new DirectoryCollection(dirs, 100.0F);
        dc.checkDirs();
        Assert.assertEquals(1, dc.getGoodDirs().size());
        Assert.assertEquals(0, dc.getFailedDirs().size());

        dc = new DirectoryCollection(dirs, testDir.getTotalSpace() / (1024 * 1024));
        dc.checkDirs();
        Assert.assertEquals(0, dc.getGoodDirs().size());
        Assert.assertEquals(1, dc.getFailedDirs().size());

        dc = new DirectoryCollection(dirs, 100.0F, 0);
        dc.checkDirs();
        Assert.assertEquals(1, dc.getGoodDirs().size());
        Assert.assertEquals(0, dc.getFailedDirs().size());
    }

    @Test
    public void testDiskLimitsCutoffSetters() {

        String[] dirs = {"dir"};
        DirectoryCollection dc = new DirectoryCollection(dirs, 0.0F, 100);
        float testValue = 57.5F;
        float delta = 0.1F;
        dc.setDiskUtilizationPercentageCutoff(testValue);
        Assert.assertEquals(testValue, dc.getDiskUtilizationPercentageCutoff(),
                delta);
        testValue = -57.5F;
        dc.setDiskUtilizationPercentageCutoff(testValue);
        Assert.assertEquals(0.0F, dc.getDiskUtilizationPercentageCutoff(), delta);
        testValue = 157.5F;
        dc.setDiskUtilizationPercentageCutoff(testValue);
        Assert.assertEquals(100.0F, dc.getDiskUtilizationPercentageCutoff(), delta);

        long spaceValue = 57;
        dc.setDiskUtilizationSpaceCutoff(spaceValue);
        Assert.assertEquals(spaceValue, dc.getDiskUtilizationSpaceCutoff());
        spaceValue = -57;
        dc.setDiskUtilizationSpaceCutoff(spaceValue);
        Assert.assertEquals(0, dc.getDiskUtilizationSpaceCutoff());
    }

    @Test
    public void testConstructors() {

        String[] dirs = {"dir"};
        float delta = 0.1F;
        DirectoryCollection dc = new DirectoryCollection(dirs);
        Assert.assertEquals(100.0F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(0, dc.getDiskUtilizationSpaceCutoff());

        dc = new DirectoryCollection(dirs, 57.5F);
        Assert.assertEquals(57.5F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(0, dc.getDiskUtilizationSpaceCutoff());

        dc = new DirectoryCollection(dirs, 57);
        Assert.assertEquals(100.0F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(57, dc.getDiskUtilizationSpaceCutoff());

        dc = new DirectoryCollection(dirs, 57.5F, 67);
        Assert.assertEquals(57.5F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(67, dc.getDiskUtilizationSpaceCutoff());

        dc = new DirectoryCollection(dirs, -57.5F, -67);
        Assert.assertEquals(0.0F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(0, dc.getDiskUtilizationSpaceCutoff());

        dc = new DirectoryCollection(dirs, 157.5F, -67);
        Assert.assertEquals(100.0F, dc.getDiskUtilizationPercentageCutoff(), delta);
        Assert.assertEquals(0, dc.getDiskUtilizationSpaceCutoff());
    }
}
