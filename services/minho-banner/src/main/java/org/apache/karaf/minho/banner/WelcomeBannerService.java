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
package org.apache.karaf.minho.banner;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Log
public class WelcomeBannerService implements Service {

    @Override
    public String name() {
        return "minho-banner-service";
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        if (serviceRegistry.get(ConfigService.class) != null) {
            ConfigService configService = serviceRegistry.get(ConfigService.class);
            if (configService.property("minho.banner") != null) {
                log.info(configService.property("minho.banner"));
                return;
            }
        }

        if (System.getenv("MINHO_BANNER") != null) {
            log.info(System.getenv("MINHO_BANNER"));
            return;
        }

        if (System.getProperty("minho.banner") != null) {
            log.info(System.getProperty("minho.banner"));
            return;
        }

        File file = new File("banner.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                log.info("\n" + builder.toString());
            } catch (Exception e) {
                // no-op
            }
            return;
        }

        log.info("\n" +
                " __  __ _       _           \n" +
                "|  \\/  (_)_ __ | |__   ___  \n" +
                "| |\\/| | | '_ \\| '_ \\ / _ \\ \n" +
                "| |  | | | | | | | | | (_) |\n" +
                "|_|  |_|_|_| |_|_| |_|\\___/ " +
                "\n" +
                "  Apache Karaf Minho 1.x\n");
    }

}
