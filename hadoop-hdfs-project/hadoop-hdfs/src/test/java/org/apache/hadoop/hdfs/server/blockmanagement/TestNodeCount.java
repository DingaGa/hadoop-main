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
package org.apache.hadoop.hdfs.server.blockmanagement;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.MiniDFSCluster.DataNodeProperties;
import org.apache.hadoop.hdfs.protocol.Block;
import org.apache.hadoop.hdfs.protocol.ExtendedBlock;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;
import org.apache.hadoop.util.Time;
import org.junit.Test;

/**
 * Test if live nodes count per node is correct 
 * so NN makes right decision for under/over-replicated blocks
 *
 * Two of the "while" loops below use "busy wait"
 * because they are detecting transient states.
 */
public class TestNodeCount {
    final short REPLICATION_FACTOR = (short) 2;
    final long TIMEOUT = 20000L;
    long timeout = 0;
    long failtime = 0;
    Block lastBlock = null;
    NumberReplicas lastNum = null;

    @Test
    public void testNodeCount() throws Exception {
        // start a mini dfs cluster of 2 nodes
        final Configuration conf = new HdfsConfiguration();
        final MiniDFSCluster cluster =
                new MiniDFSCluster.Builder(conf).numDataNodes(REPLICATION_FACTOR).build();
        try {
            final FSNamesystem namesystem = cluster.getNamesystem();
            final BlockManager bm = namesystem.getBlockManager();
            final HeartbeatManager hm = bm.getDatanodeManager().getHeartbeatManager();
            final FileSystem fs = cluster.getFileSystem();

            // populate the cluster with a one block file
            final Path FILE_PATH = new Path("/testfile");
            DFSTestUtil.createFile(fs, FILE_PATH, 1L, REPLICATION_FACTOR, 1L);
            DFSTestUtil.waitReplication(fs, FILE_PATH, REPLICATION_FACTOR);
            ExtendedBlock block = DFSTestUtil.getFirstBlock(fs, FILE_PATH);

            // keep a copy of all datanode descriptor
            final DatanodeDescriptor[] datanodes = hm.getDatanodes();

            // start two new nodes
            cluster.startDataNodes(conf, 2, true, null, null);
            cluster.waitActive();

            // bring down first datanode
            DatanodeDescriptor datanode = datanodes[0];
            DataNodeProperties dnprop = cluster.stopDataNode(datanode.getXferAddr());

            // make sure that NN detects that the datanode is down
            BlockManagerTestUtil.noticeDeadDatanode(
                    cluster.getNameNode(), datanode.getXferAddr());

            // the block will be replicated
            DFSTestUtil.waitReplication(fs, FILE_PATH, REPLICATION_FACTOR);

            // restart the first datanode
            cluster.restartDataNode(dnprop);
            cluster.waitActive();

            // check if excessive replica is detected (transient)
            initializeTimeout(TIMEOUT);
            while (countNodes(block.getLocalBlock(), namesystem).excessReplicas() == 0) {
                checkTimeout("excess replicas not detected");
            }

            // find out a non-excess node
            DatanodeDescriptor nonExcessDN = null;
            for (DatanodeStorageInfo storage : bm.blocksMap.getStorages(block.getLocalBlock())) {
                final DatanodeDescriptor dn = storage.getDatanodeDescriptor();
                Collection<Block> blocks = bm.excessReplicateMap.get(dn.getDatanodeUuid());
                if (blocks == null || !blocks.contains(block.getLocalBlock())) {
                    nonExcessDN = dn;
                    break;
                }
            }
            assertTrue(nonExcessDN != null);

            // bring down non excessive datanode
            dnprop = cluster.stopDataNode(nonExcessDN.getXferAddr());
            // make sure that NN detects that the datanode is down
            BlockManagerTestUtil.noticeDeadDatanode(
                    cluster.getNameNode(), nonExcessDN.getXferAddr());

            // The block should be replicated
            initializeTimeout(TIMEOUT);
            while (countNodes(block.getLocalBlock(), namesystem).liveReplicas() != REPLICATION_FACTOR) {
                checkTimeout("live replica count not correct", 1000);
            }

            // restart the first datanode
            cluster.restartDataNode(dnprop);
            cluster.waitActive();

            // check if excessive replica is detected (transient)
            initializeTimeout(TIMEOUT);
            while (countNodes(block.getLocalBlock(), namesystem).excessReplicas() != 2) {
                checkTimeout("excess replica count not equal to 2");
            }

        } finally {
            cluster.shutdown();
        }
    }

    void initializeTimeout(long timeout) {
        this.timeout = timeout;
        this.failtime = Time.now()
                + ((timeout <= 0) ? Long.MAX_VALUE : timeout);
    }

    /* busy wait on transient conditions */
    void checkTimeout(String testLabel) throws TimeoutException {
        checkTimeout(testLabel, 0);
    }

    /* check for timeout, then wait for cycleTime msec */
    void checkTimeout(String testLabel, long cycleTime) throws TimeoutException {
        if (Time.now() > failtime) {
            throw new TimeoutException("Timeout: "
                    + testLabel + " for block " + lastBlock + " after " + timeout
                    + " msec.  Last counts: live = " + lastNum.liveReplicas()
                    + ", excess = " + lastNum.excessReplicas()
                    + ", corrupt = " + lastNum.corruptReplicas());
        }
        if (cycleTime > 0) {
            try {
                Thread.sleep(cycleTime);
            } catch (InterruptedException ie) {
                //ignore
            }
        }
    }

    /* threadsafe read of the replication counts for this block */
    NumberReplicas countNodes(Block block, FSNamesystem namesystem) {
        namesystem.readLock();
        try {
            lastBlock = block;
            lastNum = namesystem.getBlockManager().countNodes(block);
            return lastNum;
        } finally {
            namesystem.readUnlock();
        }
    }
}
