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
package org.apache.hadoop.yarn.client.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationAttemptState;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.util.Records;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.log.Log;

import org.apache.commons.cli.Options;

public class TestYarnCLI {

    private YarnClient client = mock(YarnClient.class);
    ByteArrayOutputStream sysOutStream;
    private PrintStream sysOut;
    ByteArrayOutputStream sysErrStream;
    private PrintStream sysErr;

    @Before
    public void setup() {
        sysOutStream = new ByteArrayOutputStream();
        sysOut = spy(new PrintStream(sysOutStream));
        sysErrStream = new ByteArrayOutputStream();
        sysErr = spy(new PrintStream(sysErrStream));
        System.setOut(sysOut);
    }

    @Test
    public void testGetApplicationReport() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationReport newApplicationReport = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.FINISHED, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        when(client.getApplicationReport(any(ApplicationId.class))).thenReturn(
                newApplicationReport);
        int result = cli.run(new String[]{"application", "-status", applicationId.toString()});
        assertEquals(0, result);
        verify(client).getApplicationReport(applicationId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Application Report : ");
        pw.println("\tApplication-Id : application_1234_0005");
        pw.println("\tApplication-Name : appname");
        pw.println("\tApplication-Type : YARN");
        pw.println("\tUser : user");
        pw.println("\tQueue : queue");
        pw.println("\tStart-Time : 0");
        pw.println("\tFinish-Time : 0");
        pw.println("\tProgress : 53.79%");
        pw.println("\tState : FINISHED");
        pw.println("\tFinal-State : SUCCEEDED");
        pw.println("\tTracking-URL : N/A");
        pw.println("\tRPC Port : 124");
        pw.println("\tAM Host : host");
        pw.println("\tDiagnostics : diagnostics");
        pw.close();
        String appReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appReportStr, sysOutStream.toString());
        verify(sysOut, times(1)).println(isA(String.class));
    }

    @Test
    public void testGetApplicationAttemptReport() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(
                applicationId, 1);
        ApplicationAttemptReport attemptReport = ApplicationAttemptReport
                .newInstance(attemptId, "host", 124, "url", "diagnostics",
                        YarnApplicationAttemptState.FINISHED, ContainerId.newInstance(
                                attemptId, 1));
        when(
                client
                        .getApplicationAttemptReport(any(ApplicationAttemptId.class)))
                .thenReturn(attemptReport);
        int result = cli.run(new String[]{"applicationattempt", "-status",
                attemptId.toString()});
        assertEquals(0, result);
        verify(client).getApplicationAttemptReport(attemptId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Application Attempt Report : ");
        pw.println("\tApplicationAttempt-Id : appattempt_1234_0005_000001");
        pw.println("\tState : FINISHED");
        pw.println("\tAMContainer : container_1234_0005_01_000001");
        pw.println("\tTracking-URL : url");
        pw.println("\tRPC Port : 124");
        pw.println("\tAM Host : host");
        pw.println("\tDiagnostics : diagnostics");
        pw.close();
        String appReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appReportStr, sysOutStream.toString());
        verify(sysOut, times(1)).println(isA(String.class));
    }

    @Test
    public void testGetApplicationAttempts() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(
                applicationId, 1);
        ApplicationAttemptId attemptId1 = ApplicationAttemptId.newInstance(
                applicationId, 2);
        ApplicationAttemptReport attemptReport = ApplicationAttemptReport
                .newInstance(attemptId, "host", 124, "url", "diagnostics",
                        YarnApplicationAttemptState.FINISHED, ContainerId.newInstance(
                                attemptId, 1));
        ApplicationAttemptReport attemptReport1 = ApplicationAttemptReport
                .newInstance(attemptId1, "host", 124, "url", "diagnostics",
                        YarnApplicationAttemptState.FINISHED, ContainerId.newInstance(
                                attemptId1, 1));
        List<ApplicationAttemptReport> reports = new ArrayList<ApplicationAttemptReport>();
        reports.add(attemptReport);
        reports.add(attemptReport1);
        when(client.getApplicationAttempts(any(ApplicationId.class)))
                .thenReturn(reports);
        int result = cli.run(new String[]{"applicationattempt", "-list",
                applicationId.toString()});
        assertEquals(0, result);
        verify(client).getApplicationAttempts(applicationId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Total number of application attempts :2");
        pw.print("         ApplicationAttempt-Id");
        pw.print("\t               State");
        pw.print("\t                    AM-Container-Id");
        pw.println("\t                       Tracking-URL");
        pw.print("   appattempt_1234_0005_000001");
        pw.print("\t            FINISHED");
        pw.print("\t      container_1234_0005_01_000001");
        pw.println("\t                                url");
        pw.print("   appattempt_1234_0005_000002");
        pw.print("\t            FINISHED");
        pw.print("\t      container_1234_0005_02_000001");
        pw.println("\t                                url");
        pw.close();
        String appReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appReportStr, sysOutStream.toString());
    }

    @Test
    public void testGetContainerReport() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(
                applicationId, 1);
        ContainerId containerId = ContainerId.newInstance(attemptId, 1);
        ContainerReport container = ContainerReport.newInstance(containerId, null,
                NodeId.newInstance("host", 1234), Priority.UNDEFINED, 1234, 5678,
                "diagnosticInfo", "logURL", 0, ContainerState.COMPLETE);
        when(client.getContainerReport(any(ContainerId.class))).thenReturn(
                container);
        int result = cli.run(new String[]{"container", "-status",
                containerId.toString()});
        assertEquals(0, result);
        verify(client).getContainerReport(containerId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Container Report : ");
        pw.println("\tContainer-Id : container_1234_0005_01_000001");
        pw.println("\tStart-Time : 1234");
        pw.println("\tFinish-Time : 5678");
        pw.println("\tState : COMPLETE");
        pw.println("\tLOG-URL : logURL");
        pw.println("\tHost : host:1234");
        pw.println("\tDiagnostics : diagnosticInfo");
        pw.close();
        String appReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appReportStr, sysOutStream.toString());
        verify(sysOut, times(1)).println(isA(String.class));
    }

    @Test
    public void testGetContainers() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationAttemptId attemptId = ApplicationAttemptId.newInstance(
                applicationId, 1);
        ContainerId containerId = ContainerId.newInstance(attemptId, 1);
        ContainerId containerId1 = ContainerId.newInstance(attemptId, 2);
        ContainerReport container = ContainerReport.newInstance(containerId, null,
                NodeId.newInstance("host", 1234), Priority.UNDEFINED, 1234, 5678,
                "diagnosticInfo", "logURL", 0, ContainerState.COMPLETE);
        ContainerReport container1 = ContainerReport.newInstance(containerId1, null,
                NodeId.newInstance("host", 1234), Priority.UNDEFINED, 1234, 5678,
                "diagnosticInfo", "logURL", 0, ContainerState.COMPLETE);
        List<ContainerReport> reports = new ArrayList<ContainerReport>();
        reports.add(container);
        reports.add(container1);
        when(client.getContainers(any(ApplicationAttemptId.class))).thenReturn(
                reports);
        int result = cli.run(new String[]{"container", "-list",
                attemptId.toString()});
        assertEquals(0, result);
        verify(client).getContainers(attemptId);
        Log.info(sysOutStream.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Total number of containers :2");
        pw.print("                  Container-Id");
        pw.print("\t          Start Time");
        pw.print("\t         Finish Time");
        pw.print("\t               State");
        pw.print("\t                Host");
        pw.println("\t                            LOG-URL");
        pw.print(" container_1234_0005_01_000001");
        pw.print("\t                1234");
        pw.print("\t                5678");
        pw.print("\t            COMPLETE");
        pw.print("\t           host:1234");
        pw.println("\t                             logURL");
        pw.print(" container_1234_0005_01_000002");
        pw.print("\t                1234");
        pw.print("\t                5678");
        pw.print("\t            COMPLETE");
        pw.print("\t           host:1234");
        pw.println("\t                             logURL");
        pw.close();
        String appReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appReportStr, sysOutStream.toString());
    }

    @Test
    public void testGetApplicationReportException() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        when(client.getApplicationReport(any(ApplicationId.class))).thenThrow(
                new ApplicationNotFoundException("History file for application"
                        + applicationId + " is not found"));
        try {
            cli.run(new String[]{"application", "-status", applicationId.toString()});
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ApplicationNotFoundException);
            Assert.assertEquals("History file for application"
                    + applicationId + " is not found", ex.getMessage());
        }
    }

    @Test
    public void testGetApplications() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationReport newApplicationReport = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.RUNNING, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        List<ApplicationReport> applicationReports = new ArrayList<ApplicationReport>();
        applicationReports.add(newApplicationReport);

        ApplicationId applicationId2 = ApplicationId.newInstance(1234, 6);
        ApplicationReport newApplicationReport2 = ApplicationReport.newInstance(
                applicationId2, ApplicationAttemptId.newInstance(applicationId2, 2),
                "user2", "queue2", "appname2", "host2", 125, null,
                YarnApplicationState.FINISHED, "diagnostics2", "url2", 2, 2,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.63789f, "NON-YARN",
                null);
        applicationReports.add(newApplicationReport2);

        ApplicationId applicationId3 = ApplicationId.newInstance(1234, 7);
        ApplicationReport newApplicationReport3 = ApplicationReport.newInstance(
                applicationId3, ApplicationAttemptId.newInstance(applicationId3, 3),
                "user3", "queue3", "appname3", "host3", 126, null,
                YarnApplicationState.RUNNING, "diagnostics3", "url3", 3, 3,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.73789f, "MAPREDUCE",
                null);
        applicationReports.add(newApplicationReport3);

        ApplicationId applicationId4 = ApplicationId.newInstance(1234, 8);
        ApplicationReport newApplicationReport4 = ApplicationReport.newInstance(
                applicationId4, ApplicationAttemptId.newInstance(applicationId4, 4),
                "user4", "queue4", "appname4", "host4", 127, null,
                YarnApplicationState.FAILED, "diagnostics4", "url4", 4, 4,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.83789f, "NON-MAPREDUCE",
                null);
        applicationReports.add(newApplicationReport4);

        ApplicationId applicationId5 = ApplicationId.newInstance(1234, 9);
        ApplicationReport newApplicationReport5 = ApplicationReport.newInstance(
                applicationId5, ApplicationAttemptId.newInstance(applicationId5, 5),
                "user5", "queue5", "appname5", "host5", 128, null,
                YarnApplicationState.ACCEPTED, "diagnostics5", "url5", 5, 5,
                FinalApplicationStatus.KILLED, null, "N/A", 0.93789f, "HIVE",
                null);
        applicationReports.add(newApplicationReport5);

        ApplicationId applicationId6 = ApplicationId.newInstance(1234, 10);
        ApplicationReport newApplicationReport6 = ApplicationReport.newInstance(
                applicationId6, ApplicationAttemptId.newInstance(applicationId6, 6),
                "user6", "queue6", "appname6", "host6", 129, null,
                YarnApplicationState.SUBMITTED, "diagnostics6", "url6", 6, 6,
                FinalApplicationStatus.KILLED, null, "N/A", 0.99789f, "PIG",
                null);
        applicationReports.add(newApplicationReport6);

        // Test command yarn application -list
        // if the set appStates is empty, RUNNING state will be automatically added
        // to the appStates list
        // the output of yarn application -list should be the same as
        // equals to yarn application -list --appStates RUNNING,ACCEPTED,SUBMITTED
        Set<String> appType1 = new HashSet<String>();
        EnumSet<YarnApplicationState> appState1 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState1.add(YarnApplicationState.RUNNING);
        appState1.add(YarnApplicationState.ACCEPTED);
        appState1.add(YarnApplicationState.SUBMITTED);
        when(client.getApplications(appType1, appState1)).thenReturn(
                getApplicationReports(applicationReports, appType1, appState1, false));
        int result = cli.run(new String[]{"application", "-list"});
        assertEquals(0, result);
        verify(client).getApplications(appType1, appState1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType1
                + " and states: " + appState1 + ")" + ":" + 4);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0005\t             ");
        pw.print("appname\t                YARN\t      user\t     ");
        pw.print("queue\t           RUNNING\t         ");
        pw.print("SUCCEEDED\t         53.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0007\t            ");
        pw.print("appname3\t           MAPREDUCE\t     user3\t    ");
        pw.print("queue3\t           RUNNING\t         ");
        pw.print("SUCCEEDED\t         73.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0009\t            ");
        pw.print("appname5\t                HIVE\t     user5\t    ");
        pw.print("queue5\t          ACCEPTED\t            ");
        pw.print("KILLED\t         93.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0010\t            ");
        pw.print("appname6\t                 PIG\t     user6\t    ");
        pw.print("queue6\t         SUBMITTED\t            ");
        pw.print("KILLED\t         99.79%");
        pw.println("\t                                N/A");
        pw.close();
        String appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(1)).write(any(byte[].class), anyInt(), anyInt());

        //Test command yarn application -list --appTypes apptype1,apptype2
        //the output should be the same as
        // yarn application -list --appTypes apptyp1, apptype2 --appStates
        // RUNNING,ACCEPTED,SUBMITTED
        sysOutStream.reset();
        Set<String> appType2 = new HashSet<String>();
        appType2.add("YARN");
        appType2.add("NON-YARN");

        EnumSet<YarnApplicationState> appState2 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState2.add(YarnApplicationState.RUNNING);
        appState2.add(YarnApplicationState.ACCEPTED);
        appState2.add(YarnApplicationState.SUBMITTED);
        when(client.getApplications(appType2, appState2)).thenReturn(
                getApplicationReports(applicationReports, appType2, appState2, false));
        result =
                cli.run(new String[]{"application", "-list", "-appTypes",
                        "YARN, ,,  NON-YARN", "   ,, ,,"});
        assertEquals(0, result);
        verify(client).getApplications(appType2, appState2);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType2
                + " and states: " + appState2 + ")" + ":" + 1);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0005\t             ");
        pw.print("appname\t                YARN\t      user\t     ");
        pw.print("queue\t           RUNNING\t         ");
        pw.print("SUCCEEDED\t         53.79%");
        pw.println("\t                                N/A");
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(2)).write(any(byte[].class), anyInt(), anyInt());

        //Test command yarn application -list --appStates appState1,appState2
        sysOutStream.reset();
        Set<String> appType3 = new HashSet<String>();

        EnumSet<YarnApplicationState> appState3 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState3.add(YarnApplicationState.FINISHED);
        appState3.add(YarnApplicationState.FAILED);

        when(client.getApplications(appType3, appState3)).thenReturn(
                getApplicationReports(applicationReports, appType3, appState3, false));
        result =
                cli.run(new String[]{"application", "-list", "--appStates",
                        "FINISHED ,, , FAILED", ",,FINISHED"});
        assertEquals(0, result);
        verify(client).getApplications(appType3, appState3);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType3
                + " and states: " + appState3 + ")" + ":" + 2);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0006\t            ");
        pw.print("appname2\t            NON-YARN\t     user2\t    ");
        pw.print("queue2\t          FINISHED\t         ");
        pw.print("SUCCEEDED\t         63.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0008\t            ");
        pw.print("appname4\t       NON-MAPREDUCE\t     user4\t    ");
        pw.print("queue4\t            FAILED\t         ");
        pw.print("SUCCEEDED\t         83.79%");
        pw.println("\t                                N/A");
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(3)).write(any(byte[].class), anyInt(), anyInt());

        // Test command yarn application -list --appTypes apptype1,apptype2
        // --appStates appstate1,appstate2
        sysOutStream.reset();
        Set<String> appType4 = new HashSet<String>();
        appType4.add("YARN");
        appType4.add("NON-YARN");

        EnumSet<YarnApplicationState> appState4 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState4.add(YarnApplicationState.FINISHED);
        appState4.add(YarnApplicationState.FAILED);

        when(client.getApplications(appType4, appState4)).thenReturn(
                getApplicationReports(applicationReports, appType4, appState4, false));
        result =
                cli.run(new String[]{"application", "-list", "--appTypes",
                        "YARN,NON-YARN", "--appStates", "FINISHED ,, , FAILED"});
        assertEquals(0, result);
        verify(client).getApplications(appType2, appState2);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType4
                + " and states: " + appState4 + ")" + ":" + 1);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0006\t            ");
        pw.print("appname2\t            NON-YARN\t     user2\t    ");
        pw.print("queue2\t          FINISHED\t         ");
        pw.print("SUCCEEDED\t         63.79%");
        pw.println("\t                                N/A");
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(4)).write(any(byte[].class), anyInt(), anyInt());

        //Test command yarn application -list --appStates with invalid appStates
        sysOutStream.reset();
        result =
                cli.run(new String[]{"application", "-list", "--appStates",
                        "FINISHED ,, , INVALID"});
        assertEquals(-1, result);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("The application state  INVALID is invalid.");
        pw.print("The valid application state can be one of the following: ");
        StringBuilder sb = new StringBuilder();
        sb.append("ALL,");
        for (YarnApplicationState state : YarnApplicationState.values()) {
            sb.append(state + ",");
        }
        String output = sb.toString();
        pw.println(output.substring(0, output.length() - 1));
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(4)).write(any(byte[].class), anyInt(), anyInt());

        //Test command yarn application -list --appStates all
        sysOutStream.reset();
        Set<String> appType5 = new HashSet<String>();

        EnumSet<YarnApplicationState> appState5 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState5.add(YarnApplicationState.FINISHED);
        when(client.getApplications(appType5, appState5)).thenReturn(
                getApplicationReports(applicationReports, appType5, appState5, true));
        result =
                cli.run(new String[]{"application", "-list", "--appStates",
                        "FINISHED ,, , ALL"});
        assertEquals(0, result);
        verify(client).getApplications(appType5, appState5);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType5
                + " and states: " + appState5 + ")" + ":" + 6);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0005\t             ");
        pw.print("appname\t                YARN\t      user\t     ");
        pw.print("queue\t           RUNNING\t         ");
        pw.print("SUCCEEDED\t         53.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0006\t            ");
        pw.print("appname2\t            NON-YARN\t     user2\t    ");
        pw.print("queue2\t          FINISHED\t         ");
        pw.print("SUCCEEDED\t         63.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0007\t            ");
        pw.print("appname3\t           MAPREDUCE\t     user3\t    ");
        pw.print("queue3\t           RUNNING\t         ");
        pw.print("SUCCEEDED\t         73.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0008\t            ");
        pw.print("appname4\t       NON-MAPREDUCE\t     user4\t    ");
        pw.print("queue4\t            FAILED\t         ");
        pw.print("SUCCEEDED\t         83.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0009\t            ");
        pw.print("appname5\t                HIVE\t     user5\t    ");
        pw.print("queue5\t          ACCEPTED\t            ");
        pw.print("KILLED\t         93.79%");
        pw.println("\t                                N/A");
        pw.print("         application_1234_0010\t            ");
        pw.print("appname6\t                 PIG\t     user6\t    ");
        pw.print("queue6\t         SUBMITTED\t            ");
        pw.print("KILLED\t         99.79%");
        pw.println("\t                                N/A");
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(5)).write(any(byte[].class), anyInt(), anyInt());

        // Test command yarn application user case insensitive
        sysOutStream.reset();
        Set<String> appType6 = new HashSet<String>();
        appType6.add("YARN");
        appType6.add("NON-YARN");

        EnumSet<YarnApplicationState> appState6 =
                EnumSet.noneOf(YarnApplicationState.class);
        appState6.add(YarnApplicationState.FINISHED);
        when(client.getApplications(appType6, appState6)).thenReturn(
                getApplicationReports(applicationReports, appType6, appState6, false));
        result =
                cli.run(new String[]{"application", "-list", "-appTypes",
                        "YARN, ,,  NON-YARN", "--appStates", "finished"});
        assertEquals(0, result);
        verify(client).getApplications(appType6, appState6);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total number of applications (application-types: " + appType6
                + " and states: " + appState6 + ")" + ":" + 1);
        pw.print("                Application-Id\t    Application-Name");
        pw.print("\t    Application-Type");
        pw.print("\t      User\t     Queue\t             State\t       ");
        pw.print("Final-State\t       Progress");
        pw.println("\t                       Tracking-URL");
        pw.print("         application_1234_0006\t            ");
        pw.print("appname2\t            NON-YARN\t     user2\t    ");
        pw.print("queue2\t          FINISHED\t         ");
        pw.print("SUCCEEDED\t         63.79%");
        pw.println("\t                                N/A");
        pw.close();
        appsReportStr = baos.toString("UTF-8");
        Assert.assertEquals(appsReportStr, sysOutStream.toString());
        verify(sysOut, times(6)).write(any(byte[].class), anyInt(), anyInt());
    }

    private List<ApplicationReport> getApplicationReports(
            List<ApplicationReport> applicationReports,
            Set<String> appTypes, EnumSet<YarnApplicationState> appStates,
            boolean allStates) {

        List<ApplicationReport> appReports = new ArrayList<ApplicationReport>();

        if (allStates) {
            for (YarnApplicationState state : YarnApplicationState.values()) {
                appStates.add(state);
            }
        }
        for (ApplicationReport appReport : applicationReports) {
            if (appTypes != null && !appTypes.isEmpty()) {
                if (!appTypes.contains(appReport.getApplicationType())) {
                    continue;
                }
            }

            if (appStates != null && !appStates.isEmpty()) {
                if (!appStates.contains(appReport.getYarnApplicationState())) {
                    continue;
                }
            }

            appReports.add(appReport);
        }
        return appReports;
    }

    @Test(timeout = 10000)
    public void testAppsHelpCommand() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationCLI spyCli = spy(cli);
        int result = spyCli.run(new String[]{"application", "-help"});
        Assert.assertTrue(result == 0);
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        result = cli.run(
                new String[]{"application", "-kill", applicationId.toString(), "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        NodeId nodeId = NodeId.newInstance("host0", 0);
        result = cli.run(
                new String[]{"application", "-status", nodeId.toString(), "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationCLIHelpMessage(),
                sysOutStream.toString());
    }

    @Test(timeout = 10000)
    public void testAppAttemptsHelpCommand() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationCLI spyCli = spy(cli);
        int result = spyCli.run(new String[]{"applicationattempt", "-help"});
        Assert.assertTrue(result == 0);
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationAttemptCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        result = cli.run(
                new String[]{"applicationattempt", "-list", applicationId.toString(),
                        "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationAttemptCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        ApplicationAttemptId appAttemptId =
                ApplicationAttemptId.newInstance(applicationId, 6);
        result = cli.run(
                new String[]{"applicationattempt", "-status", appAttemptId.toString(),
                        "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createApplicationAttemptCLIHelpMessage(),
                sysOutStream.toString());
    }

    @Test(timeout = 10000)
    public void testContainersHelpCommand() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationCLI spyCli = spy(cli);
        int result = spyCli.run(new String[]{"container", "-help"});
        Assert.assertTrue(result == 0);
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createContainerCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);
        ApplicationAttemptId appAttemptId =
                ApplicationAttemptId.newInstance(applicationId, 6);
        result = cli.run(
                new String[]{"container", "-list", appAttemptId.toString(), "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createContainerCLIHelpMessage(),
                sysOutStream.toString());

        sysOutStream.reset();
        ContainerId containerId = ContainerId.newInstance(appAttemptId, 7);
        result = cli.run(
                new String[]{"container", "-status", containerId.toString(), "args"});
        verify(spyCli).printUsage(any(String.class), any(Options.class));
        Assert.assertEquals(createContainerCLIHelpMessage(),
                sysOutStream.toString());
    }

    @Test(timeout = 5000)
    public void testNodesHelpCommand() throws Exception {
        NodeCLI nodeCLI = new NodeCLI();
        nodeCLI.setClient(client);
        nodeCLI.setSysOutPrintStream(sysOut);
        nodeCLI.setSysErrPrintStream(sysErr);
        nodeCLI.run(new String[]{});
        Assert.assertEquals(createNodeCLIHelpMessage(),
                sysOutStream.toString());
    }

    @Test
    public void testKillApplication() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);

        ApplicationReport newApplicationReport2 = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.FINISHED, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        when(client.getApplicationReport(any(ApplicationId.class))).thenReturn(
                newApplicationReport2);
        int result = cli.run(new String[]{"application", "-kill", applicationId.toString()});
        assertEquals(0, result);
        verify(client, times(0)).killApplication(any(ApplicationId.class));
        verify(sysOut).println(
                "Application " + applicationId + " has already finished ");

        ApplicationReport newApplicationReport = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.RUNNING, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        when(client.getApplicationReport(any(ApplicationId.class))).thenReturn(
                newApplicationReport);
        result = cli.run(new String[]{"application", "-kill", applicationId.toString()});
        assertEquals(0, result);
        verify(client).killApplication(any(ApplicationId.class));
        verify(sysOut).println("Killing application application_1234_0005");

        doThrow(new ApplicationNotFoundException("Application with id '"
                + applicationId + "' doesn't exist in RM.")).when(client)
                .getApplicationReport(applicationId);
        cli = createAndGetAppCLI();
        try {
            int exitCode =
                    cli.run(new String[]{"application", "-kill", applicationId.toString()});
            verify(sysOut).println("Application with id '" + applicationId +
                    "' doesn't exist in RM.");
            Assert.assertNotSame("should return non-zero exit code.", 0, exitCode);
        } catch (ApplicationNotFoundException appEx) {
            Assert.fail("application -kill should not throw" +
                    "ApplicationNotFoundException. " + appEx);
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testMoveApplicationAcrossQueues() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        ApplicationId applicationId = ApplicationId.newInstance(1234, 5);

        ApplicationReport newApplicationReport2 = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.FINISHED, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        when(client.getApplicationReport(any(ApplicationId.class))).thenReturn(
                newApplicationReport2);
        int result = cli.run(new String[]{"application", "-movetoqueue",
                applicationId.toString(), "-queue", "targetqueue"});
        assertEquals(0, result);
        verify(client, times(0)).moveApplicationAcrossQueues(
                any(ApplicationId.class), any(String.class));
        verify(sysOut).println(
                "Application " + applicationId + " has already finished ");

        ApplicationReport newApplicationReport = ApplicationReport.newInstance(
                applicationId, ApplicationAttemptId.newInstance(applicationId, 1),
                "user", "queue", "appname", "host", 124, null,
                YarnApplicationState.RUNNING, "diagnostics", "url", 0, 0,
                FinalApplicationStatus.SUCCEEDED, null, "N/A", 0.53789f, "YARN", null);
        when(client.getApplicationReport(any(ApplicationId.class))).thenReturn(
                newApplicationReport);
        result = cli.run(new String[]{"application", "-movetoqueue",
                applicationId.toString(), "-queue", "targetqueue"});
        assertEquals(0, result);
        verify(client).moveApplicationAcrossQueues(any(ApplicationId.class),
                any(String.class));
        verify(sysOut).println("Moving application application_1234_0005 to queue targetqueue");
        verify(sysOut).println("Successfully completed move.");

        doThrow(new ApplicationNotFoundException("Application with id '"
                + applicationId + "' doesn't exist in RM.")).when(client)
                .moveApplicationAcrossQueues(applicationId, "targetqueue");
        cli = createAndGetAppCLI();
        try {
            result = cli.run(new String[]{"application", "-movetoqueue",
                    applicationId.toString(), "-queue", "targetqueue"});
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ApplicationNotFoundException);
            Assert.assertEquals("Application with id '" + applicationId +
                    "' doesn't exist in RM.", ex.getMessage());
        }
    }

    @Test
    public void testListClusterNodes() throws Exception {
        List<NodeReport> nodeReports = new ArrayList<NodeReport>();
        nodeReports.addAll(getNodeReports(1, NodeState.NEW));
        nodeReports.addAll(getNodeReports(2, NodeState.RUNNING));
        nodeReports.addAll(getNodeReports(1, NodeState.UNHEALTHY));
        nodeReports.addAll(getNodeReports(1, NodeState.DECOMMISSIONED));
        nodeReports.addAll(getNodeReports(1, NodeState.REBOOTED));
        nodeReports.addAll(getNodeReports(1, NodeState.LOST));

        NodeCLI cli = new NodeCLI();
        cli.setClient(client);
        cli.setSysOutPrintStream(sysOut);

        Set<NodeState> nodeStates = new HashSet<NodeState>();
        nodeStates.add(NodeState.NEW);
        NodeState[] states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        int result = cli.run(new String[]{"-list", "--states", "NEW"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Total Nodes:1");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t            NEW\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        String nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(1)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.RUNNING);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states", "RUNNING"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:2");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host1:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(2)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        result = cli.run(new String[]{"-list"});
        assertEquals(0, result);
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(3)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.UNHEALTHY);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states", "UNHEALTHY"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:1");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t      UNHEALTHY\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(4)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.DECOMMISSIONED);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states", "DECOMMISSIONED"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:1");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t DECOMMISSIONED\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(5)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.REBOOTED);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states", "REBOOTED"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:1");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t       REBOOTED\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(6)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.LOST);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states", "LOST"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:1");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t           LOST\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(7)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        nodeStates.add(NodeState.NEW);
        nodeStates.add(NodeState.RUNNING);
        nodeStates.add(NodeState.LOST);
        nodeStates.add(NodeState.REBOOTED);
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--states",
                "NEW,RUNNING,LOST,REBOOTED"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:5");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t            NEW\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host1:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t       REBOOTED\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t           LOST\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(8)).write(any(byte[].class), anyInt(), anyInt());

        sysOutStream.reset();
        nodeStates.clear();
        for (NodeState s : NodeState.values()) {
            nodeStates.add(s);
        }
        states = nodeStates.toArray(new NodeState[0]);
        when(client.getNodeReports(states))
                .thenReturn(getNodeReports(nodeReports, nodeStates));
        result = cli.run(new String[]{"-list", "--all"});
        assertEquals(0, result);
        verify(client).getNodeReports(states);
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
        pw.println("Total Nodes:7");
        pw.print("         Node-Id\t     Node-State\tNode-Http-Address\t");
        pw.println("Number-of-Running-Containers");
        pw.print("         host0:0\t            NEW\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host1:0\t        RUNNING\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t      UNHEALTHY\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t DECOMMISSIONED\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t       REBOOTED\t       host1:8888\t");
        pw.println("                           0");
        pw.print("         host0:0\t           LOST\t       host1:8888\t");
        pw.println("                           0");
        pw.close();
        nodesReportStr = baos.toString("UTF-8");
        Assert.assertEquals(nodesReportStr, sysOutStream.toString());
        verify(sysOut, times(9)).write(any(byte[].class), anyInt(), anyInt());
    }

    private List<NodeReport> getNodeReports(
            List<NodeReport> nodeReports,
            Set<NodeState> nodeStates) {
        List<NodeReport> reports = new ArrayList<NodeReport>();

        for (NodeReport nodeReport : nodeReports) {
            if (nodeStates.contains(nodeReport.getNodeState())) {
                reports.add(nodeReport);
            }
        }
        return reports;
    }

    @Test
    public void testNodeStatus() throws Exception {
        NodeId nodeId = NodeId.newInstance("host0", 0);
        NodeCLI cli = new NodeCLI();
        when(client.getNodeReports()).thenReturn(
                getNodeReports(3, NodeState.RUNNING));
        cli.setClient(client);
        cli.setSysOutPrintStream(sysOut);
        cli.setSysErrPrintStream(sysErr);
        int result = cli.run(new String[]{"-status", nodeId.toString()});
        assertEquals(0, result);
        verify(client).getNodeReports();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("Node Report : ");
        pw.println("\tNode-Id : host0:0");
        pw.println("\tRack : rack1");
        pw.println("\tNode-State : RUNNING");
        pw.println("\tNode-Http-Address : host1:8888");
        pw.println("\tLast-Health-Update : "
                + DateFormatUtils.format(new Date(0), "E dd/MMM/yy hh:mm:ss:SSzz"));
        pw.println("\tHealth-Report : ");
        pw.println("\tContainers : 0");
        pw.println("\tMemory-Used : 0MB");
        pw.println("\tMemory-Capacity : 0MB");
        pw.println("\tCPU-Used : 0 vcores");
        pw.println("\tCPU-Capacity : 0 vcores");
        pw.close();
        String nodeStatusStr = baos.toString("UTF-8");
        verify(sysOut, times(1)).println(isA(String.class));
        verify(sysOut).println(nodeStatusStr);
    }

    @Test
    public void testAbsentNodeStatus() throws Exception {
        NodeId nodeId = NodeId.newInstance("Absenthost0", 0);
        NodeCLI cli = new NodeCLI();
        when(client.getNodeReports()).thenReturn(
                getNodeReports(0, NodeState.RUNNING));
        cli.setClient(client);
        cli.setSysOutPrintStream(sysOut);
        cli.setSysErrPrintStream(sysErr);
        int result = cli.run(new String[]{"-status", nodeId.toString()});
        assertEquals(0, result);
        verify(client).getNodeReports();
        verify(sysOut, times(1)).println(isA(String.class));
        verify(sysOut).println(
                "Could not find the node report for node id : " + nodeId.toString());
    }

    @Test
    public void testAppCLIUsageInfo() throws Exception {
        verifyUsageInfo(new ApplicationCLI());
    }

    @Test
    public void testNodeCLIUsageInfo() throws Exception {
        verifyUsageInfo(new NodeCLI());
    }

    @Test
    public void testMissingArguments() throws Exception {
        ApplicationCLI cli = createAndGetAppCLI();
        int result = cli.run(new String[]{"application", "-status"});
        Assert.assertEquals(result, -1);
        Assert.assertEquals(String.format("Missing argument for options%n%1s",
                createApplicationCLIHelpMessage()), sysOutStream.toString());

        sysOutStream.reset();
        result = cli.run(new String[]{"applicationattempt", "-status"});
        Assert.assertEquals(result, -1);
        Assert.assertEquals(String.format("Missing argument for options%n%1s",
                createApplicationAttemptCLIHelpMessage()), sysOutStream.toString());

        sysOutStream.reset();
        result = cli.run(new String[]{"container", "-status"});
        Assert.assertEquals(result, -1);
        Assert.assertEquals(String.format("Missing argument for options%n%1s",
                createContainerCLIHelpMessage()), sysOutStream.toString());

        sysOutStream.reset();
        NodeCLI nodeCLI = new NodeCLI();
        nodeCLI.setClient(client);
        nodeCLI.setSysOutPrintStream(sysOut);
        nodeCLI.setSysErrPrintStream(sysErr);
        result = nodeCLI.run(new String[]{"-status"});
        Assert.assertEquals(result, -1);
        Assert.assertEquals(String.format("Missing argument for options%n%1s",
                createNodeCLIHelpMessage()), sysOutStream.toString());
    }

    private void verifyUsageInfo(YarnCLI cli) throws Exception {
        cli.setSysErrPrintStream(sysErr);
        cli.run(new String[]{"application"});
        verify(sysErr).println("Invalid Command Usage : ");
    }

    private List<NodeReport> getNodeReports(int noOfNodes, NodeState state) {
        List<NodeReport> nodeReports = new ArrayList<NodeReport>();

        for (int i = 0; i < noOfNodes; i++) {
            NodeReport nodeReport = NodeReport.newInstance(NodeId
                            .newInstance("host" + i, 0), state, "host" + 1 + ":8888",
                    "rack1", Records.newRecord(Resource.class), Records
                            .newRecord(Resource.class), 0, "", 0);
            nodeReports.add(nodeReport);
        }
        return nodeReports;
    }

    private ApplicationCLI createAndGetAppCLI() {
        ApplicationCLI cli = new ApplicationCLI();
        cli.setClient(client);
        cli.setSysOutPrintStream(sysOut);
        return cli;
    }

    private String createApplicationCLIHelpMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("usage: application");
        pw.println(" -appStates <States>             Works with -list to filter applications");
        pw.println("                                 based on input comma-separated list of");
        pw.println("                                 application states. The valid application");
        pw.println("                                 state can be one of the following:");
        pw.println("                                 ALL,NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUN");
        pw.println("                                 NING,FINISHED,FAILED,KILLED");
        pw.println(" -appTypes <Types>               Works with -list to filter applications");
        pw.println("                                 based on input comma-separated list of");
        pw.println("                                 application types.");
        pw.println(" -help                           Displays help for all commands.");
        pw.println(" -kill <Application ID>          Kills the application.");
        pw.println(" -list                           List applications. Supports optional use");
        pw.println("                                 of -appTypes to filter applications based");
        pw.println("                                 on application type, and -appStates to");
        pw.println("                                 filter applications based on application");
        pw.println("                                 state.");
        pw.println(" -movetoqueue <Application ID>   Moves the application to a different");
        pw.println("                                 queue.");
        pw.println(" -queue <Queue Name>             Works with the movetoqueue command to");
        pw.println("                                 specify which queue to move an");
        pw.println("                                 application to.");
        pw.println(" -status <Application ID>        Prints the status of the application.");
        pw.close();
        String appsHelpStr = baos.toString("UTF-8");
        return appsHelpStr;
    }

    private String createApplicationAttemptCLIHelpMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("usage: applicationattempt");
        pw.println(" -help                              Displays help for all commands.");
        pw.println(" -list <Application ID>             List application attempts for");
        pw.println("                                    aplication.");
        pw.println(" -status <Application Attempt ID>   Prints the status of the application");
        pw.println("                                    attempt.");
        pw.close();
        String appsHelpStr = baos.toString("UTF-8");
        return appsHelpStr;
    }

    private String createContainerCLIHelpMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("usage: container");
        pw.println(" -help                            Displays help for all commands.");
        pw.println(" -list <Application Attempt ID>   List containers for application attempt.");
        pw.println(" -status <Container ID>           Prints the status of the container.");
        pw.close();
        String appsHelpStr = baos.toString("UTF-8");
        return appsHelpStr;
    }

    private String createNodeCLIHelpMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("usage: node");
        pw.println(" -all               Works with -list to list all nodes.");
        pw.println(" -list              List all running nodes. Supports optional use of");
        pw.println("                    -states to filter nodes based on node state, all -all");
        pw.println("                    to list all nodes.");
        pw.println(" -states <States>   Works with -list to filter nodes based on input");
        pw.println("                    comma-separated list of node states.");
        pw.println(" -status <NodeId>   Prints the status report of the node.");
        pw.close();
        String nodesHelpStr = baos.toString("UTF-8");
        return nodesHelpStr;
    }
}
