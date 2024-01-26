# Selfcare User

This repo structure and build monorepo with Apache Maven for selfcare user domain. 

Applications under apps/ depend on shared code under libs/.
test-coverage/ is used to assess the test coverage of the entire project.



```
.

├── apps
│   ├── user-cdc
│   └── user-ms
└── libs
    ├── user-sdk-pom
    ├── user-sdk-common
└── test-coverage
```

Look at single README module for more information.

## Infrastructure

The [`.container_apps/`] sub folder contains terraform files for deploying infrastructure as container apps in Azure.


## Continous integration

The [`.github/`] sub folder contains a self-contained ci-stack for building the monorepo with Github Actions.

## Usage

```shell script
mvn clean package install
```

## Maven basic actions for monorep

Maven is really not a monorepo-*native* build tool (e.g. lacks
trustworthy incremental builds, can only build java code natively, is recursive and
struggles with partial repo checkouts) but can be made good use of with some tricks
and usage of a couple of lesser known command line switches.

| Action                                                                                       | in working directory | with Maven                                                                      |
|:---------------------------------------------------------------------------------------------|:--------------------:|:--------------------------------------------------------------------------------|
| Build the world                                                                              |         `.`          | `mvn clean package -DskipTests`                                                 |
| Run `user-ms`                                                                                |         `.`          | `java -jar apps/user-ms/target/user-ms-1.0.0-SNAPSHOT.jar`                      |
| Build and test the world                                                                     |         `.`          | `mvn clean package`                                                             |
| Build the world                                                                              |   `./apps/user-ms`   | `mvn --file ../.. clean package -DskipTests`                                    |
| Build `user-ms` and its dependencies                                                         |         `.`          | `mvn --projects :user-ms --also-make clean package -DskipTests`                 |
| Build `user-ms` and its dependencies                                                         |   `./apps/user-ms`   | `mvn --file ../.. --projects :user-ms --also-make clean package -DskipTests`    |
| Build `user-sdk` and its dependents (aka. reverse dependencies or *rdeps* in Bazel parlance) |         `.`          | `mvn --projects :user-sdk-pom --also-make-dependents clean package -DskipTests` |
| Print dependencies of `user-sdk`                                                             |   `./apps/user-ms`   | `mvn dependency:list`                                                           |
| Change version  of `user-sdk`                                                                |         `.`          | `mvn versions:set -DnewVersion=0.1.2 --projects :user-sdk-pom  `                |
| Persist version  of `user-sdk`                                                               |         `.`          | `mvn versions:commit   `                                                        |

