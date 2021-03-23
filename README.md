//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

= Apache Karaf

Apache Karaf is a runtime supporting different kind of applications, and providing
out of the box features you can leverage and benefit.

Apache Karaf provides:

* a generic "boot" (`Karaf`) bootstrapping a runtime for your applications that you can configure easily
* an API to interact with the launcher if you need
* a provisioning layer to easily add a set of applications
* core runtime services dealing with log, configuration, and much more for you
* optional features you can add in the runtime and leverage in your applications.

== Library

A library allows you to define a dependency set that modules deployed in Karaf can use (overriding the module dependencies).

The libaries are configured in `KarafConfig` (via `karaf.json` or programmatically), at main level.

== Module

A Karaf module is a generic application module you can add to the Karaf boot.

The module can be packaged in the ready to run artifact or provisioned a runtime (locally or remotely).
It could also be added on a running instance (on the fly).

Currently, Apache Karaf supports:

* OSGi bundle module
* Spring Boot module
* Microprofile module

=== Spring Boot

Apache Karaf supports Spring Boot uber jar application module.

=== OSGi Bundle

Apache Karaf supports OSGi bundle application module.

=== Microprofile

Apache Karaf supports Microprofile application module.

== Profile

A Karaf profile is a convenient way to "group" modules all together, with eventually additional resources.

A profile is defined by a JSON descriptor (inline in the main Karaf config, or added by profile jar).

For instance:

```
{
  "name": "myprofile",
  "version": "1.0",
  "config": [ "k8s:configMapId", "configfile:/path/to/config.cfg", "config:pid:foo=bar,hello=world" ],
  "properties": [ "foo=bar", "hello=world" ],
  "module": [
    { "location": "mvn:mymodules/myfirstmodule/1.1" }
    { "location": "mvn:mymodules/mysecondmodule/1.2" }
  ]
}
```

You can add an extension providing directly the location of the JSON descriptor or package the JSON descriptor
in a JAR file.

== Boot

You can easily boot a runtime. You can configure the runtime by providing a `karaf.json` descriptor:

```
{
    "home": "/path/to/home/directory",
    "data": "${home}/data/directory",
    "cache": "${cache}/cache/directory",
    "properties": [ "foo=bar", "hello=world" ],
    "library": [ 
        { "id": "json-b", "system": true, "artifacts": [ "file:/path/to/jar", ... ] },
        { "id": "my-lib", "artifacts": [ "mvn:...", "http:...", "file:...", "embedded:..." ] }
    ],
    "profiles": [
      { "name": "myprofile", "library": "my-lib", "artifacts": [ .... ] },
      { "name": "otherprofile", "module": [ ... ] }
    ],
    "modules": [ "mvn:....", "http:....", "file:....", "embedded:..." ]
}
```

This `karaf.json` file is optional: Karaf uses a preset configuration by default.

Karaf is looking for `karaf.json` file (aka Karaf Config):

* as system property: `-Dkaraf.config=/path/to/karaf.json`
* as environment variable: `export KARAF_CONFIG=/path/to/karaf.json`
* in the classpath of your application

== Distribution

As for Apache Karaf 4.x, you still have ready to use distributions.

=== Network

The Karaf Network distribution downloads all artifacts (from Maven Central) at first runtime.

=== Standard

The Karaf Standard distribution is distribution running offline (no need of Internet connection).

=== Cloud

The Karaf Cloud distribution is "light" standard distribution, ready to run on Kubernetes.

== Bootstrap & Run

You can use Apache Karaf in your code, simply bootstrapping it and running with:

```
Karaf karaf = Karaf.build(karafConfig);
karaf.init();
karaf.start();
```

or simply run with a `karaf.json`:

```
$ java -jar karaf.jar -Dkaraf.config=karaf.json
```
