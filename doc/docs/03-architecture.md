# Architecture

K5 has been designed with simplicity and user/developer focus in mind.

K5 architecture is service module oriented at core.

The K5 boot component is responsible of:

* the main Service Registr hosting services provided by modules
* the K5 core services
* the K5 service loader to register additional services in your runtime

<img src="../img/architecture.png" />

You can find details about K5 services [here](services).
