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
package org.apache.hadoop.mapreduce.lib.fieldsel;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MapReduceTestUtil;

import junit.framework.TestCase;

import java.text.NumberFormat;

public class TestMRFieldSelection extends TestCase {

    private static NumberFormat idFormat = NumberFormat.getInstance();

    static {
        idFormat.setMinimumIntegerDigits(4);
        idFormat.setGroupingUsed(false);
    }

    public void testFieldSelection() throws Exception {
        launch();
    }

    private static Path testDir = new Path(
            System.getProperty("test.build.data", "/tmp"), "field");

    public static void launch() throws Exception {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        int numOfInputLines = 10;

        Path outDir = new Path(testDir, "output_for_field_selection_test");
        Path inDir = new Path(testDir, "input_for_field_selection_test");

        StringBuffer inputData = new StringBuffer();
        StringBuffer expectedOutput = new StringBuffer();
        constructInputOutputData(inputData, expectedOutput, numOfInputLines);

        conf.set(FieldSelectionHelper.DATA_FIELD_SEPERATOR, "-");
        conf.set(FieldSelectionHelper.MAP_OUTPUT_KEY_VALUE_SPEC, "6,5,1-3:0-");
        conf.set(
                FieldSelectionHelper.REDUCE_OUTPUT_KEY_VALUE_SPEC, ":4,3,2,1,0,0-");
        Job job = MapReduceTestUtil.createJob(conf, inDir, outDir,
                1, 1, inputData.toString());
        job.setMapperClass(FieldSelectionMapper.class);
        job.setReducerClass(FieldSelectionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        job.waitForCompletion(true);
        assertTrue("Job Failed!", job.isSuccessful());

        //
        // Finally, we compare the reconstructed answer key with the
        // original one.  Remember, we need to ignore zero-count items
        // in the original key.
        //
        String outdata = MapReduceTestUtil.readOutput(outDir, conf);
        assertEquals("Outputs doesnt match.", expectedOutput.toString(), outdata);
        fs.delete(outDir, true);
    }

    public static void constructInputOutputData(StringBuffer inputData,
                                                StringBuffer expectedOutput, int numOfInputLines) {
        for (int i = 0; i < numOfInputLines; i++) {
            inputData.append(idFormat.format(i));
            inputData.append("-").append(idFormat.format(i + 1));
            inputData.append("-").append(idFormat.format(i + 2));
            inputData.append("-").append(idFormat.format(i + 3));
            inputData.append("-").append(idFormat.format(i + 4));
            inputData.append("-").append(idFormat.format(i + 5));
            inputData.append("-").append(idFormat.format(i + 6));
            inputData.append("\n");

            expectedOutput.append(idFormat.format(i + 3));
            expectedOutput.append("-").append(idFormat.format(i + 2));
            expectedOutput.append("-").append(idFormat.format(i + 1));
            expectedOutput.append("-").append(idFormat.format(i + 5));
            expectedOutput.append("-").append(idFormat.format(i + 6));

            expectedOutput.append("-").append(idFormat.format(i + 6));
            expectedOutput.append("-").append(idFormat.format(i + 5));
            expectedOutput.append("-").append(idFormat.format(i + 1));
            expectedOutput.append("-").append(idFormat.format(i + 2));
            expectedOutput.append("-").append(idFormat.format(i + 3));
            expectedOutput.append("-").append(idFormat.format(i + 0));
            expectedOutput.append("-").append(idFormat.format(i + 1));
            expectedOutput.append("-").append(idFormat.format(i + 2));
            expectedOutput.append("-").append(idFormat.format(i + 3));
            expectedOutput.append("-").append(idFormat.format(i + 4));
            expectedOutput.append("-").append(idFormat.format(i + 5));
            expectedOutput.append("-").append(idFormat.format(i + 6));
            expectedOutput.append("\n");
        }
        System.out.println("inputData:");
        System.out.println(inputData.toString());
        System.out.println("ExpectedData:");
        System.out.println(expectedOutput.toString());
    }

    /**
     * Launches all the tasks in order.
     */
    public static void main(String[] argv) throws Exception {
        launch();
    }
}
