package com.sggc;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

@SpringBootTest(classes = SteamGroupGamesApplication.class)
@TestPropertySource(properties = {"spring.config.location = classpath:app-test.yml"})
@Testcontainers
@EnableConfigurationProperties
public abstract class AbstractIntegrationTest {

    public static final int LOCALSTACK_EXPOSED_PORT = 4566;

    public static final String LOCALSTACK_DOCKER_IMAGE_NAME = "localstack/localstack:latest";

    public static final String LATEST_DOCKER_IMAGE_TAG = "latest";

    public static final String LOCALSTACK_DOCKER_FULL_NAME = LOCALSTACK_DOCKER_IMAGE_NAME + ":" + LATEST_DOCKER_IMAGE_TAG;

    public static final String LOCALSTACK_SUCCESS_LOG_MESSAGE_REGEX = ".*########## Secrets Initialized ##########.*\\n";

    static final SggcDynamoDbLocalContainer sggcDynamoDbContainer;
    static final GenericContainer<?> localStackContainer;
    static final GenericContainer<?> wiremockContainer;

    static AmazonDynamoDB amazonDynamoDB;
    static AWSSecretsManager awsSecretsManager;


    static {
        sggcDynamoDbContainer = new SggcDynamoDbLocalContainer();

        localStackContainer =
                new LocalStackContainer(DockerImageName.parse(LOCALSTACK_DOCKER_FULL_NAME))
                        .withExposedPorts(LOCALSTACK_EXPOSED_PORT)
                        .withServices(LocalStackContainer.Service.SECRETSMANAGER)
                        .withClasspathResourceMapping("/localstack", "/docker-entrypoint-initaws.d", BindMode.READ_ONLY)
                        .waitingFor(Wait.forLogMessage(LOCALSTACK_SUCCESS_LOG_MESSAGE_REGEX, 1));

        wiremockContainer =
                new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:latest"))
                        .withExposedPorts(8080)
                        .withClasspathResourceMapping("/wiremock", "/home/wiremock", BindMode.READ_ONLY);


        sggcDynamoDbContainer.start();
        localStackContainer.start();
        wiremockContainer.start();

        amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:"+sggcDynamoDbContainer.getFirstMappedPort().toString(), "eu-west-2"))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        awsSecretsManager = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:"+localStackContainer.getFirstMappedPort().toString(), "eu-west-2"))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

    }


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

    @AfterEach
    void cleanup() throws IOException {
        sggcDynamoDbContainer.reset(amazonDynamoDB);
        //TODO call cleanup methods on other containers, move dynamodb logic into aws cleanup class hierarchy
    }


}




