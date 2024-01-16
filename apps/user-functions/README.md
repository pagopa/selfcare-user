# User Functions

Repository that contains Azure functions designed for user asynchronous flow activities.

## Running locally


### Install the Azure Functions Core Tools

Follow this [guide](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local?tabs=macos%2Cisolated-process%2Cnode-v4%2Cpython-v2%2Chttp-trigger%2Ccontainer-apps&pivots=programming-language-java) for recommended way to install Core Tools on the operating system of your local development computer.

### Configuration Properties

Before running you must set these properties as environment variables.


| **Property**                                                           | **Environment Variable**   | **Default** | **Required** |
|------------------------------------------------------------------------|----------------------------|-------------|:------------:|
| quarkus.mongodb.connection-string<br/>                                 | MONGODB_CONNECTION_URI     |             |     yes      |

### Storage emulator: Azurite

Use the Azurite emulator for local Azure Storage development. Once installed, you must create `selc-d-contracts-blob` and `selc-d-product` container. Inside last one you have to put products.json file.

([guide](https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite?tabs=visual-studio))

### Install dependencies

At project root you must install dependencies:

```shell script
./mvnw install
```

### Packaging

The application can be packaged using:
```shell script
./mvnw package
```

It produces the `user-functions-1.0.0-SNAPSHOT.jar` file in the `target/` directory.

### Start application

```shell script
./mvnw package quarkus:run
```

If you want enable debugging you must add -DenableDebug

```shell script
./mvnw quarkus:run -DenableDebug
```
You can follow this guide for debugging application in IntelliJ https://www.jetbrains.com/help/idea/tutorial-remote-debug.html

## Deploy

### Configuration Properties

Before deploy you must set these properties as environment variables.


| **Property**                                       | **Environment Variable**     | **Default** | **Required** |
|----------------------------------------------------|------------------------------|-------------|:------------:|
| quarkus.azure-functions.app-name<br/>              | AZURE_APP_NAME               |             |      no      |
| quarkus.azure-functions.subscription-id<br/>       | AZURE_SUBSCRIPTION_ID        |             |      no      |
| quarkus.azure-functions.resource-group<br/>        | AZURE_RESOURCE_GROUP         |             |      no      |
| quarkus.azure-functions.app-insights-key<br/>      | AZURE_APP_INSIGHTS_KEY       |             |      no      |
| quarkus.azure-functions.app-service-plan-name<br/> | AZURE_APP_SERVICE_PLAN_NAME  |             |      no      |


## Related Guides

- Azure Functions ([guide](https://quarkus.io/guides/azure-functions)): Write Microsoft Azure functions


