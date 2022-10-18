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
package org.apache.karaf.minho.classpathprotocolhandler;

import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Log
public class ClasspathURLConnection extends URLConnection {

    protected ClasspathURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        log.fine("Connecting to " + url);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        log.info("Reading from " + url);
        String location = url.toString().substring("classpath:".length());
        log.info("Location: " + location);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        if (inputStream == null) {
            throw new IOException(location + " not found in classpath");
        }
        return inputStream;
    }

}
