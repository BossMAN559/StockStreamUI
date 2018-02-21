package stockstream.tv.logic.channel;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import lombok.extern.slf4j.Slf4j;
import stockstream.tv.data.Channel;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ChannelRegistry {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                                                                     .withRegion(Regions.US_EAST_1)
                                                                     .withCredentials(new EnvironmentVariableCredentialsProvider())
                                                                     .build();

    private final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client);

    public boolean registerChannel(final Channel channel) {
        try {
            log.debug("Registering live channel {}", channel);
            dynamoDBMapper.save(channel);
        } catch (final Exception ex) {
            log.warn("Exception registering {}", channel, ex);
            return false;
        }

        return true;
    }

    public boolean unregisterChannel(final Channel channel) {
        try {
            dynamoDBMapper.delete(channel);
        } catch (final Exception ex) {
            log.warn("Exception registering {}", channel, ex);
            return false;
        }

        return true;
    }

    public Channel getChannel(final String platform_username) {
        final Channel channel = dynamoDBMapper.load(Channel.class, platform_username);

        return channel;
    }

    public Collection<Channel> getAllChannels() {
        final List<Channel> channels = dynamoDBMapper.scan(Channel.class, new DynamoDBScanExpression());

        return channels;
    }

}
