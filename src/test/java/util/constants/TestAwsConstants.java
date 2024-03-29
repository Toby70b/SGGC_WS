package util.constants;

import com.amazonaws.regions.Regions;
/**
 * Represents a class for constants related to Amazon Web Services (AWS) required by the Application. To be used for
 * application tests.
 */
public class TestAwsConstants {
    public static final String DEFAULT_REGION = Regions.EU_WEST_2.getName();
    public static final String MOCK_ACCESS_KEY = "DUMMY_ACCESS_KEY";
    public static final String MOCK_SECRET_ACCESS_KEY = "DUMMY_SECRET_ACCESS_KEY";
}
