package stockstream.tv.application.spring;

import org.pircbotx.cap.EnableCapHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import stockstream.tv.application.Config;
import stockstream.tv.twitch.LiveCommands;
import stockstream.tv.twitch.TVBot;

@Configuration
public class ChatConfigBeans {

    @Bean
    public LiveCommands liveCommands() {
        return new LiveCommands();
    }

    @Bean
    public TVBot tvBot() {
        return new TVBot();
    }

    // TEST CONFIG
    public org.pircbotx.Configuration testChannelConfiguration() {
        return new org.pircbotx.Configuration.Builder().setAutoNickChange(false)
                                                       .setOnJoinWhoEnabled(false)
                                                       .setName("stockstreamui")
                                                       .addServer("localhost")
                                                       .addAutoJoinChannel("#stockstream")
                                                       .addListener(tvBot())
                                                       .addListener(liveCommands())
                                                       .setAutoReconnect(true)
                                                       .setAutoReconnectDelay(5 * 1000)
                                                       .buildConfiguration();
    }

    // PROD CONFIG
    public org.pircbotx.Configuration prodChannelConfiguration() {
        return new org.pircbotx.Configuration.Builder().setAutoNickChange(false)
                                                       .setOnJoinWhoEnabled(false)
                                                       .setCapEnabled(true)
                                                       .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                                                       .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                                                       .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                                                       .setName("stockstream")
                                                       .setServerPassword("oauth:wee12rhgskga3qf1iq4rbvq0f61i7s")
                                                       .addServer("irc.chat.twitch.tv")
                                                       .addAutoJoinChannel("#stockstream")
                                                       .addListener(tvBot())
                                                       .addListener(liveCommands())
                                                       .setAutoReconnect(true)
                                                       .setAutoReconnectDelay(5 * 1000)
                                                       .buildConfiguration();
    }

    @Bean
    public org.pircbotx.Configuration configuration() {
        switch (Config.stage) {
            case TEST: {
                return testChannelConfiguration();
            } case PROD: {
                return prodChannelConfiguration();
            } default: {
                return testChannelConfiguration();
            }
        }
    }

}
