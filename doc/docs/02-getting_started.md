# Getting started

You will create and run a Minho runtime in a minute.

In this first run, we will create an empty (without any module) Minho runtime (using the command line) and run it.

It's the thinest runtime you can create, just starting the Minho boot part with Minho core services.

You have several options to create a Minho runtime:

* CLI (command line)
* Maven plugin
* Gradle plugin
* REST service
* K5 Creator

For this first example, we are going to use the command line.

## Installing minho-build CLI

You can find the command line binary in `tool/cli/target` folder once you built it.
You can also directly download CLI binaries here:

* [minho-build-1.0-SNAPSHOT.tar.gz](https://repository.apache.org/content/groups/snapshots/org/apache/karaf/minho/tooling/minho-build-1.0-SNAPSHOT.tar.gz)
* [minho-build-1.0-SNAPSHOT.zip](https://repository.apache.org/content/groups/snapshots/org/apache/karaf/minho/tooling/minho-build-1.0-SNAPSHOT.tar.gz)

The installation of minho-build CLI is a simple process of extracting the archive and adding `bin` folder with the `minho-build` command to the `PATH`.

Detailed steps are:

* Have a JDK installation on your system. Either set the `JAVA_HOME` environment variable pointing to your JDK installation or have `java` executable on your `PATH`.
* Extract minho-cli distribution archive in any directory
  ```bash
  $ unzip minho-build-1.0-SNAPSHOT.zip
  ```
  ```bash
  $ tar zxvf minho-build-1.0-SNAPSHOT.tar.gz
  ```
  Alternatively use your preferred archive extraction tool.
* Add the `bin` directory of the created directory `minho-build-1.0-SNAPSHOT` to the `PATH` environment variable.
* Confirm with `minho-build --help` in a new shell. The result should look similar to:
  ```bash
  usage: minho-build [package|jar|archive]
  Default build action is package
  -f,--file <arg>   Location of the minho-build.json file
  -h,--help         print this message
  ```

## minho-build.json

`minho-build` command use a `minho-build.json` file describing your runtime.

In the directory of your choice, create the following `minho-build.json` file:

```json
{
  "name": "my-runtime",
  "dependencies": [
    "minho:minho-boot",
    "minho:minho-banner"
  ]
}
```

We will create `my-runtime` with just the `minho-boot` module (minimal piece for a Minho runtime) and `minho-banner` service module (optional) that just display a fancy message at runtime startup.

We are now ready to create our runtime distribution. We have basically three options:

1. Create a "exploded" runtime folder.
2. Create a "uber jar" ready to be executed.
3. Create a archive (zip)

## Runtime package

Let's create the `my-runtime` package using `minho-build package`:

```bash
$ minho-build package
oct. 16, 2022 5:44:42 PM org.apache.karaf.minho.tooling.common.Runtime <init>
INFOS: Creating Minho runtime package in folder my-runtime
```

A `my-runtime` directory has been created. To launch `my-runtime`, you can go in the `my-runtime` directory and do `java -jar minho-boot-1.0-SNAPSHOT.jar`:

```bash
$ cd my-runtime
$ java -jar minho-boot-1.0-SNAPSHOT.jar
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.Main main
INFOS: Starting runtime in exploded mode
Minho lib: /Users/jbonofre/Workspace/karaf5/tooling/cli/target/minho-build-1.0-SNAPSHOT/bin/my-runtime
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding config-service service (-2147483647)
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding lifecycle-service service (-1000)
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding minho-banner-service service (2147483647)
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFOS:
 __  __ _       _
|  \/  (_)_ __ | |__   ___
| |\/| | | '_ \| '_ \ / _ \
| |  | | | | | | | | | (_) |
|_|  |_|_|_| |_|_| |_|\___/
  Apache Karaf Minho 1.x

oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 16, 2022 5:45:14 PM org.apache.karaf.minho.boot.service.LifeCycleService start
INFOS: Starting lifecycle service
```

## Runtime jar

You can also create a runtime executable uber jar using `minho-build jar`:

```bash
$ minho-build jar
```

You now have `my-runtime/my-runtime.jar` executable jar file. This jar contains everything you need and you can directly run it:

```bash
$ java -jar my-runtime.jar
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.Main main
INFOS: Starting runtime in exploded mode
Minho lib: /Users/jbonofre/Workspace/karaf5/tooling/cli/target/minho-build-1.0-SNAPSHOT/bin
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding config-service service (-2147483647)
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding lifecycle-service service (-1000)
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding minho-banner-service service (2147483647)
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFOS: 
 __  __ _       _           
|  \/  (_)_ __ | |__   ___  
| |\/| | | '_ \| '_ \ / _ \ 
| |  | | | | | | | | | (_) |
|_|  |_|_|_| |_|_| |_|\___/ 
  Apache Karaf Minho 1.x

oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 16, 2022 5:46:43 PM org.apache.karaf.minho.boot.service.LifeCycleService start
INFOS: Starting lifecycle service
```

## Runtime archive

Finally, you can create a zip archive with `minho-build archive`:

```bash
$ minho-build archive
oct. 16, 2022 5:48:16 PM org.apache.karaf.minho.tooling.common.Runtime <init>
INFOS: Creating Minho runtime package in folder my-runtime
oct. 16, 2022 5:48:16 PM org.apache.karaf.minho.tooling.common.Runtime createArchive
INFOS: Creating Minho runtime archive
```

You now have `my-runtime/my-runtime.zip` file, that you can extract in the directory of your choice:

```bash
$ mv my-runtime/my-runtime.zip temp
$ cd temp
$ unzip my-runtime.zip
Archive:  my-runtime.zip
  inflating: minho-banner-1.0-SNAPSHOT.jar  
  inflating: minho-boot-1.0-SNAPSHOT.jar  
   creating: bin/
  inflating: bin/minho.sh  
```

`my-runtime` archive contains everything you need, including simple `minho.sh` script to start the runtime:

```bash
$ ./bin/minho.sh
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.Main main
INFOS: Starting runtime in exploded mode
Minho lib: /Users/jbonofre/Workspace/karaf5/tooling/cli/target/minho-build-1.0-SNAPSHOT/bin/my-runtime/test
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding config-service service (-2147483647)
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding lifecycle-service service (-1000)
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding classloader-service service (-1000)
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFOS: Adding minho-banner-service service (2147483647)
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFOS: 
 __  __ _       _           
|  \/  (_)_ __ | |__   ___  
| |\/| | | '_ \| '_ \ / _ \ 
| |  | | | | | | | | | (_) |
|_|  |_|_|_| |_|_| |_|\___/ 
  Apache Karaf Minho 1.x

oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFOS: Starting services
oct. 16, 2022 5:49:44 PM org.apache.karaf.minho.boot.service.LifeCycleService start
INFOS: Starting lifecycle service
```

You can see how Karaf is easy and fast to start with.

You are now ready to create your runtimes for your existing applications, or eventually create a new application using the Minho services.

You can take a look on the [examples](/examples) and available [Minho services](services).
