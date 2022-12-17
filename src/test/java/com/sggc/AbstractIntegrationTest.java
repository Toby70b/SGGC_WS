package com.sggc;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = SteamGroupGamesApplication.class)
@TestPropertySource(properties = {"spring.config.location = classpath:app-test.yml"})
@Testcontainers
@EnableConfigurationProperties
public abstract class AbstractIntegrationTest {

    public static final int DYNAMO_DB_LOCAL_EXPOSED_PORT = 8000;
    public static final int LOCALSTACK_EXPOSED_PORT = 4566;

    public static final String DYNAMO_DB_LOCAL_DOCKER_IMAGE_NAME = "tobypeel/steam_group_game_checker_local_db";
    public static final String LOCALSTACK_DOCKER_IMAGE_NAME = "localstack/localstack:latest";

    public static final String LATEST_DOCKER_IMAGE_TAG = "latest";

    public static final String DYNAMO_DB_LOCAL_DOCKER_IMAGE_FULL_NAME = DYNAMO_DB_LOCAL_DOCKER_IMAGE_NAME + ":" + LATEST_DOCKER_IMAGE_TAG;
    public static final String LOCALSTACK_DOCKER_FULL_NAME = LOCALSTACK_DOCKER_IMAGE_NAME + ":" + LATEST_DOCKER_IMAGE_TAG;



/*
    static {
        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        clientBuilder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-2"));
        AmazonDynamoDB client = clientBuilder.withCredentials(new DefaultAWSCredentialsProviderChain()).build();
    }
    */
    //TODO move these into seperate classes? probably avoids the constants barrage
    @Container
    private static final GenericContainer<?> sggcDynamoDbContainer =
            new GenericContainer<>(DockerImageName.parse(DYNAMO_DB_LOCAL_DOCKER_IMAGE_FULL_NAME))
                    .withExposedPorts(DYNAMO_DB_LOCAL_EXPOSED_PORT);
    @Container
    private static final LocalStackContainer localStackContainer =
            new LocalStackContainer(DockerImageName.parse(LOCALSTACK_DOCKER_FULL_NAME))
                    .withExposedPorts(LOCALSTACK_EXPOSED_PORT)
                    .withServices(LocalStackContainer.Service.SECRETSMANAGER)
                    .withClasspathResourceMapping("/localstack", "/docker-entrypoint-initaws.d", BindMode.READ_ONLY);

    @Container
    private static final GenericContainer wiremockContainer =
            new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:latest"))
                    .withExposedPorts(8080)
                    .withClasspathResourceMapping("/wiremock", "/home/wiremock", BindMode.READ_ONLY);


    @DynamicPropertySource
    static void registerDynamoDBProperties(DynamicPropertyRegistry registry) {
        registry.add("dynamodb.address", () ->
                String.format("http://%s:%d", sggcDynamoDbContainer.getHost(), sggcDynamoDbContainer.getFirstMappedPort()));
    }

    @DynamicPropertySource
    static void registerLocalStackProperties(DynamicPropertyRegistry registry) {
        registry.add("secrets_manager.address", () ->
                String.format("http://%s:%d", localStackContainer.getHost(), localStackContainer.getFirstMappedPort()));
    }

    @DynamicPropertySource
    static void registerWiremockProperties(DynamicPropertyRegistry registry) {
        registry.add("steam.api_address", () ->
                String.format("http://%s:%d", wiremockContainer.getHost(), wiremockContainer.getFirstMappedPort()));
        registry.add("steam.store_address", () ->
                String.format("http://%s:%d", wiremockContainer.getHost(), wiremockContainer.getFirstMappedPort()));
    }
}




