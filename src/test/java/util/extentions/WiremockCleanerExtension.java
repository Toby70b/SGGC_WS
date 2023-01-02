package util.extentions;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Represents a custom JUnit Extension, used to reset a local WireMock instance's stub mappings to its default mappings.
 */
public class WiremockCleanerExtension implements BeforeEachCallback {

    private final WireMock wiremockClient;

    /**
     * Initializes a new WiremockCleanerExtension object.
     * @param port he port number of the local WireMock instance.
     */
    public WiremockCleanerExtension(int port) {
        wiremockClient = new WireMock("localhost", port);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        wiremockClient.resetToDefaultMappings();
    }
}

