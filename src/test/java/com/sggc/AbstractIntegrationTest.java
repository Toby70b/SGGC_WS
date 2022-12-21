package com.sggc;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.containers.SggcDynamoDbLocalContainer;
import com.sggc.containers.SggcLocalStackContainer;
import com.sggc.containers.WiremockContainer;
import com.sggc.helper.AwsSecretsManagerTestSupporter;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.sggc.constants.TestAwsConstants.DEFAULT_REGION;

@SpringBootTest(classes = SteamGroupGamesApplication.class)
@TestPropertySource(properties = {"spring.config.location = classpath:app-test.yml"})
@Testcontainers
@EnableConfigurationProperties
public abstract class AbstractIntegrationTest {

    protected static final SggcDynamoDbLocalContainer sggcDynamoDbContainer;
    protected static final SggcLocalStackContainer localStackContainer;
    protected static final WiremockContainer wiremockContainer;

    protected static AmazonDynamoDB dynamoDbClient;
    protected static AWSSecretsManager secretsManagerClient;
    protected static WireMock wiremockClient;

    protected static AwsSecretsManagerTestSupporter secretsManagerTestSupporter;

    static {
        sggcDynamoDbContainer = new SggcDynamoDbLocalContainer();
        wiremockContainer = new WiremockContainer();
        localStackContainer = new SggcLocalStackContainer();

        sggcDynamoDbContainer.start();
        localStackContainer.start();
        wiremockContainer.start();

        //TODO for v2 maybe remove all of these and allow each test class to define what clients they want? Could be slower due to new object instantiation but probably wouldn't be noticeable. Can test
        dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:"+sggcDynamoDbContainer.getFirstMappedPort().toString(), DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        secretsManagerClient = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:"+localStackContainer.getFirstMappedPort().toString(), DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        wiremockClient = new WireMock("localhost", wiremockContainer.getFirstMappedPort());

        //TODO whats to be done about this for V2? We can keep it, as it is used, but should we do something different?
        secretsManagerTestSupporter = new AwsSecretsManagerTestSupporter(secretsManagerClient);
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

    //TODO remove for v2 and move into junit extensions
    @AfterEach
    void cleanup() {
        sggcDynamoDbContainer.reset();
        wiremockContainer.reset();
        localStackContainer.reset();
    }


}




