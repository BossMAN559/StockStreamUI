package stockstream.tv.logic.channel;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.logic.Scheduler;
import stockstream.tv.data.Channel;
import stockstream.tv.data.Platform;
import stockstream.tv.information.Whitehouse;
import stockstream.twitch.TwitchAPI;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ChannelRefresher {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ChannelRegistry channelRegistry;

    @Autowired
    private TwitchAPI twitchAPI;

    @Autowired
    private Whitehouse whitehouse;

    @PostConstruct
    public void init() {
        scheduler.scheduleJob(this::refreshActiveChannels, 5, 60, TimeUnit.SECONDS);
    }

    @VisibleForTesting
    protected void refreshActiveChannels() {
        final Collection<Channel> allChannels = channelRegistry.getAllChannels();

        final Map<String, Channel> twitchChannels = allChannels.stream().filter(channel -> Platform.TWITCH.equals(channel.getPlatform()))
                                                               .collect(Collectors.toMap(Channel::getName, c -> c));
        final Map<String, String> usernameToURL = twitchAPI.getStreamURLsByUsername(new ArrayList<>(twitchChannels.keySet()));
        usernameToURL.forEach((key, value) -> {
            final Channel existingChannel = twitchChannels.get(key);
            if (!StringUtils.equals(value, existingChannel.getStreamURL())) {
                existingChannel.setStreamURL(value);
                channelRegistry.registerChannel(existingChannel);
            }
        });

        twitchChannels.forEach((key, value) -> {
            if (!usernameToURL.containsKey(key)) {
                value.setStreamURL("");
                channelRegistry.registerChannel(value);
            }
        });

        for (final Channel channel : allChannels) {
            if (!Platform.WHITEHOUSE.equals(channel.getPlatform())) {
                continue;
            }
            channelRegistry.unregisterChannel(channel);
        }
        whitehouse.findLiveChannels().forEach(channel -> channelRegistry.registerChannel(channel));
    }




}
