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

package org.apache.hadoop.mapred;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An {@link OutputFormat} that writes {@link SequenceFile}s. 
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class SequenceFileOutputFormat<K, V> extends FileOutputFormat<K, V> {

    public RecordWriter<K, V> getRecordWriter(
            FileSystem ignored, JobConf job,
            String name, Progressable progress)
            throws IOException {
        // get the path of the temporary output file
        Path file = FileOutputFormat.getTaskOutputPath(job, name);

        FileSystem fs = file.getFileSystem(job);
        CompressionCodec codec = null;
        CompressionType compressionType = CompressionType.NONE;
        if (getCompressOutput(job)) {
            // find the kind of compression to do
            compressionType = getOutputCompressionType(job);

            // find the right codec
            Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(job,
                    DefaultCodec.class);
            codec = ReflectionUtils.newInstance(codecClass, job);
        }
        final SequenceFile.Writer out =
                SequenceFile.createWriter(fs, job, file,
                        job.getOutputKeyClass(),
                        job.getOutputValueClass(),
                        compressionType,
                        codec,
                        progress);

        return new RecordWriter<K, V>() {

            public void write(K key, V value)
                    throws IOException {

                out.append(key, value);
            }

            public void close(Reporter reporter) throws IOException {
                out.close();
            }
        };
    }

    /** Open the output generated by this format. */
    public static SequenceFile.Reader[] getReaders(Configuration conf, Path dir)
            throws IOException {
        FileSystem fs = dir.getFileSystem(conf);
        Path[] names = FileUtil.stat2Paths(fs.listStatus(dir));

        // sort names, so that hash partitioning works
        Arrays.sort(names);

        SequenceFile.Reader[] parts = new SequenceFile.Reader[names.length];
        for (int i = 0; i < names.length; i++) {
            parts[i] = new SequenceFile.Reader(fs, names[i], conf);
        }
        return parts;
    }

    /**
     * Get the {@link CompressionType} for the output {@link SequenceFile}.
     * @param conf the {@link JobConf}
     * @return the {@link CompressionType} for the output {@link SequenceFile},
     *         defaulting to {@link CompressionType#RECORD}
     */
    public static CompressionType getOutputCompressionType(JobConf conf) {
        String val = conf.get(org.apache.hadoop.mapreduce.lib.output.
                FileOutputFormat.COMPRESS_TYPE, CompressionType.RECORD.toString());
        return CompressionType.valueOf(val);
    }

    /**
     * Set the {@link CompressionType} for the output {@link SequenceFile}.
     * @param conf the {@link JobConf} to modify
     * @param style the {@link CompressionType} for the output
     *              {@link SequenceFile}
     */
    public static void setOutputCompressionType(JobConf conf,
                                                CompressionType style) {
        setCompressOutput(conf, true);
        conf.set(org.apache.hadoop.mapreduce.lib.output.
                FileOutputFormat.COMPRESS_TYPE, style.toString());
    }

}

