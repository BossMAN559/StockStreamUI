package stockstream.tv.twitch;


import stockstream.data.Voter;

public interface BotHook {
    public boolean processMessage(final Voter voter, final String message);
}