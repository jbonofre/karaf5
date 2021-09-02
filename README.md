<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# Apache Karaf

Apache Karaf is an application runtime. Karaf is able to operate different kind of applications using application 
manager services.

It provides extensible launchers per application kind and out of the box services that any application 
running on Karaf can leverage without cost.

Apache Karaf is composed by:

* Karaf boot (`Karaf`) bootstraps the runtimes for your applications, the runtimes are discovered and extensible
* an API to interact with the boot if you need
* profiles to easily add cross runtimes dependencies
* services to easily add cross runtimes features (log, configurations, URL handlers, ...)
* boot applications

Karaf boot can be described/configure programmatically or by a provided JSON file.

## Karaf Boot

Karaf boot is the main runtimes orchestrator. It basically loads Karaf Services via SPI, and it's configured via
`KarafConfig`.

`KarafConfig` can be provided programmatically:

```
KarafConfig config = new KarafConfig();
...
Karaf karaf = Karaf.build(config);
karaf.init();
karaf.start();
```

or loaded from an external resource like `karaf.json`:

```
{
 "properties": {
    "foo": "bar"
 },
 "applications": [
    {
        "url": "/path/to/my/jar"
    }
 ]
}
```

Karaf Boot is looking for `karaf.json` file (aka Karaf Config):

* as system property: `-Dkaraf.config=/path/to/karaf.json`
* as environment variable: `export KARAF_CONFIG=/path/to/karaf.json`
* in the current classpath

### Runtime

Karaf Boot provides:
* a generic services launcher
* a service registry
* core Karaf services, as the Lifecycle service.

The runtimes are known as *Karaf Services*,
and automatically discovered and loaded via Karaf Boot SPI.

The Karaf Services can be configured via the properties, using the service name as prefix.

You can configure launcher in `karaf.json`:

```
    "properties": {
        "osgi.storageDirectory": "/path/to/storage",
        "spring-boot.cache": "/path/to/cache
    },
```

### Profiles

Karaf Profiles allows you to define a dependencies set that applications can use.
It allows you to override application dependencies at runtime.

The profiles are configured in `karaf.json` (`KarafConfig`) and loaded by Karaf Boot:

```
"profiles": [
    {
      "name": "myprofile",
      "properties": {
        "foo": "bar"
      },
      "classloader": {
        "order": "PARENT_FIRST",
        "priorities": [ { "type": "RESOURCE", "pattern": "org/slf4j/LoggerFactory" }, {"type": "CLASS", "name": "com.foo.[*]"} ],
        "urls": [ "/path/to/jar/file.jar", "/path/to/folder" ]
      }
    }
  ],
```

### Applications

The applications in `karaf.json` (`KarafConfig`) are automatically started by Karaf Boot at runtime.

By default, Karaf tries to find the applications manager to use for the application. However, you can "force" the manager
(runtime) to use by providing manager name.
It's also possible to override application dependencies by providing profiles.
You can also pass some properties for Karaf service specifically to the application.

```
"applications": [
    {
      "url": "/path/to/app/spring-boot.jar",
      "profiles": [ "myprofile" ],
      "services": [
        {
          "name": "spring-boot",
          "properties": {
            "enableHttp": true,
            "enablePrometheus": true
          }
        }
      ]
    },
    {
      "url": "/path/to/osgi/bundle.jar",
      "type": "osgi"
    }
  ]
```

## Distributions

As for Apache Karaf 4.x, you can find some ready to use distribution.

## Run

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

Karaf is launching all you describe in the `karaf.json`.
