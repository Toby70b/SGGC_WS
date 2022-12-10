# Steam Group Game Checker (SGGC) Web Service - Local Developer Setup

This directory contains information on how you can set up local versions of the external dependencies that the SGGC WS 
requires for local development

## DynamoDB

The SGGC's persistence layer uses DynamoDB to function, when running in a dev environment we use a local DynamoDB instance

A local DynamoDB instance can be set up by using the tobypeel/steam_group_game_checker_local_db docker image found 
[here](https://hub.docker.com/r/tobypeel/steam_group_game_checker_local_db). This docker image is created from the 
Dockerfile under [DynamoDB](DynamoDb/Dockerfile). As you can see it's based on the amazon/dynamodb-local image found 
[here](https://hub.docker.com/r/amazon/dynamodb-local) with the AWS CLI added also for added flexibility, for example 
adding new tables.

Through the Dockerfile, the DynamoDB instance is bootstrapped with the tables required by the SGGC WS. Bootstrapping is done by
copying over the JSON representation of the tables required, a Shell script is then run which starts the DynamoDB instance
and then loops through the copied JSON files and calls the AWS CLI to create a table with each JSON file as the input.

As some find the CLI suboptimal for certain tasks the docker-compose file includes a container for the 
[aaronshaf/dynamodb-admin](https://hub.docker.com/r/aaronshaf/dynamodb-admin/) image which includes a UI for managing a 
DynamoDB instance

For more details on how tobypeel/steam_group_game_checker_local_db can be run please check the Dockerhub page 
[here](https://hub.docker.com/r/tobypeel/steam_group_game_checker_local_db).

## AWS Secrets Manager

A key is required to be able to query the Steam API, the SGGC utilizes AWS Secrets Manager (ASM) to store and retrieve this key.
Within a development environment, we run a local ASM instance via [LocalStack](https://localstack.cloud/)

A local LocalStack instance can be created from the [localstack/localstack](localstack/localstack) docker image.
When running via the docker-compose file a mount location has been added to <i>/docker-entrypoint-initaws.d</i> and includes a 
script which initializes the secret(s) required by the SGGC. As per the documentation, any scripts mounted to this location
will be run on startup.

## Steam

The SGGC depends on both the Steam API and Store to be able to filter the common games owned by Steam users. As Valve
have not published a mock API for the Steam API, the SGGC relies on a WireMock instance to mock Steam API and Store
functionality. Stubs are often bespoke to suit the needs of the current feature under development, as such the
docker-compose file does not pre-configure the instance we any stubs. However, the project does contain some example stubs
covering each endpoint required for a local SGGC to function these can be found [here](Steam/Wiremock-Stub-Examples)

