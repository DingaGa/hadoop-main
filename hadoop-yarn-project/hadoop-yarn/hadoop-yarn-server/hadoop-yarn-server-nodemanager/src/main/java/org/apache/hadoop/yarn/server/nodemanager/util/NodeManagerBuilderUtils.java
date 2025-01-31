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
package org.apache.hadoop.yarn.server.nodemanager.util;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.server.nodemanager.api.ResourceLocalizationSpec;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

public class NodeManagerBuilderUtils {

    public static ResourceLocalizationSpec newResourceLocalizationSpec(
            LocalResource rsrc, Path path) {
        URL local = ConverterUtils.getYarnUrlFromPath(path);
        ResourceLocalizationSpec resourceLocalizationSpec =
                Records.newRecord(ResourceLocalizationSpec.class);
        resourceLocalizationSpec.setDestinationDirectory(local);
        resourceLocalizationSpec.setResource(rsrc);
        return resourceLocalizationSpec;
    }

}
