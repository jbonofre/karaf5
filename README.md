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

Karaf boot is the main runtime orchestrator. It basically loads Karaf Services via SPI, and it's configured via
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

### Karaf Services

A Karaf Services is a class implementing the `org.apache.karaf.boot.spi.Service` interface, and loaded via `META-INF/services/org.apache.karaf.boot.spi.Service`, containing the FQDN of the implementation class.

The service doesn't have to implement any method by default. Optionally, you can define the following methods:

```java
public class MyService implements org.apache.karaf.boot.spi.Service {
    
    @Override
    public String name() {
        // return the service name
        return "my-service";
    }
    
    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        // callback method, called when the service is registered in the Karaf Service Registry
        // you can interact with the Karaf Service Registry `serviceRegistry` here, looking for services, etc
    }
    
    @Override
    public int priority() {
        // return the service priority, default is 1000. Lower priority are started before higher priority.
        return 1001;
    }
    
}

```

### Runtime

Karaf Boot provides a runtime with:
* a launcher
* a service registry (`ServiceRegistry`) where all services will be located
* a config service (`KarafConfigService`) loads Karaf config
* a lifecycle service (`KarafLifeCycleService`) is responsible to callback start and stop methods from the services

Then, `org.apache.karaf.boot.Karaf` launcher can start (`Karaf.builder().build().start()`) all Karaf services located in the classloader, you can repackage all dependencies (jar) in a single uber jar.

Karaf itself provides several "core" services:
* `classpath:` protocol handler
* archive extractor
* JSON configuration loader
* Properties configuration loader
* welcome banner
* ...

Services can also deploy "third party" applications, for instance:
* OSGi application manager is able to deploy OSGi applications (bundles, Karaf 4 features)
* Spring Boot application manager is able to deploy Spring Boot applications

Karaf Services can be configured via the properties, using the service name as prefix.

You can configure launcher in `karaf.json`:

```
    "properties": {
        "osgi.storageDirectory": "/path/to/storage",
        "spring-boot.cache": "/path/to/cache
    },
```

Each service is responsible to retrieve the `KarafConfig` service from the service registry, and get the properties.

### Third party applications

When you use a third party application manager service, you can define the applications you want to deploy via `KarafConfig` service.

For instance, you can use the following `karaf.json` configuration file:

```json
{
  "properties": {
    "osgi.storageDirectory": "path/to/store",
    "osgi.cache": "path/to/cache"
  },
  "applications": [
    {
      "url": "https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar",
      "type": "osgi"
    }
  ]
}
```

Here, you can see how to configure and deploy `commons-lang-2.6.jar` in the OSGi application manager.

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
