package stockstream.tv.logic.elections;

import org.springframework.beans.factory.annotation.Autowired;
import stockstream.data.Voter;
import stockstream.logic.elections.Election;
import stockstream.tv.application.Config;
import stockstream.tv.twitch.WebTVSocket;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TVChannelElection extends Election<TVVote> {

    @Autowired
    private WebTVSocket webTVSocket;

    public TVChannelElection() {
        super("!channel", TVVote.class, 5);
    }

    @PostConstruct
    public void init() {
        final Set<Voter> adminVoters = Config.TV_ADMINS.stream().map(admin -> new Voter(admin.toLowerCase(), "twitch", "stockstream", true)).collect(Collectors.toSet());

        this.withSubscribersOnly(Config.SUBSCRIBERS_ONLY)
            .withEligibleVoters(adminVoters)
            .withOutcome(new TVVote(TVAction.NO), () -> {})
            .withExpirationDate(0);

        this.withMessageParser(s -> {
            if (s.equalsIgnoreCase("!tv yes")) {
                return Optional.of(new TVVote(TVAction.YES));
            } else if (s.equalsIgnoreCase("!tv no")) {
                return Optional.of(new TVVote(TVAction.NO));
            }
            return Optional.empty();
        });
    }
}
