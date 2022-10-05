# Services & registry

K5 registry and services are the core of the runtime. Services provided additional features in your runtime, like supporting loading appliccations, starting a HTTP container, etc.

Basically, a runtime is modules loader, easy module register services in the K5 service registry.

## K5 service

A service is a "classic" Java SPI: a service is described by a Java interface.

Any module in a K5 runtime can use and register services.

A service can:

* have a name (default is class simple name)
* have a priority (if you want to define an order between services or defining a kind of starting stages by grouping services).

## K5 registry & loading services

The K5 Service Registry stores any K5 services. The registry is basically a `Map<Class<?>, Service>`.

It automatically loads K5 services: the services loading can be "ordered" by priority in the registry.

K5 Service Registry is unique in a runtime and shared by any services/applications present in the runtime.

The K5 service registration give you access to the Service Registry, and you can interact with it. You also have util method to get the K5 Service Registry without registrering a service.

Once you have the Service Registry, you can:

* get any service from the registry (including the K5 core services)
* get all services from a certain type (interface)
* registry your own services
* unregister any service

K5 Service Registry service class is `org.apache.karaf.boot.service.ServiceRegistry`:

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

K5 Service Registry is itself a K5 service.

## Core services

Any K5 runtime includes karaf-boot module (runtime core). Karaf Boot provides the core services, provided out of the box for you, available in the service registry.

### Configuration service

The configuration service is the main configuration store for the runtime itself, but can also be used by the modules to store their own configurations.

`KarafConfigService` provides access to the `KarafConfig` resources:

* _Properties_ is key/value pair where you can store anything. These properties can be populated by system properties or environment variables.
* _Profiles_ store libaries that you want to share accross several modules in the runtime.
* _Applications_ define the properties of your applications modules.

You can interact with the K5 configuration service programmatically, but you can also populate the configuration via other K5 services, like K5 JSON Configuration or K5 Properties Configuration services. You can also create your own service to populate and interact with the core K5 configuration service.

### Lifecycle service

K5 Lifecycle service allows you to "hook" your own services into the runtime lifecycle. It allows to call service method during runtime start and stop.

The lifecycle service is registered in the K5 service registry, it means that starting the service registry is actually starting the lifecycle service.

You basically have two callback hooks you can create in your service:

* calling one of your service method when the service is registered in the service registry. For that, you just have to implement `onRegister(ServiceRegistry serviceRegistry)` method in your service:
  ```java
  public void onRegister(ServiceRegistry serviceRegistry) {
    ...
  }
  ```
* once you have the `ServiceRegistry`, you can add your service methods in the `KarafLifeCycleService`, in the `onStart()` and/or `onShutdown()` phases:
  ```java
  KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
  karafLifeCycleService.onStart(() -> {
    // start
  });
  karafLifeCycleService.onShutdown(() -> {
    // shutdow 
  });
  ```

The runtime start cinetic is:

1. Load and register all services
2. Once all services are registered (all `onRegister()` methods executed), then, the `KarafLifeCycleService#start()` method is executed, calling all methods registered `onStart()`.

On the other hand, the runtime shutdown cinetic is:

1. execute `KarafLifeCycleService#close()` method, calling all methods registered `onShutdown()`
2. actually shutdown the runtime.

### Classloader service

## Module services

K5 also provides additional services that you can use "out of the box" to easily create your own services.

You don't have to implement everything, K5 services help you. Loading and using these services is very simple: just add the services in your dependencies (for instance in your Maven `pom.xml` dependencies) and in the runtime `dependencies` (classpath).

### Banner (k5:karaf-banner)

Banner service just display a fancy message when the runtime start. To use K5 Banner service, you just have to add `k5:karaf-banner` module in your runtime `dependencies` (or classpath).

You can customize the message using the `KARAF_BANNER` environment variable or `karaf.banner` system property.

For instance, here's a `karaf-build.json` to add the Banner Service in a runtime:

```json
{
  "name": "my-runtime",
  "properties": {},
  "dependencies": [
    "k5:karaf-boot", "k5:banner"
  ]
}
```

If we start `my-runtime`, you can see the startup message:

```bash
$ java -jar karaf-boot-5.0-SNAPSHOT.jar
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test/my-runtime
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding karaf-banner service (2147483647)
oct. 02, 2022 12:05:25 PM org.apache.karaf.banner.WelcomeBannerService onRegister
INFOS:
        __ __                  ____
       / //_/____ __________ _/ __/
      / ,<  / __ `/ ___/ __ `/ /_
     / /| |/ /_/ / /  / /_/ / __/
    /_/ |_|\__,_/_/   \__,_/_/

  Apache Karaf 5.x

oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 02, 2022 12:05:25 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

Now, we can try to change the message by settings the `KARAF_BANNER` environment variable:

```bash
$ export KARAF_BANNER="Hello K5"
```

and we can see now our message when launching `my-runtime`:

```bash
$ java -jar karaf-boot-5.0-SNAPSHOT.jar
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test/my-runtime
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding karaf-banner service (2147483647)
oct. 02, 2022 12:07:58 PM org.apache.karaf.banner.WelcomeBannerService onRegister
INFOS: Welcome K5
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 02, 2022 12:07:58 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

### Camel (k5:karaf-camel)

You want to run [Apache Camel](https://camel.apache.org) routes in your K5 runtime, K5 Camel service can help you. To use K5 Camel service, you just have to add `k5:karaf-camel` module in your runtime `dependencies` (or classpath).

The K5 Camel service automatically creates a `CamelContext` for you. Then, you can directly create the Camel routes as K5 service and they will be automatically added to the `CamelContext`.

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

Don't worry about the version, the K5 service modules provide all "verified" dependencies running in K5. For instance, if you use Apache Maven, you just need these dependencies:

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
            <groupId>org.apache.karaf</groupId>
            <artifactId>karaf-boot</artifactId>
            <version>5.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.apache.karaf</groupId>
            <artifactId>karaf-camel</artifactId>
            <version>5.0-SNAPSHOT</version>
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

Now, you can create the following `karaf-build.json`, optionally overriding Camel dependencies: 

```json
{
  "name": "my-runtime",
  "properties": {
    "include.transitive": "true"
  },
  "dependencies": [
    "k5:boot",
    "k5:camel",
    "mvn:org.apache.camel/camel-core/3.17.0",
    "mvn:my.group.id/my-route/1.0-SNAPSHOT"
  ]
}
```

_Instead of using `karaf-build` CLI, you can also use directly the `karaf-maven-plugin` to easily create the runtime based on your Maven project._

When we launch `my-runtime`, we can see the Camel route started:

```bash
$ java -jar karaf-boot-5.0-SNAPSHOT.jar
2022-10-02 18:46:01.774343000 INFO [ org.apache.karaf.boot.service.ServiceRegistry add ] : Adding config service (-2147483647)
2022-10-02 18:46:01.812901000 INFO [ org.apache.karaf.boot.service.ServiceRegistry add ] : Adding lifecycle service (-1000)
2022-10-02 18:46:01.813346000 INFO [ org.apache.karaf.boot.service.ServiceRegistry add ] : Adding test-route service (1000)
2022-10-02 18:46:01.813705000 INFO [ org.apache.karaf.boot.service.ServiceRegistry add ] : Adding karaf-camel service (1001)
2022-10-02 18:46:01.814024000 INFO [ org.apache.karaf.camel.CamelService onRegister ] : Creating default CamelContext
2022-10-02 18:46:01.934149000 INFO [ org.apache.karaf.camel.CamelService onRegister ] : Looking for RouteBuilder in the registry
2022-10-02 18:46:01.975948000 INFO [ org.apache.karaf.boot.service.ServiceRegistry lambda$start$2 ] : Starting services
2022-10-02 18:46:01.976382000 INFO [ org.apache.karaf.boot.service.KarafLifeCycleService start ] : Starting lifecycle service
2022-10-02 18:46:02.140007000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStartContext ] : Apache Camel 3.17.0 (default-camel-context) is starting
2022-10-02 18:46:02.163201000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] : Routes startup (total:1 started:1)
2022-10-02 18:46:02.163682000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] :     Started test-route (direct://test)
2022-10-02 18:46:02.164197000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext logStartSummary ] : Apache Camel 3.17.0 (default-camel-context) started in 245ms (build:60ms init:161ms start:24ms)
2022-10-02 18:46:02.190766000 INFO [ org.apache.camel.spi.CamelLogger log ] : Exchange[ExchangePattern: InOnly, BodyType: String, Body: Hello K5!]
2022-10-02 18:46:02.196397000 INFO [ org.apache.camel.component.mock.MockEndpoint assertIsSatisfied ] : Asserting: mock://test is satisfied
2022-10-02 18:46:02.196985000 INFO [ org.apache.karaf.boot.service.ServiceRegistry close ] : Closing service registry
2022-10-02 18:46:02.198728000 INFO [ org.apache.karaf.boot.service.KarafLifeCycleService close ] : Stopping lifecycle service
2022-10-02 18:46:02.200030000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStop ] : Apache Camel 3.17.0 (default-camel-context) shutting down (timeout:45s)
2022-10-02 18:46:02.221227000 INFO [ org.apache.camel.spi.CamelLogger log ] : Routes stopped (total:1 stopped:1)
2022-10-02 18:46:02.221589000 INFO [ org.apache.camel.spi.CamelLogger log ] :     Stopped test-route (direct://test)
2022-10-02 18:46:02.224676000 INFO [ org.apache.camel.impl.engine.AbstractCamelContext doStop ] : Apache Camel 3.17.0 (default-camel-context) shutdown in 25ms (uptime:84ms)
```

### CDI Modules manager (k5:karaf-cdi)

_Coming soon!_

### Classpath (k5:karaf-classpath)

K5 Classpath service provides a protocol handler allowing you to use `classpath:` protocol in your modules. To use K5 Classpath service, you just have to add `k5:karaf-classpath` module in your runtime `dependencies` (or classpath).

You just need to have `k5:karaf-classpath` service in your `karaf-build.json` `dependencies`.

### Configuration JSON (k5:karaf-config-json)

K5 Configuration JSON allows you to populate the K5 Configuration Service using a JSON file. To use K5 Configuration JSON service, you just have to add `k5:karaf-config-json` module in your runtime `dependencies` (or classpath).

As soon as you add `k5:karaf-config-json` module in your runtime, you can also include a `karaf.json` file.

_You can also define the location of the `karaf.json` file using `KARAF_CONFIG` environment variable or `karaf.config` system property._

The `karaf.json` populates the `KarafConfigService`, with `properties`, `profiles`, `applications`. For instance:

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

### Configuration Properties (k5:karaf-config-properties)

K5 Configuration Properties allows you to populate the K5 Configuration Service using a Properties file. To use K5 Configurtion Properties service, you just have to add `k5:karaf-config-properties` module in your runtime `dependencies` (or classpath).

As soon as you add `k5:karaf-config-properties` module in your runtime, you can also include a `karaf.properties` file.

_You can also define the location of the `karaf.properties` file using `KARAF_CONFIG` environment variable or `karaf.config` system property._

The `karaf.properties` populates the `KarafConfigService`, with `properties`, `profiles`, `applications`. For instance:

```properties

=bar
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

### Extractor (k5:karaf-extractor)

K5 Extractor service is able to extract/copy folder to the location of your choice. To use K5 Extractor service, you just have to add `k5:karaf-extractor` module in your runtime `dependencies` (or classpath).

The extraction process is performed at register time.

The Extractor service uses properties (from the `KarafConfig` service):

* `extractor.target` property (default is the current folder) is the target directory where the resources will be copied/extractor.
* `extractor.sources` property (default is `resources` folder) is a list of source directories where to read the resources.

### HTTP (k5:karaf-http)

K5 HTTP service starts a HTTP container (powered by Jetty) where you can deploy your servlets. To use K5 HTTP service, you just have to add `k5:karaf-http` module in your runtime `dependencies` (or classpath).

You can configure the HTTP container via `KarafConfig` properties:

* `http.maxThreads`
* `http.minThreads`
* `http.idleTimeout`
* `http.acceptors`
* `http.selectors`
* `http.port`
* `http.host`
* `http.acceptQueueSize`

### JMX (k5:karaf-jmx)

_Coming soon!_

### JPA (k5:karaf-jpa)

K5 JPA service provides a JPA engine (powered by OpenJPA) mapping your data beans with a database. To use K5 JPA service, you just have to add `k5:karaf-jpa` module in your runtime `dependencies` (or classpath).

K5 JPA service start the JPA engine at register time. Then, you can get the JPA service to get the JPA `EntityManager` to register your JPA entities.

### OSGi modules manager (k5:karaf-osgi)

K5 OSGi Modules manager add support of OSGi bundles in your runtime. To enable OSGi support, you have to add `k5:karaf-osgi` module in your runtime `dependencies` (or classpath).

It uses `KarafConfig` service to retrieve the OSGi `applications` to be deployed and started in the runtime.

For instance, using `karaf-config-json` module, you can configure the list of OSGi bundles part of your runtime:

```json
{
	"applications": [
		{
			"url": "https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar",
			"type": "osgi"
	]
}
```

_NOTE: K5 OSGi Modules Manager tries to automatically detect when an artifact is an OSGi bundle (so `type` is optional). However, defining application `type` as `osgi` is recommended to force K5 OSGi modules manager to handle the artifact._

Here, `commons-lang-2.6.jar` will be installed as an OSGi bundle into the K5 OSGi modules manager.

The location (`url`) of the OSGi modules can be remote (using `http`, `https`, `mvn`, ...) or local (`file`). 
Of course, you can also package the OSGi module as part of the runtime `dependencies`, so included in the runtime "out of the box".

In details, K5 OSGi modules manager starts an OSGi framework (Apache Felix) and manage the bundles inside the framework.

You can "tweak" OSGi framework with the following properties:

* `osgi.storageDirectory`
* `osgi.clearCache`
* `osgi.startLevel`
* `osgi.bundleStartLevel`
* `osgi.logLevel`
* `osgi.cache`

### Microprofile modules manager (k5:karaf-microprofile)

_Coming soon!_

### REST (k5:karaf-rest)

K5 REST service provides JAX-RS REST API support (powered by Jersey). To use K5 REST service, you just have to add `k5:karaf-rest` module in your runtime `dependencies` (or classpath).

K5 REST service uses `KarafConfig` `rest.packages` property to define the packages where to look for JAX-RS resources.

Optionally, K5 REST service can use `rest.path` property to define where the REST resources are exposed (by default it's `/rest/*`.

### Spring Boot modules manager (k5:karaf-spring-boot)

K5 runtime can collocate multiple Spring Boot modules (applications). To add Spring Boot support, you have to load `k5:karaf-spring-boot` module in your runtime `dependencies` (or classpath).

It uses `KarafConfig` service to retrieve the Spring Boot `applications` to be started in the runtime.

For instance, using `karaf-config-json`, you can configure the list of Spring Boot modules part of your runtime:

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

_NOTE: K5 Spring Boot Modules Manager tries to automatically detect a Spring Boot artifact. However, defining application `type` as `spring-boot` is recommended to force K5 Spring Boot modules manager to handle the artifact._

Here, we deploy the Spring Boot PetClinic demo application in our runtime.

The location (`url`) of the Spring Boot modules can be remote (using `http`, `https`, `mvn`, ...) or local (`file`). 
Of course, you can also package the Spring Boot module as part of the runtime `dependencies`, so included in the runtime "out of the box".

### Shell (k5:karaf-shell)

K5 Shell provides a full Unix like shell environment where you can interact your runtime: listing services from the registry, manipulating application modules, ...

_Coming soon!_

## Create a service

You can create your own K5 service. It's very easy.

To create your K5 service, you just have to write a class implementing `org.apache.karaf.boot.spi.Service` interface:

```java


/**
 * Generic Karaf Service.
 */
public interface Service {

    int DEFAULT_PRIORITY = 1000;

    /**
     * Register a service in the Karaf registry.
     * @param serviceRegistry the Karaf service registry.
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
package k5.my.service;

import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

public class MyService implements Service {

    @Override
    public String name() {
        return "my-service";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        System.out.println("HELLLLOOOOO K5 !");
    }

}
```

Then, you have to define your service implementation (for the Service Loader) in `META-INF/services/org.apache.karaf.boot.spi.Service` file:

```
k5.my.service.MyService
```

You can use the following Maven `pom.xml` to build your service jar:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>net.nanthrax.test</groupId>
    <artifactId>k5-my-service</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.apache.karaf</groupId>
            <artifactId>karaf-boot</artifactId>
            <version>5.0-SNAPSHOT</version>
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

You can create a runtime with your service using the following `karaf-build.json` file:

```json
{
	"name": "my-runtime",
	"dependencies": [
		"k5:karaf-boot",
		"mvn:net.nanthrax.test/k5-my-service/1.0-SNAPSHOT"
	]
}
```

Then, you can launch the runtime and we see our service registered:

```bash
java -jar karaf-boot-5.0-SNAPSHOT.jar
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test/my-runtime
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding my-service service (1000)
HELLLLOOOOO K5 !
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 03, 2022 7:08:07 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

Let's now extend the service by adding callback in the K5 Lifecycle service.

To "hook" in the K5 Lifecycle service, we have to:

* get the `KarafLifecycleService` from the `ServiceRegistry` in the `onRegister()` method of our service
* register callback method in `KarafLifeCycleService` `onStart()` and `onShutdown()` hooks

Basically, `MyService` could look like:

```java
package k5.my.service;

import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;

public class MyService implements Service {

    @Override
    public String name() {
        return "my-service";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        System.out.println("Hello K5!");
        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);
        karafLifeCycleService.onStart(() -> {
            System.out.println("Hey, my-service is starting !");
        });
        karafLifeCycleService.onShutdown(() -> {
            System.out.println("Hey, my-service is shutting down!");
        });
    }

}
```

After updating `my-runtime`, at launch we can see the start/shudown hooks called:

```bash
java -jar karaf-boot-5.0-SNAPSHOT.jar                                                                                                                                             
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.Main main
INFO: Starting runtime in exploded mode
Karaf lib: /home/jbonofre/test/my-runtime
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.ServiceRegistry add
INFO: Adding config service (-2147483647)
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.ServiceRegistry add
INFO: Adding lifecycle service (-1000)
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.ServiceRegistry add
INFO: Adding classloader-service service (-1000)
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.ServiceRegistry add
INFO: Adding my-service service (1000)
Hello K5!
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Oct 04, 2022 2:01:05 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFO: Starting lifecycle service
Hey, my-service is starting !
```
