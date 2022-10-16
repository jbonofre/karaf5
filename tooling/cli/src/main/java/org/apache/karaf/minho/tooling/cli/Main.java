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
package org.apache.karaf.minho.tooling.cli;

import org.apache.commons.cli.*;
import org.apache.karaf.minho.tooling.common.Runtime;

import java.io.File;
import java.io.FileInputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option("f", "file", true, "Location of the minho-build.json file"));
        options.addOption(new Option("h", "help", false, "print this message"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("minho-build [package|jar|archive] \nDefault build action is package", options);
            System.exit(1);
        }

        File minhoBuild;
        if (cmd.hasOption("file")) {
            minhoBuild = new File(cmd.getOptionValue("file"));
        } else {
            minhoBuild = new File("minho-build.json");
        }

        if (!minhoBuild.exists()) {
            System.err.println("minho-build.json file not found");
            System.exit(1);
        }

        String action = "package";

        if (cmd.getArgList().size() == 1) {
            action = cmd.getArgList().get(0);
        }

        if (action.equalsIgnoreCase("package")) {
            Runtime.createPackage(new FileInputStream(minhoBuild));
        } else if (action.equalsIgnoreCase("jar")) {
            Runtime.createJar(new FileInputStream(minhoBuild));
        } else if (action.equalsIgnoreCase("archive")) {
            Runtime.createArchive(new FileInputStream(minhoBuild));
        } else {
            System.err.println("Build action argument is not valid. It should be package|jar|archive");
            System.exit(1);
        }

    }

}
