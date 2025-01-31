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

package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Input format that is a <code>CombineFileInputFormat</code>-equivalent for
 * <code>TextInputFormat</code>.
 *
 * @see CombineFileInputFormat
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class CombineTextInputFormat
        extends CombineFileInputFormat<LongWritable, Text> {
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
                                                               TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<LongWritable, Text>(
                (CombineFileSplit) split, context, TextRecordReaderWrapper.class);
    }

    /**
     * A record reader that may be passed to <code>CombineFileRecordReader</code>
     * so that it can be used in a <code>CombineFileInputFormat</code>-equivalent
     * for <code>TextInputFormat</code>.
     *
     * @see CombineFileRecordReader
     * @see CombineFileInputFormat
     * @see TextInputFormat
     */
    private static class TextRecordReaderWrapper
            extends CombineFileRecordReaderWrapper<LongWritable, Text> {
        // this constructor signature is required by CombineFileRecordReader
        public TextRecordReaderWrapper(CombineFileSplit split,
                                       TaskAttemptContext context, Integer idx)
                throws IOException, InterruptedException {
            super(new TextInputFormat(), split, context, idx);
        }
    }
}
