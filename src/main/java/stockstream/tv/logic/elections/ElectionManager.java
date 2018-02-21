package stockstream.tv.logic.elections;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.logic.Scheduler;
import stockstream.tv.data.Channel;
import stockstream.tv.twitch.TVBot;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ElectionManager {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private TVBot tvBot;

    @Autowired
    private TVChannelElection tvChannelElection;

    @Setter
    @Getter
    private Channel nextChannel = null;

    @PostConstruct
    public void init() {
        tvBot.registerHook((voter, message) -> {
            if (!isElectionActive()) {
                return false;
            }

            tvChannelElection.receiveVote(message, voter);

            return false;
        });

        scheduler.scheduleJob(this::tickElections, 500, 500, TimeUnit.MILLISECONDS);
    }

    public TVChannelElection getTvChannelElection() {
        return isElectionActive() ? tvChannelElection : null;
    }

    public void tickElections() {
        if (!isElectionActive()) {
            return;
        }

        final long currentTimestamp = new Date().getTime();
        if (currentTimestamp > tvChannelElection.getExpirationDate()) {
            tvChannelElection.executeOutcome();
            tvChannelElection.setExpirationDate(0);
            nextChannel = null;
        }
    }

    public boolean isElectionActive() {
        return tvChannelElection.getExpirationDate() != 0;
    }

}
