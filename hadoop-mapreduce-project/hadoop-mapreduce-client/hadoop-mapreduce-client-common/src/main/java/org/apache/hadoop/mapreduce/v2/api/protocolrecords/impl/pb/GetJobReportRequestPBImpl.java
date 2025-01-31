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

package org.apache.hadoop.mapreduce.v2.api.protocolrecords.impl.pb;


import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetJobReportRequest;
import org.apache.hadoop.mapreduce.v2.api.records.JobId;
import org.apache.hadoop.mapreduce.v2.api.records.impl.pb.JobIdPBImpl;
import org.apache.hadoop.mapreduce.v2.proto.MRProtos.JobIdProto;
import org.apache.hadoop.mapreduce.v2.proto.MRServiceProtos.GetJobReportRequestProto;
import org.apache.hadoop.mapreduce.v2.proto.MRServiceProtos.GetJobReportRequestProtoOrBuilder;
import org.apache.hadoop.yarn.api.records.impl.pb.ProtoBase;


public class GetJobReportRequestPBImpl extends ProtoBase<GetJobReportRequestProto> implements GetJobReportRequest {
    GetJobReportRequestProto proto = GetJobReportRequestProto.getDefaultInstance();
    GetJobReportRequestProto.Builder builder = null;
    boolean viaProto = false;

    private JobId jobId = null;


    public GetJobReportRequestPBImpl() {
        builder = GetJobReportRequestProto.newBuilder();
    }

    public GetJobReportRequestPBImpl(GetJobReportRequestProto proto) {
        this.proto = proto;
        viaProto = true;
    }

    public GetJobReportRequestProto getProto() {
        mergeLocalToProto();
        proto = viaProto ? proto : builder.build();
        viaProto = true;
        return proto;
    }

    private void mergeLocalToBuilder() {
        if (this.jobId != null) {
            builder.setJobId(convertToProtoFormat(this.jobId));
        }
    }

    private void mergeLocalToProto() {
        if (viaProto)
            maybeInitBuilder();
        mergeLocalToBuilder();
        proto = builder.build();
        viaProto = true;
    }

    private void maybeInitBuilder() {
        if (viaProto || builder == null) {
            builder = GetJobReportRequestProto.newBuilder(proto);
        }
        viaProto = false;
    }


    @Override
    public JobId getJobId() {
        GetJobReportRequestProtoOrBuilder p = viaProto ? proto : builder;
        if (this.jobId != null) {
            return this.jobId;
        }
        if (!p.hasJobId()) {
            return null;
        }
        this.jobId = convertFromProtoFormat(p.getJobId());
        return this.jobId;
    }

    @Override
    public void setJobId(JobId jobId) {
        maybeInitBuilder();
        if (jobId == null)
            builder.clearJobId();
        this.jobId = jobId;
    }

    private JobIdPBImpl convertFromProtoFormat(JobIdProto p) {
        return new JobIdPBImpl(p);
    }

    private JobIdProto convertToProtoFormat(JobId t) {
        return ((JobIdPBImpl) t).getProto();
    }


}
