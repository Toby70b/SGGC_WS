# Steam Group Game Checker (SGGC) Web Service - Local Developer Setup

This directory contains information on how you can set up local versions of the external dependencies that the SGGC WS 
requires for local development

## DynamoDB

A local DynamoDB instance can be setup by using the tobypeel/steam_group_game_checker_local_db docker image found 
[here](https://hub.docker.com/r/tobypeel/steam_group_game_checker_local_db). This docker image is created from the 
Dockerfile under [DynamoDB](DynamoDb/Dockerfile). As you can see its based off of the amazon/dynamodb-local image found 
[here](https://hub.docker.com/r/amazon/dynamodb-local) with the aws cli added also for added flexibility, for example 
adding new tables.

Through the Dockerfile, the DynamoDB instance is bootstrapped with the tables required by the SGGC WS. Bootstrapping is done by
copying over the JSON representation of the tables required, a Shell script is then run which starts the DynamoDB instance
and then loops through the copied JSON files and calls the AWS CLI to create a table with each JSON file as the input.

As some find the CLI suboptimal for certain tasks there is also a docker-compose file [here](DynamoDb/docker-compose.yml),
this includes a container for the [aaronshaf/dynamodb-admin](https://hub.docker.com/r/aaronshaf/dynamodb-admin/) image
which includes a UI for managing a DynamoDB instance

For more details on how tobypeel/steam_group_game_checker_local_db can be run please check the Dockerhub page 
[here](https://hub.docker.com/r/tobypeel/steam_group_game_checker_local_db).
