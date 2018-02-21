package stockstream.tv.application.spring;

import com.cheddar.robinhood.RobinhoodAPI;
import com.cheddar.robinhood.client.RobinhoodClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import stockstream.http.HTTPClient;
import stockstream.spring.DatabaseBeans;
import stockstream.spring.LogicBeans;
import stockstream.spring.TwitchBeans;
import stockstream.api.StockStreamAPI;
import stockstream.tv.application.Config;
import stockstream.tv.application.SparkServer;
import stockstream.tv.application.Stage;
import stockstream.tv.cache.InfoMessageCache;
import stockstream.tv.information.NewsAPI;
import stockstream.tv.information.Twitter;
import stockstream.tv.information.Whitehouse;
import stockstream.tv.logic.AssetComputer;
import stockstream.tv.logic.channel.ChannelRefresher;
import stockstream.tv.logic.channel.ChannelRegistry;
import stockstream.tv.logic.elections.ElectionManager;
import stockstream.tv.logic.elections.TVChannelElection;
import stockstream.tv.twitch.WebTVSocket;

@Import({DatabaseBeans.class,
         LogicBeans.class,
         TwitchBeans.class,
         ChatConfigBeans.class})
@Configuration
public class AppContext {

    @Bean
    public RobinhoodAPI robinhoodAPI() {
        return new RobinhoodClient(null, null);
    }

    @Bean
    public StockStreamAPI stockStreamAPI() {
        return new StockStreamAPI();
    }

    @Bean
    public AssetComputer assetComputer() {
        return new AssetComputer();
    }

    @Bean
    public Whitehouse whitehouse() {
        return new Whitehouse();
    }

    @Bean
    public InfoMessageCache infoMessageCache() {
        return new InfoMessageCache();
    }

    @Bean
    public NewsAPI newsAPI() {
        return new NewsAPI();
    }

    @Bean
    public Twitter twitter() {
        return new Twitter();
    }

    @Bean
    public WebTVSocket webTVSocket() {
        return new WebTVSocket();
    }

    @Bean
    public ChannelRegistry channelRegistry() {
        return new ChannelRegistry();
    }

    @Bean
    public TVChannelElection tvChannelElection() {
        return new TVChannelElection();
    }


    @Bean
    public ElectionManager electionManager() {
        return new ElectionManager();
    }

    @Bean
    public ChannelRefresher channelRefresher() {
        return new ChannelRefresher();
    }

    @Bean
    public HTTPClient httpClient() {
        return new HTTPClient(Stage.TEST.equals(Config.stage));
    }

    @Bean
    public SparkServer sparkServer() {
        return new SparkServer();
    }

}
