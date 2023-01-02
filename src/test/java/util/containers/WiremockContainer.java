package util.containers;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Represents a Testcontainer for the WireMock mocking service.
 */
public class WiremockContainer extends GenericContainer<WiremockContainer>  {

    private static final String DOCKER_IMAGE = "wiremock/wiremock:latest";
    private static final int EXPOSED_PORT = 8080;
    private static final String FILES_HOST_PATH = "/wiremock/files";
    private static final String FILES_CONTAINER_PATH = "/home/wiremock/__files";

    public WiremockContainer() {
        super(DockerImageName.parse(DOCKER_IMAGE));
        this.withExposedPorts(EXPOSED_PORT)
                .withClasspathResourceMapping(FILES_HOST_PATH,
                        FILES_CONTAINER_PATH,BindMode.READ_ONLY);
    }

}

