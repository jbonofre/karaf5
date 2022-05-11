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
package org.apache.karaf.web.jetty;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.karaf.boot.spi.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class TestServlet extends HttpServlet implements Service {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Writer writer = response.getWriter()) {
            writer.write("<html><head><title>Test</title></head><body>Hello World!</body></html>");
            writer.flush();
        }
    }

    @Override
    public Properties properties() {
        Properties properties = new Properties();
        properties.put("contextPath", "/test");
        return properties;
    }

}
