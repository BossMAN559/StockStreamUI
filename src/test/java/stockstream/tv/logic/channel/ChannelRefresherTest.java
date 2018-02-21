package stockstream.tv.logic.channel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import stockstream.logic.Scheduler;
import stockstream.tv.data.Channel;
import stockstream.tv.data.Platform;
import stockstream.tv.information.Whitehouse;
import stockstream.twitch.TwitchAPI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChannelRefresherTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private ChannelRegistry channelRegistry;

    @Mock
    private TwitchAPI twitchAPI;

    @Mock
    private Whitehouse whitehouse;

    @InjectMocks
    private ChannelRefresher channelRefresher;

    @Before
    public void setTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRefreshActiveChannels_oneTwitchChannel_expectChannelSaved() {
        final Channel channel = new Channel("testchannel", Platform.TWITCH, "url", false);

        final ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);

        when(channelRegistry.getAllChannels()).thenReturn(ImmutableList.of(channel));
        when(twitchAPI.getStreamURLsByUsername(any())).thenReturn(ImmutableMap.of("testchannel", "apiurl"));

        channelRefresher.refreshActiveChannels();

        verify(channelRegistry, times(1)).registerChannel(captor.capture());

        final Channel registeredChannel = captor.getValue();
        assertEquals(registeredChannel.getStreamURL(), "apiurl");
    }

}
