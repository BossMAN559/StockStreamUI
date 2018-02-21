package stockstream.tv.information;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import stockstream.tv.data.Channel;
import stockstream.tv.data.Platform;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Whitehouse {

    private static final String LIVE_EVENTS_URL = "https://www.whitehouse.gov/live";

    public Set<Channel> findLiveChannels() {
        final Set<String> liveEvents = findYoutubeLinks();

        if (CollectionUtils.isEmpty(liveEvents)) {
            return Collections.emptySet();
        }

        final Set<Channel> whitehouseChannels = new HashSet<>();

        int channels = 1;
        for (final String youtubeURL : liveEvents) {
            final String channelName = "whitehouse" + String.valueOf(channels);
            final String embedLink = youtubeURL.replace("watch?v=", "embed/");
            final Channel channel = new Channel(channelName, Platform.WHITEHOUSE, embedLink + "?autoplay=1", false);
            whitehouseChannels.add(channel);
            channels++;
        }

        return whitehouseChannels;
    }


    private Set<String> findYoutubeLinks() {
        final Set<String> youtubeLinks = new HashSet<>();

        try {
            final Elements links = Jsoup.connect(LIVE_EVENTS_URL).get().select("a");

            for (final Element element : links) {
                if (!element.hasAttr("href") || !element.attr("href").startsWith("https://www.youtube.com")) {
                    continue;
                }

                youtubeLinks.add(element.attr("href"));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return youtubeLinks;
    }
}
