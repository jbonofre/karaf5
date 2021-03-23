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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class KarafConfig {

    // TODO add interpolation, env, sys support in properties

    private final static String DEFAULT_HOME = ".";
    private final static String DEFAULT_START_LEVEL = "100";
    private final static String DEFAULT_BUNDLE_START_LEVEL = "80";
    private final static String DEFAULT_FRAMEWORK_LOG_LEVEL = "3";
    private final static String DEFAULT_BOOT_DELEGATION = "com.sun.*,\n" +
            "javax.transaction,\n" +
            "javax.transaction.*,\n" +
            "javax.xml.crypto,\n" +
            "javax.xml.crypto.*,\n" +
            "jdk.nashorn,\n" +
            "sun.*,\n" +
            "jdk.internal.reflect,\n" +
            "jdk.internal.reflect.*";
    private final static String DEFAULT_SYSTEM_PACKAGES = "org.osgi.dto;version=\"1.1\",\n" +
            "org.osgi.resource;version=\"1.0\",\n" +
            "org.osgi.resource.dto;version=\"1.0\";uses:=\"org.osgi.dto\",\n" +
            "org.osgi.framework;version=\"1.9\",\n" +
            "org.osgi.framework.dto;version=\"1.9\";uses:=\"org.osgi.dto\",\n" +
            "org.osgi.framework.hooks.module;version=\"1.1\";uses:=\"org.osgi.framework\",\n" +
            "org.osgi.framework.hooks.mavenResolver;version=\"1.0\";uses:=\"org.osgi.framework.wiring\",\n" +
            "org.osgi.framework.hooks.service;version=\"1.1\";uses:=\"org.osgi.framework\",\n" +
            "org.osgi.framework.hooks.weaving;version=\"1.1\";uses:=\"org.osgi.framework.wiring\",\n" +
            "org.osgi.framework.launch;version=\"1.2\";uses:=\"org.osgi.framework\",\n" +
            "org.osgi.framework.namespace;version=\"1.1\";uses:=\"org.osgi.resource\",\n" +
            "org.osgi.framework.startlevel;version=\"1.0\";uses:=\"org.osgi.framework\",\n" +
            "org.osgi.framework.startlevel.dto;version=\"1.0\";uses:=\"org.osgi.dto\",\n" +
            "org.osgi.framework.wiring;version=\"1.2\";uses:=\"org.osgi.framework,org.osgi.resource\",\n" +
            "org.osgi.framework.wiring.dto;version=\"1.3\";uses:=\"org.osgi.dto,org.osgi.resource.dto\",\n" +
            "org.osgi.service.condpermadmin;version=\"1.1.1\";uses:=\"org.osgi.framework,org.osgi.service.permissionadmin\",\n" +
            "org.osgi.service.packageadmin;version=\"1.2\";uses:=\"org.osgi.framework\",org.osgi.service.permissionadmin;version=\"1.2\",\n" +
            "org.osgi.service.mavenResolver;version=\"1.1\";uses:=\"org.osgi.resource\",\n" +
            "org.osgi.service.startlevel;version=\"1.1\";uses:=\"org.osgi.framework\",\n" +
            "org.osgi.service.url;version=\"1.0\",\n" +
            "org.osgi.util.tracker;version=\"1.5.2\";uses:=\"org.osgi.framework\",\n" +
            "javax.accessibility,\n" +
            "javax.activity,\n" +
            "javax.annotation;version=\"1.3\",\n" +
            "javax.annotation.processing;version=\"1.0\",\n" +
            "javax.crypto,\n" +
            "javax.crypto.interfaces,\n" +
            "javax.crypto.spec,\n" +
            "javax.imageio,\n" +
            "javax.imageio.event,\n" +
            "javax.imageio.metadata,\n" +
            "javax.imageio.plugins.bmp,\n" +
            "javax.imageio.plugins.jpeg,\n" +
            "javax.imageio.spi,\n" +
            "javax.imageio.stream,\n" +
            "javax.lang.model,\n" +
            "javax.lang.model.element,\n" +
            "javax.lang.model.type,\n" +
            "javax.lang.model.util,\n" +
            "javax.management,\n" +
            "javax.management.loading,\n" +
            "javax.management.modelmbean,\n" +
            "javax.management.monitor,\n" +
            "javax.management.openmbean,\n" +
            "javax.management.relation,\n" +
            "javax.management.remote,\n" +
            "javax.management.remote.rmi,\n" +
            "javax.management.timer,\n" +
            "javax.naming,\n" +
            "javax.naming.directory,\n" +
            "javax.naming.event,\n" +
            "javax.naming.ldap,\n" +
            "javax.naming.spi,\n" +
            "javax.net,\n" +
            "javax.net.ssl,\n" +
            "javax.print,\n" +
            "javax.print.attribute,\n" +
            "javax.print.attribute.standard,\n" +
            "javax.print.event,\n" +
            "javax.rmi,\n" +
            "javax.rmi.ssl,\n" +
            "javax.script,\n" +
            "javax.security.auth,\n" +
            "javax.security.auth.callback,\n" +
            "javax.security.auth.kerberos,\n" +
            "javax.security.auth.login,\n" +
            "javax.security.auth.spi,\n" +
            "javax.security.auth.x500,\n" +
            "javax.security.cert,\n" +
            "javax.security.sasl,\n" +
            "javax.sound.midi,\n" +
            "javax.sound.midi.spi,\n" +
            "javax.sound.sampled,\n" +
            "javax.sound.sampled.spi,\n" +
            "javax.sql,\n" +
            "javax.sql.rowset,\n" +
            "javax.sql.rowset.serial,\n" +
            "javax.sql.rowset.spi,\n" +
            "javax.swing,\n" +
            "javax.swing.border,\n" +
            "javax.swing.colorchooser,\n" +
            "javax.swing.event,\n" +
            "javax.swing.filechooser,\n" +
            "javax.swing.plaf,\n" +
            "javax.swing.plaf.basic,\n" +
            "javax.swing.plaf.metal,\n" +
            "javax.swing.plaf.multi,\n" +
            "javax.swing.plaf.synth,\n" +
            "javax.swing.table,\n" +
            "javax.swing.text,\n" +
            "javax.swing.text.html,\n" +
            "javax.swing.text.html.mavenUrlParser,\n" +
            "javax.swing.text.rtf,\n" +
            "javax.swing.tree,\n" +
            "javax.swing.undo,\n" +
            "javax.tools,\n" +
            "javax.transaction; javax.transaction.xa; version=\"1.1\"; partial=true; mandatory:=partial,\n" +
            "javax.xml,\n" +
            "javax.xml.bind;version=\"2.3.0\",\n" +
            "javax.xml.bind.annotation;version=\"2.3.0\",\n" +
            "javax.xml.bind.annotation.adapters;version=\"2.3.0\",\n" +
            "javax.xml.bind.attachment;version=\"2.3.0\",\n" +
            "javax.xml.bind.helpers;version=\"2.3.0\",\n" +
            "javax.xml.bind.util;version=\"2.3.0\",\n" +
            "javax.xml.crypto,\n" +
            "javax.xml.crypto.dom,\n" +
            "javax.xml.crypto.dsig,\n" +
            "javax.xml.crypto.dsig.dom,\n" +
            "javax.xml.crypto.dsig.keyinfo,\n" +
            "javax.xml.crypto.dsig.spec,\n" +
            "javax.xml.datatype,\n" +
            "javax.xml.namespace,\n" +
            "javax.xml.parsers,\n" +
            "javax.xml.stream;version=\"1.2\",\n" +
            "javax.xml.stream.events;version=\"1.2\",\n" +
            "javax.xml.stream.util;version=\"1.2\",\n" +
            "javax.xml.transform,\n" +
            "javax.xml.transform.dom,\n" +
            "javax.xml.transform.sax,\n" +
            "javax.xml.transform.stax,\n" +
            "javax.xml.transform.stream,\n" +
            "javax.xml.validation,\n" +
            "javax.xml.xpath,\n" +
            "javafx.animation,\n" +
            "javafx.application,\n" +
            "javafx.beans,\n" +
            "javafx.beans.binding,\n" +
            "javafx.beans.property,\n" +
            "javafx.beans.property.adapter,\n" +
            "javafx.beans.value,\n" +
            "javafx.collections,\n" +
            "javafx.collections.transformation,\n" +
            "javafx.concurrent,\n" +
            "javafx.css,\n" +
            "javafx.embed.swing,\n" +
            "javafx.embed.swt,\n" +
            "javafx.event,\n" +
            "javafx.fxml,\n" +
            "javafx.geometry,\n" +
            "javafx.print,\n" +
            "javafx.scene,\n" +
            "javafx.scene.canvas,\n" +
            "javafx.scene.chart,\n" +
            "javafx.scene.control,\n" +
            "javafx.scene.control.cell,\n" +
            "javafx.scene.effect,\n" +
            "javafx.scene.image,\n" +
            "javafx.scene.input,\n" +
            "javafx.scene.layout,\n" +
            "javafx.scene.media,\n" +
            "javafx.scene.paint,\n" +
            "javafx.scene.shape,\n" +
            "javafx.scene.text,\n" +
            "javafx.scene.transform,\n" +
            "javafx.scene.web,\n" +
            "javafx.stage,\n" +
            "javafx.util,\n" +
            "javafx.util.converter,\n" +
            "netscape.javascript,\n" +
            "org.ietf.jgss,\n" +
            "org.w3c.dom,\n" +
            "org.w3c.dom.bootstrap,\n" +
            "org.w3c.dom.css,\n" +
            "org.w3c.dom.events,\n" +
            "org.w3c.dom.html,\n" +
            "org.w3c.dom.ls,\n" +
            "org.w3c.dom.ranges,\n" +
            "org.w3c.dom.stylesheets,\n" +
            "org.w3c.dom.traversal,\n" +
            "org.w3c.dom.views,\n" +
            "org.w3c.dom.xpath,\n" +
            "org.xml.sax,\n" +
            "org.xml.sax.ext,\n" +
            "org.xml.sax.helpers";

    private String home;
    private String data;
    private String cache;
    private boolean clearCache;
    private String startLevel;
    private String bundleStartLevel;
    private String frameworkLogLevel;
    private String bootDelegation;
    private String systemPackages;
    private Map<String, String> properties = new HashMap<>();
    private List<Library> libraries = new ArrayList<>();
    private Configs configs;
    private List<Profile> profiles = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();

    public KarafConfig() {
        this.home = DEFAULT_HOME;
        this.data = this.home + "/data";
        this.cache = this.data + "/cache";
        this.clearCache = false;
        this.startLevel = DEFAULT_START_LEVEL;
        this.bundleStartLevel = DEFAULT_BUNDLE_START_LEVEL;
        this.frameworkLogLevel = DEFAULT_FRAMEWORK_LOG_LEVEL;
        this.bootDelegation = DEFAULT_BOOT_DELEGATION;
        this.systemPackages = DEFAULT_SYSTEM_PACKAGES;
    }

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
