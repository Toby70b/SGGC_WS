package util.containers;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;

/**
 * Represents a Testcontainer for the Localstack service, with some extra default values applied to better suit usage
 * by the SGGC application
 */
public class SggcLocalStackContainer extends LocalStackContainer {

    private static final String DEFAULT_DOCKER_IMAGE = "localstack/localstack:latest";
    private static final int DEFAULT_EXPOSED_PORT = 4566;
    public static final LocalStackContainer.Service[] ENABLED_SERVICES = {SECRETSMANAGER};

    public SggcLocalStackContainer() {
        super(DockerImageName.parse(DEFAULT_DOCKER_IMAGE));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
                .withServices(ENABLED_SERVICES);
    }
}
