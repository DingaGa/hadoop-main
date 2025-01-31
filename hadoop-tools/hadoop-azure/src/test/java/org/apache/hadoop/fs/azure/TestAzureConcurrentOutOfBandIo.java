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

package org.apache.hadoop.fs.azure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.fs.permission.PermissionStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAzureConcurrentOutOfBandIo {

    // Class constants.
    static final int DOWNLOAD_BLOCK_SIZE = 8 * 1024 * 1024;
    static final int UPLOAD_BLOCK_SIZE = 4 * 1024 * 1024;
    static final int BLOB_SIZE = 32 * 1024 * 1024;

    // Number of blocks to be written before flush.
    private static final int NUMBER_OF_BLOCKS = 2;

    private AzureBlobStorageTestAccount testAccount;

    // Overridden TestCase methods.
    @Before
    public void setUp() throws Exception {
        testAccount = AzureBlobStorageTestAccount.createOutOfBandStore(
                UPLOAD_BLOCK_SIZE, DOWNLOAD_BLOCK_SIZE);
        assumeNotNull(testAccount);
    }

    @After
    public void tearDown() throws Exception {
        if (testAccount != null) {
            testAccount.cleanup();
            testAccount = null;
        }
    }

    class DataBlockWriter implements Runnable {

        Thread runner;
        AzureBlobStorageTestAccount writerStorageAccount;
        String key;
        boolean done = false;

        /**
         * Constructor captures the test account.
         *
         * @param testAccount
         */
        public DataBlockWriter(AzureBlobStorageTestAccount testAccount, String key) {
            writerStorageAccount = testAccount;
            this.key = key;
        }

        /**
         * Start writing blocks to Azure storage.
         */
        public void startWriting() {
            runner = new Thread(this); // Create the block writer thread.
            runner.start(); // Start the block writer thread.
        }

        /**
         * Stop writing blocks to Azure storage.
         */
        public void stopWriting() {
            done = true;
        }

        /**
         * Implementation of the runnable interface. The run method is a tight loop
         * which repeatedly updates the blob with a 4 MB block.
         */
        public void run() {
            byte[] dataBlockWrite = new byte[UPLOAD_BLOCK_SIZE];

            DataOutputStream outputStream = null;

            try {
                for (int i = 0; !done; i++) {
                    // Write two 4 MB blocks to the blob.
                    //
                    outputStream = writerStorageAccount.getStore().storefile(key,
                            new PermissionStatus("", "", FsPermission.getDefault()));

                    Arrays.fill(dataBlockWrite, (byte) (i % 256));
                    for (int j = 0; j < NUMBER_OF_BLOCKS; j++) {
                        outputStream.write(dataBlockWrite);
                    }

                    outputStream.flush();
                    outputStream.close();
                }
            } catch (AzureException e) {
                System.out
                        .println("DatablockWriter thread encountered a storage exception."
                                + e.getMessage());
            } catch (IOException e) {
                System.out
                        .println("DatablockWriter thread encountered an I/O exception."
                                + e.getMessage());
            }
        }
    }

    @Test
    public void testReadOOBWrites() throws Exception {

        byte[] dataBlockWrite = new byte[UPLOAD_BLOCK_SIZE];
        byte[] dataBlockRead = new byte[UPLOAD_BLOCK_SIZE];

        // Write to blob to make sure it exists.
        //
        // Write five 4 MB blocks to the blob. To ensure there is data in the blob
        // before reading. This eliminates the race between the reader and writer
        // threads.
        DataOutputStream outputStream = testAccount.getStore().storefile(
                "WASB_String.txt",
                new PermissionStatus("", "", FsPermission.getDefault()));
        Arrays.fill(dataBlockWrite, (byte) 255);
        for (int i = 0; i < NUMBER_OF_BLOCKS; i++) {
            outputStream.write(dataBlockWrite);
        }

        outputStream.flush();
        outputStream.close();

        // Start writing blocks to Azure store using the DataBlockWriter thread.
        DataBlockWriter writeBlockTask = new DataBlockWriter(testAccount,
                "WASB_String.txt");
        writeBlockTask.startWriting();
        int count = 0;
        DataInputStream inputStream = null;

        for (int i = 0; i < 5; i++) {
            try {
                inputStream = testAccount.getStore().retrieve("WASB_String.txt", 0);
                count = 0;
                int c = 0;

                while (c >= 0) {
                    c = inputStream.read(dataBlockRead, 0, UPLOAD_BLOCK_SIZE);
                    if (c < 0) {
                        break;
                    }

                    // Counting the number of bytes.
                    count += c;
                }
            } catch (IOException e) {
                System.out.println(e.getCause().toString());
                e.printStackTrace();
                fail();
            }

            // Close the stream.
            if (null != inputStream) {
                inputStream.close();
            }
        }

        // Stop writing blocks.
        writeBlockTask.stopWriting();

        // Validate that a block was read.
        assertEquals(NUMBER_OF_BLOCKS * UPLOAD_BLOCK_SIZE, count);
    }
}