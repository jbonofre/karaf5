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
package org.apache.karaf.classpathprotocolhandler;

import lombok.extern.java.Log;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

import java.net.URL;

@Log
public class ClasspathProtocolService implements Service {

    @Override
    public String name() {
        return "classpath-protocol";
    }

    @Override
    public void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        log.info("Starting classpath: protocol service");
        URL.setURLStreamHandlerFactory(new ClasspathURLStreamHandlerFactory());
    }

}
