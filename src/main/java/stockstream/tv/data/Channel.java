package stockstream.tv.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "StockStreamTVChannelsProd")
public class Channel {

    @DynamoDBHashKey
    private String name;

    @DynamoDBTypeConverted(converter = PlatformConverter.class)
    private Platform platform;

    private String streamURL;

    private boolean musicChannel;

    @JsonIgnore
    @DynamoDBIgnore
    public boolean isStreaming() {
        return !StringUtils.isEmpty(streamURL);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(platform).append(streamURL).build();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (!(otherObject instanceof Channel)) {
            return false;
        }
        final Channel otherChannel = (Channel) otherObject;
        return Objects.equals(this.getName(), otherChannel.getName()) &&
               Objects.equals(this.getPlatform(), otherChannel.getPlatform()) &&
               Objects.equals(this.getStreamURL(), otherChannel.getStreamURL());
    }
}
