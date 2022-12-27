package testsupport.constants;

import com.amazonaws.services.s3.model.Region;
/**
 * Represents a class for constants related to Amazon Web Services (AWS) required by the Application. To be used for
 * application tests.
 */
public class TestAwsConstants {
    public static final String DEFAULT_REGION = Region.EU_London.getFirstRegionId();
}
