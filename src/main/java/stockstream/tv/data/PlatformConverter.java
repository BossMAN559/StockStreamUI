package stockstream.tv.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class PlatformConverter implements DynamoDBTypeConverter<String, Platform> {
    @Override
    public String convert(final Platform platform) {
        return platform.name().toLowerCase();
    }

    @Override
    public Platform unconvert(final String platform) {
        return Platform.valueOf(platform.toUpperCase());
    }
}
