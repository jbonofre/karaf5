# Services & registry

Minho registry and services are the core of the runtime. Services provided additional features in your runtime, like supporting loading appliccations, starting a HTTP container, etc.

Basically, a runtime is modules loader, easy module register services in the Minho service registry.

## Minho service

A service is a "classic" Java SPI: a service is described by a Java interface.

Any module in a Minho runtime can use and register services.

A service can:

* have a name (default is class simple name)
* have a priority (if you want to define an order between services or defining a kind of starting stages by grouping services).

## Service registry & loading services

The Minho Service Registry stores any Minho services. The registry is basically a `Map<Class<?>, Service>`.

It automatically loads Minho services: the services loading can be "ordered" by priority in the registry.

Minho Service Registry is unique in a runtime and shared by any services/applications present in the runtime.

The Minho service registration give you access to the Service Registry, and you can interact with it. You also have util method to get the Service Registry without registrering a service.

Once you have the Service Registry, you can:

* get any service from the registry (including the Minho core services)
* get all services from a certain type (interface)
* registry your own services
* unregister any service

Minho Service Registry service class is `org.apache.karaf.minho.boot.service.ServiceRegistry`:

```java
public class ServiceRegistry implements AutoCloseable {

  /**
   * Retrieve all services from the registry.
   *
   * @return all services from the registry.
   */
  public Map<Class<?>, Service> getAll() { ... }

    /**
     * Retrieve a service from the registry.
     *
     * @param serviceClass the service class identifying the service.
     * @param <T>          the service type.
     * @return the service instance from the registry.
     */
    public <T> T get(final Class<T> serviceClass) { ... }

    /**
     * Lookup a stream of service by type.
     *
     * @param serviceClass looked up type.
     * @param <T>          expected type.
     * @return the instances matching the requested type.
     */
    public <T> Stream<Service> findByType(final Class<T> serviceClass) { ... }

    /**
     * Register a service in the registry.
     *
     * @param service the service to add in the registry.
     * @return true if the service has been added, false else.
     */
    public boolean add(final Service service) { ... }

    /**
     * Remove a service from the registry.
     *
     * @param service the service to remove.
     */
    public void remove(final Service service) { ... }

    /**
     * Close (stop) the service registry.
     */
    @Override
    public void close() { ... }

    /**
     * Start the service registry.
     */
    public void start() { ... }
    

}
```

Minho Service Registry is itself a Minho service.

## Core services

Any Minho runtime includes minho-boot module (runtime core). Minho Boot provides the core services, provided out of the box for you, available in the service registry.

### Configuration service

The configuration service is the main configuration store for the runtime itself, but can also be used by the modules to store their own configurations.

`ConfigService` provides access to the `Config` resources:

* _Properties_ is key/value pair where you can store anything. These properties can be populated by system properties or environment variables.
* _Profiles_ store libaries that you want to share accross several modules in the runtime.
* _Applications_ define the properties of your applications modules.

You can interact with the Minho configuration service programmatically, but you can also populate the configuration via other Minho services, like Minho JSON Configuration or Minho Properties Configuration services. You can also create your own service to populate and interact with the core Minho configuration service.

### Lifecycle service

Minho Lifecycle service allows you to "hook" your own services into the runtime lifecycle. It allows to call service method during runtime start and stop.

The lifecycle service is registered in the Minho service registry, it means that starting the service registry is actually starting the lifecycle service.

You basically have two callback hooks you can create in your service:

* calling one of your service method when the service is registered in the service registry. For that, you just have to implement `onRegister(ServiceRegistry serviceRegistry)` method in your service:
  ```java
  public void onRegister(ServiceRegistry serviceRegistry) {
    ...
  }
  ```
* once you have the `ServiceRegistry`, you can add your service methods in the `LifeCycleService`, in the `onStart()` and/or `onShutdown()` phases:
  ```java
  LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);
  lifeCycleService.onStart(() -> {
    // start
  });
  lifeCycleService.onShutdown(() -> {
    // shutdow 
  });
  ```

The runtime start cinetic is:

1. Load and register all services
2. Once all services are registered (all `onRegister()` methods executed), then, the `LifeCycleService#start()` method is executed, calling all methods registered `onStart()`.

On the other hand, the runtime shutdown cinetic is:

1. execute `LifeCycleService#close()` method, calling all methods registered `onShutdown()`
2. actually shutdown the runtime.

### Classloader service

## Module services

Minho also provides additional services that you can use "out of the box" to easily create your own services.

You don't have to implement everything, Minho services help you. Loading and using these services is very simple: just add the services in your dependencies (for instance in your Maven `pom.xml` dependencies) and in the runtime `dependencies` (classpath).

### Banner (minho:minho-banner)

Banner service just display a fancy message when the runtime start. To use Minho Banner service, you just have to add `minho:minho-banner` module in your runtime `dependencies` (or classpath).

You can customize the message using the `MINHO_BANNER` environment variable, `minho.banner` system property, or even `banner.txt` file in your runtime classpath.

For instance, here's a `minho-build.json` to add the Banner Service in a runtime:

```json
{
  "name": "my-runtime",
  "properties": {},
  "dependencies": [
    "minho:minho-boot", "minho:minho-banner"
  ]
}
```

If we start `my-runtime`, you can see the startup message:

```bash
$ java -jar minho-boot-1.0-SNAPSHOT.jar
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.Main main
INFO: Starting runtime in exploded mode
Minho lib: /home/jbonofre/Workspace/karaf5/tooling/cli/target/minho-build-1.0-SNAPSHOT/bin/my-runtime
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding config-service service (-2147483647)
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding lifecycle-service service (-1000)
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding classloader-service service (-1000)
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding minho-banner-service service (2147483647)
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFO:
 __  __ _       _
|  \/  (_)_ __ | |__   ___
| |\/| | | '_ \| '_ \ / _ \
| |  | | | | | | | | | (_) |
|_|  |_|_|_| |_|_| |_|\___/
  Apache Karaf Minho 1.x

Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Oct 17, 2022 10:14:47 AM org.apache.karaf.minho.boot.service.LifeCycleService start
INFO: Starting lifecycle service

```

Now, we can try to change the message by settings the `MINHO_BANNER` environment variable:

```bash
$ export MINHO_BANNER="Hello Minho"
```

and we can see now our message when launching `my-runtime`:

```bash
$ java -jar minho-boot-1.0-SNAPSHOT.jar
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.Main main
INFO: Starting runtime in exploded mode
Minho lib: /home/jbonofre/Workspace/karaf5/tooling/cli/target/minho-build-1.0-SNAPSHOT/bin/my-runtime
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding config-service service (-2147483647)
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding lifecycle-service service (-1000)
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding classloader-service service (-1000)
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding minho-banner-service service (2147483647)
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFO: Hello Minho
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Oct 17, 2022 10:18:29 AM org.apache.karaf.minho.boot.service.LifeCycleService start
INFO: Starting lifecycle service
```

### Camel (minho:minho-camel)

You want to run [Apache Camel](https://camel.apache.org) routes in your Minho runtime, Minho Camel service can help you. To use Minho Camel service, you just have to add `minho:minho-camel` module in your runtime `dependencies` (or classpath).

The Minho Camel service automatically creates a `CamelContext` for you. Then, you can directly create the Camel routes as Minho service and they will be automatically added to the `CamelContext`.

For instance, your Camel route service can look like:

```java
public class MyRouteBuilder extends RouteBuilder implements Service {

    @Override
    public String name() {
        return "test-route";
    }

    @Override
    public void configure() throws Exception {
        from("direct:test").id(name()).to("log:test").to("mock:test");
    }

}
```

Don't worry about the version, the Minho service modules provide all "verified" dependencies running in Minho. For instance, if you use Apache Maven, you just need these dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>my.group.id</groupId>
    <artifactId>my-route</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.apache.karaf.minho</groupId>
            <artifactId>minho-boot</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.apache.karaf.minho</groupId>
            <artifactId>minho-camel</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

Using Apache Maven, just build your route project with `mvn clean install`.

Now, you can create the following `minho-build.json`, optionally overriding Camel dependencies: 

```json
{
  "name": "my-runtime",
  "properties": {
    "include.transitive": "true"
  },
  "dependencies": [
    "minho:minho-boot",
    "minho:minho-camel",
    "mvn:org.apache.camel/camel-core/3.17.0",
    "mvn:my.group.id/my-route/1.0-SNAPSHOT"
  ]
}
```

_Instead of using `minho-build` CLI, you can also use directly the `minho-maven-plugin` to easily create the runtime based on your Maven project._

When we launch `my-runtime`, we can see the Camel route started:

```bash
$ java -jar minho-boot-1.0-SNAPSHOT.jar
2022-10-17 10:49:25.084680000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry add ] : Adding config-service service (-2147483647)
2022-10-17 10:49:25.106504000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry add ] : Adding lifecycle-service service (-1000)
2022-10-17 10:49:25.106886000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry add ] : Adding test-route service (1000)
2022-10-17 10:49:25.107205000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry add ] : Adding minho-camel-service service (1001)
2022-10-17 10:49:25.107482000 INFO [ org.apache.karaf.minho.camel.CamelService onRegister ] : Creating default CamelContext
2022-10-17 10:49:25.177597000 INFO [ org.apache.karaf.minho.camel.CamelService onRegister ] : Looking for RouteBuilder in the registry
2022-10-17 10:49:25.200343000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2 ] : Starting services
2022-10-17 10:49:25.200957000 INFO [ org.apache.karaf.minho.boot.service.LifeCycleService start ] : Starting lifecycle service
2022-10-17 10:49:25.278259000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStartContext ] : Apache Camel 3.17.0 (default-camel-context) is starting
2022-10-17 10:49:25.289817000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] : Routes startup (total:1 started:1)
2022-10-17 10:49:25.290242000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] :     Started test-route (direct://test)
2022-10-17 10:49:25.290532000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] : Apache Camel 3.17.0 (default-camel-context) started in 127ms (build:39ms init:76ms start:12ms)
2022-10-17 10:49:25.304808000 INFO [ org.apache.camel.spi.CamelLogger log ] : Exchange[ExchangePattern: InOnly, BodyType: String, Body: Hello world!]
2022-10-17 10:49:25.309058000 INFO [ org.apache.camel.component.mock.MockEndpoint assertIsSatisfied ] : Asserting: mock://test is satisfied
2022-10-17 10:49:25.309417000 INFO [ org.apache.karaf.minho.boot.service.ServiceRegistry close ] : Closing service registry
2022-10-17 10:49:25.310262000 INFO [ org.apache.karaf.minho.boot.service.LifeCycleService close ] : Stopping lifecycle service
2022-10-17 10:49:25.311000000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStop ] : Apache Camel 3.17.0 (default-camel-context) shutting down (timeout:45s)
2022-10-17 10:49:25.323954000 INFO [ org.apache.camel.spi.CamelLogger log ] : Routes stopped (total:1 stopped:1)
2022-10-17 10:49:25.324325000 INFO [ org.apache.camel.spi.CamelLogger log ] :     Stopped test-route (direct://test)
2022-10-17 10:49:25.326111000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStop ] : Apache Camel 3.17.0 (default-camel-context) shutdown in 16ms (uptime:48ms)
```

### CDI Modules manager (minho:minho-cdi)

_Coming soon!_

### Classpath (minho:minho-classpath)

Minho Classpath service provides a protocol handler allowing you to use `classpath:` protocol in your modules. To use Minho Classpath service, you just have to add `minho:minho-classpath` module in your runtime `dependencies` (or classpath).

You just need to have `minho:minho-classpath` service in your `minho-build.json` `dependencies`.

### Config JSON (minho:minho-config-json)

Minho Config JSON allows you to populate the Minho Config Service using a JSON file. To use Minho Config JSON service, you just have to add `minho:minho-config-json` module in your runtime `dependencies` (or classpath).

As soon as you add `minho:minho-config-json` module in your runtime, you can also include a `minho.json` file.

_You can also define the location of the `minho.json` file using `MINHO_CONFIG` environment variable or `minho.config` system property._

The `minho.json` populates the `ConfigService`, with `properties`, `profiles`, `applications`. For instance:

```json
{
  "properties": {
    "foo": "bar",
    "lifecycle.enabled": "true",
    "log.patternLayout": "%m %n",
  },
  "profiles": [
    {
      "name": "myprofile",
      "properties": {
        "foo": "bar"
      },
      "urls": [ "/path/to/jar/file.jar", "/path/to/folder" ]
    }
  ],
  "applications": [
    {
      "name": "spring-boot-foo",
      "version": "1.0-SNAPSHOT",
      "url": "/path/to/app/spring-boot.jar",
      "profile": "myprofile",
      "type": "spring-boot",
      "properties": {
        "enableHttp": "true",
        "enablePrometheus": "true"
      }
    },
    {
      "name": "bundle-bar",
      "url": "/path/to/osgi/bundle.jar",
      "type": "osgi"
    }
  ]
}
```

### Config Properties (minho:minho-config-properties)

Minho Config Properties allows you to populate the Minho Config Service using a Properties file. To use Minho Config Properties service, you just have to add `minho:minho-config-properties` module in your runtime `dependencies` (or classpath).

As soon as you add `minho:minho-config-properties` module in your runtime, you can also include a `minho.properties` file.

_You can also define the location of the `minho.properties` file using `MINHO_CONFIG` environment variable or `minho.config` system property._

The `minho.properties` populates the `ConfigService`, with `properties`, `profiles`, `applications`. For instance:

```properties

foo=bar
lifecycle.enabled=true
log.patternLayout=%m %n

application.app1.url=/path/to/app/spring-boot.jar
application.app1.profiles=myprofile
application.app1.type=spring-boot
application.app1.enableHttp=true
application.app1.enablePrometheus=true

application.app2.url=/path/to/osgi/bundle.jar
application.app2.type=osgi
```

### Extractor (minho:minho-extractor)

Minho Extractor service is able to extract/copy folder to the location of your choice. To use Minho Extractor service, you just have to add `minho:minho-extractor` module in your runtime `dependencies` (or classpath).

The extraction process is performed at register time.

The Extractor service uses properties (from the `Config` service):

* `extractor.target` property (default is the current folder) is the target directory where the resources will be copied/extractor.
* `extractor.sources` property (default is `resources` folder) is a list of source directories where to read the resources.

### HTTP (minho:minho-http)

Minho HTTP service starts a HTTP container (powered by Jetty) where you can deploy your servlets. To use Minho HTTP service, you just have to add `minho:minho-http` module in your runtime `dependencies` (or classpath).

You can configure the HTTP container via `Config` properties:

* `http.maxThreads`
* `http.minThreads`
* `http.idleTimeout`
* `http.acceptors`
* `http.selectors`
* `http.port`
* `http.host`
* `http.acceptQueueSize`

### JMX (minho:minho-jmx)

_Coming soon!_

### JPA (minho:minho-jpa)

Minho JPA service provides a JPA engine (powered by OpenJPA) mapping your data beans with a database. To use Minho JPA service, you just have to add `minho:minho-jpa` module in your runtime `dependencies` (or classpath).

Minho JPA service start the JPA engine at register time. Then, you can get the JPA service to get the JPA `EntityManager` to register your JPA entities.

### OSGi modules manager (minho:minho-osgi)

Minho OSGi Modules manager add support of OSGi bundles in your runtime. To enable OSGi support, you have to add `minho:minho-osgi` module in your runtime `dependencies` (or classpath).

It uses `Config` service to retrieve the OSGi `applications` to be deployed and started in the runtime.

For instance, using `minho-config-json` module, you can configure the list of OSGi bundles part of your runtime:

```json
{
	"applications": [
		{
			"url": "https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar",
			"type": "osgi"
	]
}
```

_NOTE: Minho OSGi Modules Manager tries to automatically detect when an artifact is an OSGi bundle (so `type` is optional). However, defining application `type` as `osgi` is recommended to force Minho OSGi modules manager to handle the artifact._

Here, `commons-lang-2.6.jar` will be installed as an OSGi bundle into the Minho OSGi modules manager.

The location (`url`) of the OSGi modules can be remote (using `http`, `https`, `mvn`, ...) or local (`file`). 
Of course, you can also package the OSGi module as part of the runtime `dependencies`, so included in the runtime "out of the box".

In details, Minho OSGi modules manager starts an OSGi framework (Apache Felix) and manage the bundles inside the framework.

You can "tweak" OSGi framework with the following properties:

* `osgi.storageDirectory`
* `osgi.clearCache`
* `osgi.startLevel`
* `osgi.bundleStartLevel`
* `osgi.logLevel`
* `osgi.cache`

### Microprofile modules manager (minho:minho-microprofile)

_Coming soon!_

### REST (minho:minho-rest)

Minho REST service provides JAX-RS REST API support (powered by Jersey). To use Minho REST service, you just have to add `minho:minho-rest` module in your runtime `dependencies` (or classpath).

Minho REST service uses `Config` `rest.packages` property to define the packages where to look for JAX-RS resources.

Optionally, Minho REST service can use `rest.path` property to define where the REST resources are exposed (by default it's `/rest/*`).

### Spring Boot modules manager (minho:minho-spring-boot)

Minho runtime can collocate multiple Spring Boot modules (applications). To add Spring Boot support, you have to load `minho:minho-spring-boot` module in your runtime `dependencies` (or classpath).

It uses `Config` service to retrieve the Spring Boot `applications` to be started in the runtime.

For instance, using `minho-config-json`, you can configure the list of Spring Boot modules part of your runtime:

```json
{
	"applications': [
		{
			"url": "file:./lib/spring-petclinic.jar",
			"type": "spring-boot"
		}
	]
}
```

_NOTE: Minho Spring Boot Modules Manager tries to automatically detect a Spring Boot artifact. However, defining application `type` as `spring-boot` is recommended to force Minho Spring Boot modules manager to handle the artifact._

Here, we deploy the Spring Boot PetClinic demo application in our runtime.

The location (`url`) of the Spring Boot modules can be remote (using `http`, `https`, `mvn`, ...) or local (`file`). 
Of course, you can also package the Spring Boot module as part of the runtime `dependencies`, so included in the runtime "out of the box".

### Shell (minho:minho-shell)

Minho Shell provides a full Unix like shell environment where you can interact your runtime: listing services from the registry, manipulating application modules, ...

_Coming soon!_

## Create a service

You can create your own Minho service. It's very easy.

To create your Minho service, you just have to write a class implementing `org.apache.karaf.minho.boot.spi.Service` interface:

```java


/**
 * Generic Minho Service.
 */
public interface Service {

    int DEFAULT_PRIORITY = 1000;

    /**
     * Register a service in the registry.
     * @param serviceRegistry the service registry.
     * @throws Exception if register fails.
     */
    default void onRegister(final ServiceRegistry serviceRegistry) throws Exception {
        // no-op, service
    }

    /**
     * Retrieve the service priority (allow services sorting).
     * Default is 1000.
     * @return the service priority.
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * Retrieve the service name.
     * Default is the service simple class name.
     * @return the service name.
     */
    default String name() {
        return getClass().getSimpleName().toLowerCase(Locale.ROOT).replaceAll("Service", "");
    }

    /**
     * Add properties specific to a service that could be used by other services during lookup.
     * Default is empty properties.
     *
     * @return the service properties.
     */
    default Properties properties() {
        return new Properties();
    }

}
```

Here's a very simple `MyService`:

```java
package minho.my.service;

import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

public class MyService implements Service {

  @Override
  public String name() {
    return "my-service";
  }

  @Override
  public void onRegister(ServiceRegistry serviceRegistry) {
    System.out.println("Hello Minho !");
  }

}
```

Then, you have to define your service implementation (for the Service Loader) in `META-INF/services/org.apache.karaf.minho.boot.spi.Service` file:

```
minho.my.service.MyService
```

You can use the following Maven `pom.xml` to build your service jar:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>my.group.id</groupId>
    <artifactId>minho-my-service</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.apache.karaf.minho</groupId>
            <artifactId>minho-boot</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

You can create a runtime with your service using the following `minho-build.json` file:

```json
{
	"name": "my-runtime",
	"dependencies": [
		"minho:minho-boot",
		"mvn:my.group.id/minho-my-service/1.0-SNAPSHOT"
	]
}
```

Then, you can launch the runtime and we see our service registered:

```bash
java -jar minho-boot-1.0-SNAPSHOT.jar
...
INFOS: Adding my-service service (1000)
Hello Minho !
....
```

Let's now extend the service by adding callback in the Minho Lifecycle service.

To "hook" in the lifecycle service, we have to:

* get the `LifecycleService` from the `ServiceRegistry` in the `onRegister()` method of our service
* register callback method in `LifeCycleService` `onStart()` and `onShutdown()` hooks

Basically, `MyService` could look like:

```java
package minho.my.service;

import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

public class MyService implements Service {

  @Override
  public String name() {
    return "my-service";
  }

  @Override
  public void onRegister(ServiceRegistry serviceRegistry) {
    System.out.println("Hello Minho !");
    LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);
    lifeCycleService.onStart(() -> {
      System.out.println("Hey, my-service is starting !");
    });
    lifeCycleService.onShutdown(() -> {
      System.out.println("Hey, my-service is shutting down!");
    });
  }

}
```

After updating `my-runtime`, at launch we can see the start/shudown hooks called:

```bash
$ java -jar minho-boot-1.0-SNAPSHOT.jar                                                                                                                                             
...
INFO: Adding my-service service (1000)
Hello Minho !
...
INFO: Starting lifecycle service
Hey, my-service is starting !
```
