package stockstream.tv.information;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.logic.PubSub;
import stockstream.logic.Scheduler;
import stockstream.tv.data.InfoMessage;
import stockstream.util.RandomUtil;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Twitter {

    private static final int MIN_FAVS = 3;
    private static final int MAX_AGE_HOURS = 6;

    private final Collection<String> twitterUsers =
            ImmutableList.of("realdonaldtrump", "twitch", "jimcramer", "SEC_News", "RobinhoodApp",
                             "JeffBezos", "jonsteinberg", "Cheddar", "WisdomyQuotes", "justinkan", "BenEisen",
                             "elonmusk", "SouthPark", "TheOnion", "BarackObama");

    @Autowired
    private PubSub pubSub;

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        this.scheduler.scheduleJob(this::findFollowTweets, 10, 600, TimeUnit.SECONDS);
    }

    private void findFollowTweets() {
        twitter4j.Twitter twitter = TwitterFactory.getSingleton();

        String user = RandomUtil.randomChoice(twitterUsers).orElse(twitterUsers.iterator().next());

        List<Status> statuses = new ArrayList<>();

        try {
            statuses = twitter.getUserTimeline(user);
        } catch (TwitterException e) {
            log.warn(e.getMessage(), e);
        }

        publishTweets(statuses);
    }

    private void publishTweets(final Collection<Status> tweets) {
        final Set<InfoMessage> infoMessages = new HashSet<>();

        for (final Status status : tweets) {

            if (status.isRetweet() || status.getFavoriteCount() < MIN_FAVS) {
                continue;
            }

            if (status.getCreatedAt().after(DateUtils.addHours(new Date(), -MAX_AGE_HOURS))) {
                continue;
            }

            final Optional<InfoMessage> infoMessage = constructInfoMessageFromTweet(status);
            if (!infoMessage.isPresent()) {
                continue;
            }
            infoMessages.add(infoMessage.get());
        }

        log.info("Got {} InfoMessages out of {} tweets.", infoMessages.size(), tweets.size());

        infoMessages.forEach(infoMessage -> pubSub.publishClassType(InfoMessage.class, infoMessage));
    }


    public Optional<InfoMessage> constructInfoMessageFromTweet(final Status tweet) {
        final String text = tweet.getText();

        final String handle = tweet.getUser().getScreenName();

        final String url= "https://twitter.com/" + handle + "/status/" + tweet.getId();

        final String platform = "twitter";

        final InfoMessage infoMessage = new InfoMessage("@" + handle, text,
                                                        tweet.getCreatedAt().getTime(), platform, url, tweet.getUser().getOriginalProfileImageURL());

        return Optional.of(infoMessage);
    }

}
