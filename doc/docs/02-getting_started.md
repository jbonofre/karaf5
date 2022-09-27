# Getting started

You will create and run a K5 runtime in a minute.

In this first run, we will create an empty (without any module) K5 runtime (using the command line) and run it.

It's the thinest runtime you can create, just starting the K5 boot part with K5 core services.

You have several options to create a K5 runtime:

* CLI (command line)
* Maven plugin
* Gradle plugin
* REST service
* K5 Creator

For this first example, we are going to use the command line.

## Installing karaf-build CLI

You can find the command line binary in `tool/cli/target` folder once you built it.
You can also directly download CLI binaries here:

* [karaf-build-5.0-SNAPSHOT.tar.gz](https://repository.apache.org/content/groups/snapshots/org/apache/karaf/tooling/karaf-build-5.0-SNAPSHOT.tar.gz)
* [karaf-build-5.0-SNAPSHOT.zip](https://repository.apache.org/content/groups/snapshots/org/apache/karaf/tooling/karaf-build-5.0-SNAPSHOT.tar.gz)

The installation of karaf-build CLI is a simple process of extracting the archive and adding `bin` folder with the `karaf-build` command to the `PATH`.

Detailed steps are:

* Have a JDK installation on your system. Either set the `JAVA_HOME` environment variable pointing to your JDK installation or have `java` executable on your `PATH`.
* Extract karaf-cli distribution archive in any directory
  ```bash
  $ unzip karaf-build-5.0-SNAPSHOT.zip
  ```
  ```bash
  $ tar zxvf karaf-build-5.0-SNAPSHOT.tar.gz
  ```
  Alternatively use your preferred archive extraction tool.
* Add the `bin` directory of the created directory `karaf-build-5.0-SNAPSHOT` to the `PATH` environment variable.
* Confirm with `karaf-build --help` in a new shell. The result should look similar to:
  ```bash
  usage: karaf-build [package|jar|archive]
  Default build action is package
  -f,--file <arg>   Location of the karaf-build.json file
  -h,--help         print this message
  ```

## karaf-build.json

`karaf-build` command use a `karaf-build.json` file describing your runtime.

In the directory of your choice, create the following `karaf-build.json` file:

```json
{
  "name": "my-runtime",
  "dependencies": [
    "k5:karaf-boot",
    "k5:karaf-banner"
  ]
}
```

We will create `my-runtime` with just the `karaf-boot` module (minimal piece for a K5 runtime) and `karaf-banner` service module (optional) that just display a fancy message at runtime startup.

We are now ready to create our runtime distribution. We have basically three options:

1. Create a "exploded" runtime folder.
2. Create a "uber jar" ready to be executed.
3. Create a archive (zip)

## Runtime package

Let's create the `my-runtime` package using `karaf-build package`:

```bash
$ karaf-build package
oct. 01, 2022 5:43:42 PM org.apache.karaf.tooling.common.Runtime <init>
INFOS: Creating Karaf runtime package in folder my-runtime
```

A `my-runtime` directory has been created. To launch `my-runtime`, you can go in the `my-runtime` directory and do `java -jar karaf-boot-5.0-SNAPSHOT.jar`:

```bash
$ cd my-runtime
$ java -jar karaf-boot-5.0-SNAPSHOT.jar
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test/my-runtime
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding karaf-banner service (2147483647)
oct. 01, 2022 5:45:09 PM org.apache.karaf.banner.WelcomeBannerService onRegister
INFOS:  
        __ __                  ____
       / //_/____ __________ _/ __/
      / ,<  / __ `/ ___/ __ `/ /_
     / /| |/ /_/ / /  / /_/ / __/
    /_/ |_|\__,_/_/   \__,_/_/

  Apache Karaf 5.x

oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

## Runtime jar

You can also create a runtime executable uber jar using `karaf-build jar`:

```bash
$ karaf-build jar
```

You now have `my-runtime/my-runtime.jar` executable jar file. This jar contains everything you need and you can directly run it:

```bash
$ java -jar my-runtime.jar
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding karaf-banner service (2147483647)
oct. 01, 2022 5:56:04 PM org.apache.karaf.banner.WelcomeBannerService onRegister
INFOS:  
        __ __                  ____
       / //_/____ __________ _/ __/
      / ,<  / __ `/ ___/ __ `/ /_
     / /| |/ /_/ / /  / /_/ / __/
    /_/ |_|\__,_/_/   \__,_/_/

  Apache Karaf 5.x

oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

## Runtime archive

Finally, you can create a zip archive with `karaf-build archive`:

```bash
$ karaf-build archive
oct. 01, 2022 6:00:27 PM org.apache.karaf.tooling.common.Runtime <init>
INFOS: Creating Karaf runtime package in folder my-runtime
oct. 01, 2022 6:00:27 PM org.apache.karaf.tooling.common.Runtime createArchive
INFOS: Creating Karaf runtime archive
```

You now have `my-runtime/my-runtime.zip` file, that you can extract in the directory of your choice:

```bash
$ mv my-runtime/my-runtime.zip temp
$ cd temp
$ unzip my-runtime.zip
Archive:  my-runtime.zip
   creating: bin/
  inflating: bin/karaf.sh
  inflating: karaf-banner-5.0-SNAPSHOT.jar
  inflating: karaf-boot-5.0-SNAPSHOT.jar
```

`my-runtime` archive contains everything you need, including simple `karaf.sh` script to start the runtime:

```bash
$ ./bin/karaf.sh
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.Main main
INFOS: Starting runtime in exploded mode
Karaf lib: /Users/jbonofre/test/temp
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding config service (-2147483647)
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding lifecycle service (-1000)
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add
INFOS: Adding karaf-banner service (2147483647)
oct. 01, 2022 6:04:23 PM org.apache.karaf.banner.WelcomeBannerService onRegister
INFOS:  
        __ __                  ____
       / //_/____ __________ _/ __/
      / ,<  / __ `/ ___/ __ `/ /_
     / /| |/ /_/ / /  / /_/ / __/
    /_/ |_|\__,_/_/   \__,_/_/

  Apache Karaf 5.x

oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.KarafLifeCycleService start
INFOS: Starting lifecycle service
```

You can see how Karaf is easy and fast to start with.

You are now ready to create your runtimes for your existing applications, or eventually create a new application using the K5 services.

You can take a look on the [examples](/examples) and available [K5 services](services).
