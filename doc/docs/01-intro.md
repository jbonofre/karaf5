---
sidebar_position: 1
---

# Welcome to Apache Karaf Minho 

Apache Karaf Minho is the NewGen modulith runtime. In a nutshell, Minho is a modules container.

A module can be:
* a Minho service
* a Spring Boot application
* a OSGi bundle
* ...

You can see Minho as a local micro services container. Why ? For several reasons:

## Lightning fast

Minho is super light and fast. The resources are allocated to modules. The modules can be collocated to optimize resources utilization.

Starting a Minho runtime is super fast, and eventually support native execution.

## Turnkey services

Minho core ships ready to use services (configuration, lifecycle, ...).

Minho also provides a bunch of additional services. You want to work with a database, expose a REST API, etc: you don't have to implement all yourself, you can leverage services provided by Minho.

## Any framework in one runtime

Spring Boot, OSGi, microprofile, ... : no worries, Minho supports all of them that you can mix in an unique runtime. Minho provides special services named module managers.

A new framework or technology emerge: no problem, you can add a new related module manager.

## Cloud ready

Minho runtime is easy to start (just regular java). It's very easy to create a docker image containing Minho runtime, that you can orchestrate with Kubernetes.

Minho runtime is natively build to integrate smoothly on cloud, like logging on standard output, or passing configuration via env variable (and so easy to integration with Kubernetes ConfigMap).

## Green and resources efficient

Minho runtime is built with resources efficiency in mind. "Classic" micro services approach requires a lot of containers/pods, meaning that it requires lot of resources and power.

With Minho, you optimize your resources by gathering modules by runtimes. You don't need one container per micro services anymore: just create the Minho runtimes you need.

By resources collocation, we dramatically reduce the CPU, memory, power consumption: it's efficient and good for our planet. Minho is green.

## Easily create runtime packages

Focus on coding/assembling your modules, Minho tools deal with the rest.

Minho provides several tools allowing you to easily create runtimes:

* CLI to easily create on your console, in a script, in a CI/CD
* Maven plugin
* Gradle plugin
* REST service
* Minho Creator on your phone or browser

# Minho Green Orchestrator
