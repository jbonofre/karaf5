/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.minho.tooling.common.model;

import lombok.Data;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Data
public class MinhoBuild {

    private String name;
    private String version;
    private Map<String, Object> properties;
    private List<String> dependencies;

    public static MinhoBuild load(InputStream inputStream) throws Exception {
        Mapper mapper = new MapperBuilder().build();
        return mapper.readObject(inputStream, MinhoBuild.class);
    }

}
