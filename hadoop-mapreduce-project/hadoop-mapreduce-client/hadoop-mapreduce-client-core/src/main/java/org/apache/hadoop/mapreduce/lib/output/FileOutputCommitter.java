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

package org.apache.hadoop.mapreduce.lib.output;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;

/** An {@link OutputCommitter} that commits files specified 
 * in job output directory i.e. ${mapreduce.output.fileoutputformat.outputdir}.
 **/
@InterfaceAudience.Public
@InterfaceStability.Stable
public class FileOutputCommitter extends OutputCommitter {
    private static final Log LOG = LogFactory.getLog(FileOutputCommitter.class);

    /**
     * Name of directory where pending data is placed.  Data that has not been
     * committed yet.
     */
    public static final String PENDING_DIR_NAME = "_temporary";
    /**
     * Temporary directory name
     *
     * The static variable to be compatible with M/R 1.x
     */
    @Deprecated
    protected static final String TEMP_DIR_NAME = PENDING_DIR_NAME;
    public static final String SUCCEEDED_FILE_NAME = "_SUCCESS";
    public static final String SUCCESSFUL_JOB_OUTPUT_DIR_MARKER =
            "mapreduce.fileoutputcommitter.marksuccessfuljobs";
    private Path outputPath = null;
    private Path workPath = null;

    /**
     * Create a file output committer
     * @param outputPath the job's output path, or null if you want the output
     * committer to act as a noop.
     * @param context the task's context
     * @throws IOException
     */
    public FileOutputCommitter(Path outputPath,
                               TaskAttemptContext context) throws IOException {
        this(outputPath, (JobContext) context);
        if (outputPath != null) {
            workPath = getTaskAttemptPath(context, outputPath);
        }
    }

    /**
     * Create a file output committer
     * @param outputPath the job's output path, or null if you want the output
     * committer to act as a noop.
     * @param context the task's context
     * @throws IOException
     */
    @Private
    public FileOutputCommitter(Path outputPath,
                               JobContext context) throws IOException {
        if (outputPath != null) {
            FileSystem fs = outputPath.getFileSystem(context.getConfiguration());
            this.outputPath = fs.makeQualified(outputPath);
        }
    }

    /**
     * @return the path where final output of the job should be placed.  This
     * could also be considered the committed application attempt path.
     */
    private Path getOutputPath() {
        return this.outputPath;
    }

    /**
     * @return true if we have an output path set, else false.
     */
    private boolean hasOutputPath() {
        return this.outputPath != null;
    }

    /**
     * @return the path where the output of pending job attempts are
     * stored.
     */
    private Path getPendingJobAttemptsPath() {
        return getPendingJobAttemptsPath(getOutputPath());
    }

    /**
     * Get the location of pending job attempts.
     * @param out the base output directory.
     * @return the location of pending job attempts.
     */
    private static Path getPendingJobAttemptsPath(Path out) {
        return new Path(out, PENDING_DIR_NAME);
    }

    /**
     * Get the Application Attempt Id for this job
     * @param context the context to look in
     * @return the Application Attempt Id for a given job.
     */
    private static int getAppAttemptId(JobContext context) {
        return context.getConfiguration().getInt(
                MRJobConfig.APPLICATION_ATTEMPT_ID, 0);
    }

    /**
     * Compute the path where the output of a given job attempt will be placed.
     * @param context the context of the job.  This is used to get the
     * application attempt id.
     * @return the path to store job attempt data.
     */
    public Path getJobAttemptPath(JobContext context) {
        return getJobAttemptPath(context, getOutputPath());
    }

    /**
     * Compute the path where the output of a given job attempt will be placed.
     * @param context the context of the job.  This is used to get the
     * application attempt id.
     * @param out the output path to place these in.
     * @return the path to store job attempt data.
     */
    public static Path getJobAttemptPath(JobContext context, Path out) {
        return getJobAttemptPath(getAppAttemptId(context), out);
    }

    /**
     * Compute the path where the output of a given job attempt will be placed.
     * @param appAttemptId the ID of the application attempt for this job.
     * @return the path to store job attempt data.
     */
    protected Path getJobAttemptPath(int appAttemptId) {
        return getJobAttemptPath(appAttemptId, getOutputPath());
    }

    /**
     * Compute the path where the output of a given job attempt will be placed.
     * @param appAttemptId the ID of the application attempt for this job.
     * @return the path to store job attempt data.
     */
    private static Path getJobAttemptPath(int appAttemptId, Path out) {
        return new Path(getPendingJobAttemptsPath(out), String.valueOf(appAttemptId));
    }

    /**
     * Compute the path where the output of pending task attempts are stored.
     * @param context the context of the job with pending tasks.
     * @return the path where the output of pending task attempts are stored.
     */
    private Path getPendingTaskAttemptsPath(JobContext context) {
        return getPendingTaskAttemptsPath(context, getOutputPath());
    }

    /**
     * Compute the path where the output of pending task attempts are stored.
     * @param context the context of the job with pending tasks.
     * @return the path where the output of pending task attempts are stored.
     */
    private static Path getPendingTaskAttemptsPath(JobContext context, Path out) {
        return new Path(getJobAttemptPath(context, out), PENDING_DIR_NAME);
    }

    /**
     * Compute the path where the output of a task attempt is stored until
     * that task is committed.
     *
     * @param context the context of the task attempt.
     * @return the path where a task attempt should be stored.
     */
    public Path getTaskAttemptPath(TaskAttemptContext context) {
        return new Path(getPendingTaskAttemptsPath(context),
                String.valueOf(context.getTaskAttemptID()));
    }

    /**
     * Compute the path where the output of a task attempt is stored until
     * that task is committed.
     *
     * @param context the context of the task attempt.
     * @param out The output path to put things in.
     * @return the path where a task attempt should be stored.
     */
    public static Path getTaskAttemptPath(TaskAttemptContext context, Path out) {
        return new Path(getPendingTaskAttemptsPath(context, out),
                String.valueOf(context.getTaskAttemptID()));
    }

    /**
     * Compute the path where the output of a committed task is stored until
     * the entire job is committed.
     * @param context the context of the task attempt
     * @return the path where the output of a committed task is stored until
     * the entire job is committed.
     */
    public Path getCommittedTaskPath(TaskAttemptContext context) {
        return getCommittedTaskPath(getAppAttemptId(context), context);
    }

    public static Path getCommittedTaskPath(TaskAttemptContext context, Path out) {
        return getCommittedTaskPath(getAppAttemptId(context), context, out);
    }

    /**
     * Compute the path where the output of a committed task is stored until the
     * entire job is committed for a specific application attempt.
     * @param appAttemptId the id of the application attempt to use
     * @param context the context of any task.
     * @return the path where the output of a committed task is stored.
     */
    protected Path getCommittedTaskPath(int appAttemptId, TaskAttemptContext context) {
        return new Path(getJobAttemptPath(appAttemptId),
                String.valueOf(context.getTaskAttemptID().getTaskID()));
    }

    private static Path getCommittedTaskPath(int appAttemptId, TaskAttemptContext context, Path out) {
        return new Path(getJobAttemptPath(appAttemptId, out),
                String.valueOf(context.getTaskAttemptID().getTaskID()));
    }

    private static class CommittedTaskFilter implements PathFilter {
        @Override
        public boolean accept(Path path) {
            return !PENDING_DIR_NAME.equals(path.getName());
        }
    }

    /**
     * Get a list of all paths where output from committed tasks are stored.
     * @param context the context of the current job
     * @return the list of these Paths/FileStatuses.
     * @throws IOException
     */
    private FileStatus[] getAllCommittedTaskPaths(JobContext context)
            throws IOException {
        Path jobAttemptPath = getJobAttemptPath(context);
        FileSystem fs = jobAttemptPath.getFileSystem(context.getConfiguration());
        return fs.listStatus(jobAttemptPath, new CommittedTaskFilter());
    }

    /**
     * Get the directory that the task should write results into.
     * @return the work directory
     * @throws IOException
     */
    public Path getWorkPath() throws IOException {
        return workPath;
    }

    /**
     * Create the temporary directory that is the root of all of the task
     * work directories.
     * @param context the job's context
     */
    public void setupJob(JobContext context) throws IOException {
        if (hasOutputPath()) {
            Path jobAttemptPath = getJobAttemptPath(context);
            FileSystem fs = jobAttemptPath.getFileSystem(
                    context.getConfiguration());
            if (!fs.mkdirs(jobAttemptPath)) {
                LOG.error("Mkdirs failed to create " + jobAttemptPath);
            }
        } else {
            LOG.warn("Output Path is null in setupJob()");
        }
    }

    /**
     * The job has completed so move all committed tasks to the final output dir.
     * Delete the temporary directory, including all of the work directories.
     * Create a _SUCCESS file to make it as successful.
     * @param context the job's context
     */
    public void commitJob(JobContext context) throws IOException {
        if (hasOutputPath()) {
            Path finalOutput = getOutputPath();
            FileSystem fs = finalOutput.getFileSystem(context.getConfiguration());
            for (FileStatus stat : getAllCommittedTaskPaths(context)) {
                mergePaths(fs, stat, finalOutput);
            }

            // delete the _temporary folder and create a _done file in the o/p folder
            cleanupJob(context);
            // True if the job requires output.dir marked on successful job.
            // Note that by default it is set to true.
            if (context.getConfiguration().getBoolean(SUCCESSFUL_JOB_OUTPUT_DIR_MARKER, true)) {
                Path markerPath = new Path(outputPath, SUCCEEDED_FILE_NAME);
                fs.create(markerPath).close();
            }
        } else {
            LOG.warn("Output Path is null in commitJob()");
        }
    }

    /**
     * Merge two paths together.  Anything in from will be moved into to, if there
     * are any name conflicts while merging the files or directories in from win.
     * @param fs the File System to use
     * @param from the path data is coming from.
     * @param to the path data is going to.
     * @throws IOException on any error
     */
    private static void mergePaths(FileSystem fs, final FileStatus from,
                                   final Path to)
            throws IOException {
        LOG.debug("Merging data from " + from + " to " + to);
        if (from.isFile()) {
            if (fs.exists(to)) {
                if (!fs.delete(to, true)) {
                    throw new IOException("Failed to delete " + to);
                }
            }

            if (!fs.rename(from.getPath(), to)) {
                throw new IOException("Failed to rename " + from + " to " + to);
            }
        } else if (from.isDirectory()) {
            if (fs.exists(to)) {
                FileStatus toStat = fs.getFileStatus(to);
                if (!toStat.isDirectory()) {
                    if (!fs.delete(to, true)) {
                        throw new IOException("Failed to delete " + to);
                    }
                    if (!fs.rename(from.getPath(), to)) {
                        throw new IOException("Failed to rename " + from + " to " + to);
                    }
                } else {
                    //It is a directory so merge everything in the directories
                    for (FileStatus subFrom : fs.listStatus(from.getPath())) {
                        Path subTo = new Path(to, subFrom.getPath().getName());
                        mergePaths(fs, subFrom, subTo);
                    }
                }
            } else {
                //it does not exist just rename
                if (!fs.rename(from.getPath(), to)) {
                    throw new IOException("Failed to rename " + from + " to " + to);
                }
            }
        }
    }

    @Override
    @Deprecated
    public void cleanupJob(JobContext context) throws IOException {
        if (hasOutputPath()) {
            Path pendingJobAttemptsPath = getPendingJobAttemptsPath();
            FileSystem fs = pendingJobAttemptsPath
                    .getFileSystem(context.getConfiguration());
            fs.delete(pendingJobAttemptsPath, true);
        } else {
            LOG.warn("Output Path is null in cleanupJob()");
        }
    }

    /**
     * Delete the temporary directory, including all of the work directories.
     * @param context the job's context
     */
    @Override
    public void abortJob(JobContext context, JobStatus.State state)
            throws IOException {
        // delete the _temporary folder
        cleanupJob(context);
    }

    /**
     * No task setup required.
     */
    @Override
    public void setupTask(TaskAttemptContext context) throws IOException {
        // FileOutputCommitter's setupTask doesn't do anything. Because the
        // temporary task directory is created on demand when the
        // task is writing.
    }

    /**
     * Move the files from the work directory to the job output directory
     * @param context the task context
     */
    @Override
    public void commitTask(TaskAttemptContext context)
            throws IOException {
        commitTask(context, null);
    }

    @Private
    public void commitTask(TaskAttemptContext context, Path taskAttemptPath)
            throws IOException {
        TaskAttemptID attemptId = context.getTaskAttemptID();
        if (hasOutputPath()) {
            context.progress();
            if (taskAttemptPath == null) {
                taskAttemptPath = getTaskAttemptPath(context);
            }
            Path committedTaskPath = getCommittedTaskPath(context);
            FileSystem fs = taskAttemptPath.getFileSystem(context.getConfiguration());
            if (fs.exists(taskAttemptPath)) {
                if (fs.exists(committedTaskPath)) {
                    if (!fs.delete(committedTaskPath, true)) {
                        throw new IOException("Could not delete " + committedTaskPath);
                    }
                }
                if (!fs.rename(taskAttemptPath, committedTaskPath)) {
                    throw new IOException("Could not rename " + taskAttemptPath + " to "
                            + committedTaskPath);
                }
                LOG.info("Saved output of task '" + attemptId + "' to " +
                        committedTaskPath);
            } else {
                LOG.warn("No Output found for " + attemptId);
            }
        } else {
            LOG.warn("Output Path is null in commitTask()");
        }
    }

    /**
     * Delete the work directory
     * @throws IOException
     */
    @Override
    public void abortTask(TaskAttemptContext context) throws IOException {
        abortTask(context, null);
    }

    @Private
    public void abortTask(TaskAttemptContext context, Path taskAttemptPath) throws IOException {
        if (hasOutputPath()) {
            context.progress();
            if (taskAttemptPath == null) {
                taskAttemptPath = getTaskAttemptPath(context);
            }
            FileSystem fs = taskAttemptPath.getFileSystem(context.getConfiguration());
            if (!fs.delete(taskAttemptPath, true)) {
                LOG.warn("Could not delete " + taskAttemptPath);
            }
        } else {
            LOG.warn("Output Path is null in abortTask()");
        }
    }

    /**
     * Did this task write any files in the work directory?
     * @param context the task's context
     */
    @Override
    public boolean needsTaskCommit(TaskAttemptContext context
    ) throws IOException {
        return needsTaskCommit(context, null);
    }

    @Private
    public boolean needsTaskCommit(TaskAttemptContext context, Path taskAttemptPath
    ) throws IOException {
        if (hasOutputPath()) {
            if (taskAttemptPath == null) {
                taskAttemptPath = getTaskAttemptPath(context);
            }
            FileSystem fs = taskAttemptPath.getFileSystem(context.getConfiguration());
            return fs.exists(taskAttemptPath);
        }
        return false;
    }

    @Override
    @Deprecated
    public boolean isRecoverySupported() {
        return true;
    }

    @Override
    public void recoverTask(TaskAttemptContext context)
            throws IOException {
        if (hasOutputPath()) {
            context.progress();
            TaskAttemptID attemptId = context.getTaskAttemptID();
            int previousAttempt = getAppAttemptId(context) - 1;
            if (previousAttempt < 0) {
                throw new IOException("Cannot recover task output for first attempt...");
            }

            Path committedTaskPath = getCommittedTaskPath(context);
            Path previousCommittedTaskPath = getCommittedTaskPath(
                    previousAttempt, context);
            FileSystem fs = committedTaskPath.getFileSystem(context.getConfiguration());

            LOG.debug("Trying to recover task from " + previousCommittedTaskPath
                    + " into " + committedTaskPath);
            if (fs.exists(previousCommittedTaskPath)) {
                if (fs.exists(committedTaskPath)) {
                    if (!fs.delete(committedTaskPath, true)) {
                        throw new IOException("Could not delete " + committedTaskPath);
                    }
                }
                //Rename can fail if the parent directory does not yet exist.
                Path committedParent = committedTaskPath.getParent();
                fs.mkdirs(committedParent);
                if (!fs.rename(previousCommittedTaskPath, committedTaskPath)) {
                    throw new IOException("Could not rename " + previousCommittedTaskPath +
                            " to " + committedTaskPath);
                }
                LOG.info("Saved output of " + attemptId + " to " + committedTaskPath);
            } else {
                LOG.warn(attemptId + " had no output to recover.");
            }
        } else {
            LOG.warn("Output Path is null in recoverTask()");
        }
    }
}
