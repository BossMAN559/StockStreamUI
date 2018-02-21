package stockstream.tv.logic;


import com.cheddar.robinhood.RobinhoodAPI;
import com.cheddar.robinhood.data.Quote;
import com.cheddar.robinhood.exception.RobinhoodException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.data.Account;
import stockstream.data.AssetNode;
import stockstream.database.Asset;
import stockstream.api.StockStreamAPI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class AssetComputer {

    @Autowired
    private RobinhoodAPI broker;

    @Autowired
    private StockStreamAPI stockStreamAPI;

    public Collection<Asset> getAllAssets() throws RobinhoodException {
        log.info("Getting owned assets.");

        final Account stockStreamAccount = stockStreamAPI.getStockStreamAccount();

        final Set<String> symbols = stockStreamAccount.getAssets().stream().map(AssetNode::getSymbol).collect(Collectors.toSet());
        final Map<String, Quote> symbolToQuote = loadSymbolToQuote(symbols);

        final List<Asset> ownedAssets = new ArrayList<>();

        for (final AssetNode assetNode : stockStreamAccount.getAssets()) {
            final String symbol = assetNode.getSymbol();
            final double shares = assetNode.getShares();
            final double avgBuyPrice = assetNode.getAvgCost();

            if (shares <= 0) {
                continue;
            }

            if (!symbolToQuote.containsKey(symbol)) {
                continue;
            }

            final Quote quote = symbolToQuote.get(symbol);

            ownedAssets.add(new Asset(symbol, Math.toIntExact((long) shares), avgBuyPrice, quote));
        }

        log.info("Got {} assets from the StockStreamAPI.", ownedAssets.size());

        return ownedAssets;
    }

    public Map<String, Quote> loadSymbolToQuote(final Set<String> symbols) throws RobinhoodException {
        final Map<String, Quote> symbolToQuote = new ConcurrentHashMap<>();

        final List<Quote> quotes = this.broker.getQuotes(symbols);
        quotes.forEach(entry -> symbolToQuote.put(entry.getSymbol(), entry));

        return symbolToQuote;
    }

}
