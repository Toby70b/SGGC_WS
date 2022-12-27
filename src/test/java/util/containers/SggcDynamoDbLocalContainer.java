package util.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

public class SggcDynamoDbLocalContainer extends GenericContainer<SggcDynamoDbLocalContainer> {

    public static final String DEFAULT_DOCKER_FILE_LOCATION = "Local-Developer-Setup/DynamoDB/Dockerfile";
    public static final int DEFAULT_EXPOSED_PORT = 8000;
    public static final String SUCCESS_LOG_MESSAGE_REGEX = ".*########## DB boostrap completed! ##########.*\\n";

    public SggcDynamoDbLocalContainer() {
        super(new ImageFromDockerfile().withDockerfile(Path.of(DEFAULT_DOCKER_FILE_LOCATION)));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
            .waitingFor(Wait.forLogMessage(SUCCESS_LOG_MESSAGE_REGEX, 1));
    }

}
