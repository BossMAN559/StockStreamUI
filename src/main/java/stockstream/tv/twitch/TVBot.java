package stockstream.tv.twitch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.data.Voter;
import stockstream.tv.application.Config;
import stockstream.tv.cache.InfoMessageCache;
import stockstream.tv.data.Channel;
import stockstream.tv.data.Platform;
import stockstream.tv.logic.channel.ChannelRegistry;
import stockstream.tv.logic.elections.ElectionManager;
import stockstream.tv.logic.elections.TVAction;
import stockstream.tv.logic.elections.TVChannelElection;
import stockstream.tv.logic.elections.TVVote;
import stockstream.twitch.TwitchAPI;
import stockstream.twitch.TwitchChat;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TVBot extends ListenerAdapter {

    private List<BotHook> botHooks = new ArrayList<>();

    @Autowired
    private InfoMessageCache infoMessageCache;

    @Autowired
    private ChannelRegistry channelRegistry;

    @Autowired
    private TwitchAPI twitchAPI;

    @Autowired
    private WebTVSocket webTVSocket;

    @Autowired
    private TwitchChat twitchChat;

    @Autowired
    private ElectionManager electionManager;

    @Autowired
    private TVChannelElection tvChannelElection;

    @PostConstruct
    public void init() {
        createFunctions();
    }

    public void registerHook(final BotHook botHook) {
        botHooks.add(botHook);
    }

    @Override
    public void onEvent(final Event event) throws Exception {
        super.onEvent(event);
    }

    @Override
    public void onMessage(final MessageEvent event) {
        final String eventMessage = event.getMessage().toLowerCase();
        if (StringUtils.isEmpty(eventMessage)) {
            return;
        }

        if (!eventMessage.startsWith("!")) {
            return;
        }

        if (event.getUser() == null) {
            return;
        }

        final boolean isSubscriber = "1".equals(event.getTags().getOrDefault("subscriber", "0"));

        final String fromChannel = event.getChannelSource();
        final String message = eventMessage.toLowerCase();
        final String sender = event.getUser().getNick();

        final Voter voter = new Voter(sender, "twitch", fromChannel, isSubscriber);

        for (final BotHook botHook : botHooks) {
            final boolean messageProcessed = botHook.processMessage(voter, message);
            if (messageProcessed) {
                return;
            }
        }
    }

    private boolean preProcessingStepsOk(final Voter voter) {
        if (!voter.isSubscriber() && Config.SUBSCRIBERS_ONLY) {
            return false;
        }

        if (Config.TV_ADMINS.size() > 0 && !Config.TV_ADMINS.contains(voter.getUsername())) {
            return false;
        }

        return true;
    }

    private void createFunctions() {

        botHooks.add((voter, message) -> {
            final boolean preProcessOk = preProcessingStepsOk(voter);

            if (preProcessOk && message.equalsIgnoreCase("!tv")) {
                final String response = "@" + voter.getUsername() + " " +
                                        "Welcome to StockStream TV. Anyone can broadcast and anyone can change the channel. " +
                                        "Type !tv add or !tv remove to add or remove your channel. " +
                                        "Type !tv channel to see what's playing. " +
                                        "Type !tv channels for a list of live channels. " +
                                        "Type !tv play <channel> to change the channel. " +
                                        "Type !tv off to turn off the tv.";
                twitchChat.enqueueMessage(voter.getChannel(), response);
                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            if (message.equalsIgnoreCase("!tv channel") || message.equalsIgnoreCase("!song")) {
                final Channel nowPlaying = webTVSocket.getNowPlaying();
                String nowPlayingStr = "@" + voter.getUsername() + " ";

                switch (nowPlaying.getPlatform()) {
                    case CHEDDAR: {
                        nowPlayingStr += "You're watching Cheddar News from cheddar.com. Cheddar is live from 9am to 10am and Noon to 1pm.";
                        break;
                    } case PD_RADIO: {
                        nowPlayingStr += String.format("You're listening to %s streaming from http://radio.publicdomainproject.org/", nowPlaying.getName());
                        break;
                    } case OFF: {
                        nowPlayingStr += "You're not watch anything. The TV is off.";
                        break;
                    } default: {
                        nowPlayingStr += String.format("You're watching %s streaming from %s", nowPlaying.getName(), nowPlaying.getStreamURL());
                        break;
                    }
                }

                twitchChat.enqueueMessage(voter.getChannel(), nowPlayingStr);
                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            if (message.equalsIgnoreCase("!tv add")) {
                final Optional<String> streamURL = twitchAPI.isChannelStreaming(voter.getUsername());
                final Channel senderChannel = new Channel(voter.getUsername(), Platform.TWITCH, streamURL.orElse(null), false);

                final boolean isRegistered = channelRegistry.registerChannel(senderChannel);
                String response = "@" + voter.getUsername() + " ";
                if (isRegistered) {
                    response += "Success! Your twitch channel will now show up in the !tv channels when you are live";
                } else {
                    response += "Failed to register channel. Try again later.";
                }

                twitchChat.enqueueMessage(voter.getChannel(), response);
                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            if (message.equalsIgnoreCase("!tv remove")) {
                final Optional<String> streamURL = twitchAPI.isChannelStreaming(voter.getUsername());
                final Channel senderChannel = new Channel(voter.getUsername(), Platform.TWITCH, streamURL.orElse(null), false);

                final boolean isRegistered = channelRegistry.unregisterChannel(senderChannel);
                String response = "@" + voter.getUsername() + " ";
                if (isRegistered) {
                    response += "Success! Your twitch channel is removed from the !tv channels";
                } else {
                    response += "Failed to unregister channel. Try again later.";
                }

                twitchChat.enqueueMessage(voter.getChannel(), response);

                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            if (message.equalsIgnoreCase("!tv channels")) {
                final Collection<Channel> allChannels = new ArrayList<>(channelRegistry.getAllChannels());
                allChannels.removeIf(channel1 -> !channel1.isStreaming());

                final Set<String> channelNames = allChannels.stream().map(Channel::getName).collect(Collectors.toSet());
                final String response = "Live channels: " + String.join(", ", channelNames);
                twitchChat.enqueueMessage(voter.getChannel(), response);
                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            final boolean preProcessOk = preProcessingStepsOk(voter);

            if (preProcessOk && message.equalsIgnoreCase("!tv off")) {
                final Channel offChannel = new Channel("off", Platform.OFF, "/welcome.html", false);

                launchChannelChangeElection(offChannel, voter);
                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            final boolean preProcessOk = preProcessingStepsOk(voter);

            if (preProcessOk && message.toLowerCase().startsWith("!tv play")) {
                if (electionManager.isElectionActive()) {
                    final String response = "@" + voter.getUsername() + " can't change the channel until voting ends.";
                    twitchChat.enqueueMessage(voter.getChannel(), response);
                    return true;
                }

                final String[] tokens = message.toLowerCase().split(" ");

                if (tokens.length != 3) {
                    return true;
                }

                String channelName = tokens[2];
                channelName = channelName.replace("<", "");
                channelName = channelName.replace(">", "");

                final Channel votedChannel = channelRegistry.getChannel(channelName);
                if (!votedChannel.isStreaming()) {
                    return true;
                }

                launchChannelChangeElection(votedChannel, voter);

                return true;
            }
            return false;
        });

        botHooks.add((voter, message) -> {
            final Optional<String> urlForCommand = this.infoMessageCache.getURLForId(message);
            if (!urlForCommand.isPresent()) {
                return false;
            }
            twitchChat.enqueueMessage(voter.getChannel(), urlForCommand.get());
            return true;
        });

    }

    private void launchChannelChangeElection(final Channel votedChannel, final Voter voter) {
        tvChannelElection.withOutcome(new TVVote(TVAction.YES), () -> webTVSocket.changeChannel(votedChannel))
                         .withExpirationDate(DateUtils.addSeconds(new Date(), Config.ELECTION_LENGTH_SECONDS).getTime());

        tvChannelElection.receiveVote("!tv yes", voter);

        electionManager.setNextChannel(votedChannel);
    }

}
