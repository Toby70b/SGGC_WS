package com.sggc;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class WiremockContainer extends GenericContainer<WiremockContainer>  {

    private static final String DEFAULT_DOCKER_IMAGE = "wiremock/wiremock:latest";
    private static final int DEFAULT_EXPOSED_PORT = 8080;
    private static final String PRE_CONFIGURED_STUBS_HOST_PATH = "/wiremock";
    private static final String PRE_CONFIGURED_STUBS_CONTAINER_PATH = "/home/wiremock";

    public WiremockContainer() {
        super(DockerImageName.parse(DEFAULT_DOCKER_IMAGE));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
                .withClasspathResourceMapping(PRE_CONFIGURED_STUBS_HOST_PATH,
                        PRE_CONFIGURED_STUBS_CONTAINER_PATH,BindMode.READ_ONLY);
    }

    /**
     * Resets the state of the WireMock instance
     */
    public void reset() {
        WireMock wiremockClient = new WireMock("localhost", this.getFirstMappedPort());
        wiremockClient.resetToDefaultMappings();
    }


}

