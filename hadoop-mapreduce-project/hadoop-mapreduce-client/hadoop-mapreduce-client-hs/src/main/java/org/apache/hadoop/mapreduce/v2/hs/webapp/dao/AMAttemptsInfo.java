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
 * Unless required by joblicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapreduce.v2.hs.webapp.dao;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jobAttempts")
@XmlAccessorType(XmlAccessType.FIELD)
public class AMAttemptsInfo {

    @XmlElement(name = "jobAttempt")
    protected ArrayList<AMAttemptInfo> attempt = new ArrayList<AMAttemptInfo>();

    public AMAttemptsInfo() {
    } // JAXB needs this

    public void add(AMAttemptInfo info) {
        this.attempt.add(info);
    }

    public ArrayList<AMAttemptInfo> getAttempts() {
        return this.attempt;
    }

}
