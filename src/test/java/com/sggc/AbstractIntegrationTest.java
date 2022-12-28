package com.sggc;

import util.containers.SggcDynamoDbLocalContainer;
import util.containers.SggcLocalStackContainer;
import util.containers.WiremockContainer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = SteamGroupGamesApplication.class)
@TestPropertySource(properties = {"spring.config.location = classpath:app-int-test.yml"})
@Testcontainers
@EnableConfigurationProperties
public abstract class AbstractIntegrationTest {

    protected static final SggcDynamoDbLocalContainer sggcDynamoDbContainer;
    protected static final SggcLocalStackContainer localStackContainer;
    protected static final WiremockContainer wiremockContainer;

    static {
        sggcDynamoDbContainer = new SggcDynamoDbLocalContainer();
        wiremockContainer = new WiremockContainer();
        localStackContainer = new SggcLocalStackContainer();

        sggcDynamoDbContainer.start();
        localStackContainer.start();
        wiremockContainer.start();
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
}




