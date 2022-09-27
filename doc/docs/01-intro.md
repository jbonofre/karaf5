---
sidebar_position: 1
---

# Welcome to K5

Apache Karaf K5 is the NewGen modulith runtime. In a nutshell, K5 is a modules container.

A module can be:
* a K5 service
* a Spring Boot application
* a OSGi bundle
* ...

You can see K5 a micro services container local. Why ? For several reasons:

## Lightning fast

K5 is super light and fast. The resources are allocated to modules. The modules can be collocated to optimize resources utilization.

Starting a K5 runtime is super fast, and eventually support native execution.

## Turnkey services

K5 core ships ready to use services (configuration, lifecycle, ...).

K5 also provides a bunch of additional services. You want to work with a database, expose a REST API, etc: you don't have to implement all yourself, you can leverage services provided by K5.

## Any framework in one runtime

Spring Boot, OSGi, microprofile, ... : no worries, K5 supports all of them that you can mix in an unique runtime. K5 provides special services named module managers.

A new framework or technology emerge: no problem, you can add a new related module manager.

## Cloud ready

K5 runtime is easy to start (just regular java). It's very easy to create a docker image containing K5 runtime, that you can orchestrate with Kubernetes.

K5 runtime is natively build to integrate smoothly on cloud, like logging on standard output, or passing configuration via env variable (and so easy to integration with Kubernetes ConfigMap).

## Green and resources efficient

K5 runtime is built with resources efficiency in mind. "Classic" micro services approach requires a lot of containers/pods, meaning that it requires lot of resources and power.

With K5, you optimize your resources by gathering modules by runtimes. You don't need one container per micro services anymore: just create the K5 runtimes you need.

By resources collocation, we dramatically reduce the CPU, memory, power consumption: it's efficient and good for our planet. K5 is green.

## Easily create runtime packages

Focus on coding/assembling your modules, K5 tools deal with the rest.

K5 provides several tools allowing you to easily create runtimes:

* CLI to easily create on your console, in a script, in a CI/CD
* Maven plugin
* Gradle plugin
* REST service
* K5 Creator on your phone or browser
