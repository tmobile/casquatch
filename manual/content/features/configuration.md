---
title: "Configuration"
---

## Concepts
Casquatch configuration is designed as an extension of the underlying Datastax [Reference Configuration](https://docs.datastax.com/en/developer/java-driver/4.0/manual/core/configuration/reference/) which utilizes TypeSafe Config and implements the [standard behavior](https://github.com/lightbend/config#standard-behavior).

## Order of Precedence
Profiles are loaded in the following order:

* datastax-java-driver
* casquatch-defaults
* casquatch
* config from prefix (CasquatchDaoBuilder.withPrefix)
* config provided by CasquatchDaoBuilder
