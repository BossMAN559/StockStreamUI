package stockstream.tv.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Filter;
import spark.Spark;
import stockstream.database.RobinhoodAccountRegistry;
import stockstream.api.StockStreamAPI;
import stockstream.tv.cache.InfoMessageCache;
import stockstream.tv.logic.AssetComputer;
import stockstream.tv.logic.elections.ElectionManager;
import stockstream.tv.twitch.LiveCommands;
import stockstream.tv.twitch.WebTVSocket;
import stockstream.util.JSONUtil;

import java.util.HashMap;

import static spark.Spark.port;


@Slf4j
public class SparkServer {

    @Autowired
    private WebTVSocket webTVSocket;

    @Autowired
    private InfoMessageCache infoMessageCache;

    @Autowired
    private ElectionManager electionManager;

    @Autowired
    private RobinhoodAccountRegistry robinhoodAccountRegistry;

    @Autowired
    private AssetComputer assetComputer;

    @Autowired
    private LiveCommands liveCommands;

    @Autowired
    private StockStreamAPI stockStreamAPI;

    private static final HashMap<String, String> corsHeaders = new HashMap<>();

    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    private void apply() {
        Filter filter = (request, response) -> corsHeaders.forEach(response::header);
        Spark.after(filter);
    }


    public void startServer(final int port) {
        port(port);
        Spark.webSocket("/webTVSocket", this.webTVSocket);
        Spark.webSocket("/liveCommands", this.liveCommands);
        Spark.staticFileLocation("/public");
        apply();

        Spark.get("/news", (request, response) -> JSONUtil.serializeObject(infoMessageCache.getNext()).orElse("{}"));
        Spark.get("/voteCounts", (request, response) -> stockStreamAPI.getElections().toString());
        Spark.get("/account", (request, response) -> stockStreamAPI.queryEndpoint(StockStreamAPI.ACCOUNT_API).toString());
        Spark.get("/gameState", (request, response) -> stockStreamAPI.queryEndpoint(StockStreamAPI.GAMESTATE_API).toString());
        Spark.get("/assets", (request, response) -> JSONUtil.serializeObject(assetComputer.getAllAssets()).orElse("{}"));
        Spark.get("/channelElection", (request, response) -> JSONUtil.serializeObject(electionManager.getTvChannelElection()).orElse("{}"));
        Spark.get("/nextChannel", (request, response) -> JSONUtil.serializeObject(electionManager.getNextChannel()).orElse("{}"));
        Spark.get("/scores", (request, response) -> JSONUtil.serializeObject(stockStreamAPI.getScores()).orElse("{}"));
        Spark.get("/customStreamURL", (request, response) -> JSONUtil.serializeObject(Config.CUSTOM_PRESENTATION_URL).orElse(""));

        Spark.init();
    }

}
