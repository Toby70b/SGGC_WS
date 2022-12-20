package com.sggc;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class WiremockContainer extends GenericContainer<WiremockContainer>  {

    private static final String DEFAULT_DOCKER_IMAGE = "wiremock/wiremock:latest";
    private static final int DEFAULT_EXPOSED_PORT = 8080;

    public WiremockContainer() {
        super(DockerImageName.parse(DEFAULT_DOCKER_IMAGE));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT);
    }

    /**
     * Resets the state of the WireMock instance
     */
    public void reset() {
        WireMock wiremockClient = new WireMock("localhost", this.getFirstMappedPort());
        wiremockClient.resetToDefaultMappings();
    }


}

