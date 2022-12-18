package com.sggc;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;

public class WiremockContainer  extends GenericContainer<WiremockContainer> {

    public static final String DEFAULT_DOCKER_IMAGE = "wiremock/wiremock:latest";
    public static final int DEFAULT_EXPOSED_PORT = 8080;
    public static final String PRE_CONFIGURED_STUBS_HOST_PATH = "/wiremock";
    public static final String PRE_CONFIGURED_STUBS_CONTAINER_PATH = "/home/wiremock";

    public WiremockContainer() {
        super(DockerImageName.parse(DEFAULT_DOCKER_IMAGE));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
                .withClasspathResourceMapping(PRE_CONFIGURED_STUBS_HOST_PATH,
                        PRE_CONFIGURED_STUBS_CONTAINER_PATH,BindMode.READ_ONLY);
    }

    /**
     * Resets the state of the WireMock instance
     */
    public void reset(WireMock wireMock) {
        wireMock.resetToDefaultMappings();
    }


}

