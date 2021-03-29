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
package org.apache.karaf.boot.config;

import lombok.Data;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Data
public class KarafConfig {

    private Launcher launcher;
    private List<Profile> profiles = new LinkedList<>();
    private List<Application> applications = new LinkedList<>();

    private final static Jsonb jsonb;

    static {
        jsonb = JsonbBuilder.create();
    }

    public final static KarafConfig build() throws Exception {
        return new KarafConfig();
    }

    public final static KarafConfig read(InputStream inputStream) throws Exception {
        return jsonb.fromJson(inputStream, KarafConfig.class);
    }

}
