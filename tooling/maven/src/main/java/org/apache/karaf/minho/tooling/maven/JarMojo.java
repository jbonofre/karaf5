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
package org.apache.karaf.minho.tooling.maven;

import org.apache.karaf.minho.tooling.common.Runtime;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.FileInputStream;

@Mojo(name = "jar", defaultPhase = LifecyclePhase.PACKAGE)
public class JarMojo extends MojoSupport {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Creating Minho runtime uber jar");
        if (name != null && version != null) {
            try {
                Runtime runtime = new Runtime(name, version, properties);
                dependencies.forEach(dependency -> {
                    runtime.getDependencies().add(dependency);
                });
                runtime.createJar();
            } catch (Exception e) {
                throw new MojoExecutionException("Can't create runtime uber jar", e);
            }
        } else {
            try {
                Runtime.createJar(new FileInputStream(minhoBuild));
            } catch (Exception e) {
                throw new MojoExecutionException("Can't create runtime uber jar", e);
            }
        }
    }

}
