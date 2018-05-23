Transparent Router
=========================================

[![Build Status](https://travis-ci.org/candrews/transparentrouter.svg?branch=master)](https://travis-ci.org/candrews/transparentrouter)

Transparent Router for [AWS SQS](https://aws.amazon.com/sqs/) routes messages, implementing the [request/reply enterprise integration pattern](http://www.enterpriseintegrationpatterns.com/patterns/messaging/RequestReplyJmsExample.html) for services that don't support it and always send replies to a specific queue.

Transparent Router:
1. Accept a message on an incoming queue
1. Records the values of the `JMSCorrelationID` (if that's not set, then the `JMSMessageID`) and `ReplyToQueueArn` message properties
1. Sends that message on an outgoing queue
1. Receives a message on a reply queue
1. Looks up the value of the message's `JMSCorrelationID` message property to see where to forward message (using the `JMSMessageID`/`JMSCorrelationID` and `ReplyToQueueArn` of a previously received message)
1. Sends that message to the `ReplyToQueueArn`

# Running on a Server
1. Run `./mvnw package` to create the jar at `target/transparentrouter-*.jar`
1. Copy the jar to the server
1. On the server
    1. Set the environment variables:
        1. `SPRING_DATASOURCE_URL=jdbc:...` (for example, `jdbc:oracle:thin:@HOSTNAME:1521:MYDATABASE` for Oracle)
        1. `SPRING_DATASOURCE_USERNAME=someone`
        1. `SPRING_DATASOURCE_PASSWORD=secret`
        1. For each route, starting with number 0, declare these environment variables (substitute 999 for a number starting with 0, unique for each route):
            1. `QUEUES_ROUTES_999_INCOMINGARN=arn:aws:sqs:REGION:ACCOUNTNUMBER:QUEUENAME`
            1. `QUEUES_ROUTES_999_OUTGOINGARN=arn:aws:sqs:REGION:ACCOUNTNUMBER:QUEUENAME`
            1. `QUEUES_ROUTES_999_REPLYARN=arn:aws:sqs:REGION:ACCOUNTNUMBER:QUEUENAME`
    1. Provide the region using any AWS Java SDK supported method: environment variables, system properties, profile, or instance metadata (when running inside AWS)
    1. Provide credentials to access the queues using any AWS Java SDK supported method: environment variables, system properties, profile, or instance metadata (when running inside AWS)
    1. Get the JDBC driver jar (for example, `ojdbc8.jar` for Oracle)
    1. Run the jar and include the JDBC driver : `java -cp "ojdbc8.jar:transparentrouter-0.0.1-SNAPSHOT.jar" "org.springframework.boot.loader.JarLauncher"`

At startup, the required database databases, indexes, and constraints will be created if they don't already exist.

# Developer Quick Start

## Prerequisites
To get Transparent Router running on your system, you'll need:

* Java 8 (JDK)
* Credentials:
   * Access to this git repository
   * AWS access key id and secret key

## Running Transparent Router

### Spring Boot Run
From this directory:

1. In this directory, copy `application-local.properties.template` to `application-local.properties`
2. Edit `application-local.properties` providing queue information and AWS credentials (do not commit this file because it now contains your AWS credentials; it is listed in `.gitignore` for this reason)
1. Run `./mvnw spring-boot:run` - the application will start.

The application will be available at http://localhost:8080/

## Developing with Eclipse

Any IDE can be used to develop Transparent Router. Eclipse specific instructions are provided for convenience.

The Project Lombok Eclipse integration must be setup. See the Eclipse instructions on [Project Lombok's site](https://projectlombok.org/features/index.html).

Using [Spring Tool Suite](https://spring.io/tools/sts/) or adding those plugins to Eclipse will provide improved developer tools, such as autocomplete of configuration in .properties files and automatic restart when debugging.

### Getting Started

1. Start Eclipse
1. Create a new workspace (the workspace directory _must not_ be in this directory or a subdirectory of this directory)
1. Go to `File`->`Import`->`Maven`->`Existing Maven Projects`
1. Select this directory as the root directory
1. Click `Finish`

### Debugging

1. In Eclipse, right click on the `transparentrouter` project and select `Debug As`->`Java Application`

If Eclipse has the Spring Boot plugin installed or STS is being used, instead of the above, right click on `transparentrouter` and select `Debug As`->`Spring Boot App`

You can now set breakpoints and perform all other debugging tasks.
