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

package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.PathIOException;

/**
 * Modifies the replication factor
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
class SetReplication extends FsCommand {
    public static void registerCommands(CommandFactory factory) {
        factory.addClass(SetReplication.class, "-setrep");
    }

    public static final String NAME = "setrep";
    public static final String USAGE = "[-R] [-w] <rep> <path> ...";
    public static final String DESCRIPTION =
            "Set the replication level of a file. If <path> is a directory " +
                    "then the command recursively changes the replication factor of " +
                    "all files under the directory tree rooted at <path>.\n" +
                    "-w: It requests that the command waits for the replication " +
                    "to complete. This can potentially take a very long time.\n" +
                    "-R: It is accepted for backwards compatibility. It has no effect.";

    protected short newRep = 0;
    protected List<PathData> waitList = new LinkedList<PathData>();
    protected boolean waitOpt = false;

    @Override
    protected void processOptions(LinkedList<String> args) throws IOException {
        CommandFormat cf = new CommandFormat(2, Integer.MAX_VALUE, "R", "w");
        cf.parse(args);
        waitOpt = cf.getOpt("w");
        setRecursive(true);

        try {
            newRep = Short.parseShort(args.removeFirst());
        } catch (NumberFormatException nfe) {
            displayWarning("Illegal replication, a positive integer expected");
            throw nfe;
        }
        if (newRep < 1) {
            throw new IllegalArgumentException("replication must be >= 1");
        }
    }

    @Override
    protected void processArguments(LinkedList<PathData> args)
            throws IOException {
        super.processArguments(args);
        if (waitOpt) waitForReplication();
    }

    @Override
    protected void processPath(PathData item) throws IOException {
        if (item.stat.isSymlink()) {
            throw new PathIOException(item.toString(), "Symlinks unsupported");
        }

        if (item.stat.isFile()) {
            if (!item.fs.setReplication(item.path, newRep)) {
                throw new IOException("Could not set replication for: " + item);
            }
            out.println("Replication " + newRep + " set: " + item);
            if (waitOpt) waitList.add(item);
        }
    }

    /**
     * Wait for all files in waitList to have replication number equal to rep.
     */
    private void waitForReplication() throws IOException {
        for (PathData item : waitList) {
            out.print("Waiting for " + item + " ...");
            out.flush();

            boolean printedWarning = false;
            boolean done = false;
            while (!done) {
                item.refreshStatus();
                BlockLocation[] locations =
                        item.fs.getFileBlockLocations(item.stat, 0, item.stat.getLen());

                int i = 0;
                for (; i < locations.length; i++) {
                    int currentRep = locations[i].getHosts().length;
                    if (currentRep != newRep) {
                        if (!printedWarning && currentRep > newRep) {
                            out.println("\nWARNING: the waiting time may be long for "
                                    + "DECREASING the number of replications.");
                            printedWarning = true;
                        }
                        break;
                    }
                }
                done = i == locations.length;
                if (done) break;

                out.print(".");
                out.flush();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
            out.println(" done");
        }
    }
}
