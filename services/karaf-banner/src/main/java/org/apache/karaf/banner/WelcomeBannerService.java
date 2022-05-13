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
package org.apache.karaf.banner;

import lombok.extern.java.Log;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

@Log
public class WelcomeBannerService implements Service {

    @Override
    public String name() {
        return "karaf-banner";
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // banner
        if (System.getenv("KARAF_BANNER") != null) {
            log.info(System.getenv("KARAF_BANNER"));
        } else if (System.getProperty("karaf.banner") != null) {
            log.info(System.getProperty("karaf.banner"));
        } else {
            log.info("\n" +
                    "        __ __                  ____      \n" +
                    "       / //_/____ __________ _/ __/      \n" +
                    "      / ,<  / __ `/ ___/ __ `/ /_        \n" +
                    "     / /| |/ /_/ / /  / /_/ / __/        \n" +
                    "    /_/ |_|\\__,_/_/   \\__,_/_/         \n" +
                    "\n" +
                    "  Apache Karaf 5.x\n");
        }
    }

}
