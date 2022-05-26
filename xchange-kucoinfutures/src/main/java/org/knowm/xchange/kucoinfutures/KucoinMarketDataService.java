package org.knowm.xchange.kucoinfutures;

import java.io.IOException;
import java.util.*;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.response.TickerResponse;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;

public class KucoinMarketDataService extends KucoinMarketDataServiceRaw
    implements MarketDataService {

  /**
   * Set on calls to {@link #getOrderBook(Instrument, Object...)} to return the full orderbook
   * rather than the default 100 prices either side.
   */
  public static final String PARAM_FULL_ORDERBOOK = "Full_Orderbook";

  /**
   * Set on calls to {@link #getOrderBook(Instrument, Object...)} to return the shallow partial
   * orderbook depth of 20.
   */
  public static final String PARAM_PARTIAL_SHALLOW_ORDERBOOK = "Shallow_Orderbook";

  protected KucoinMarketDataService(
          KucoinExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  @Override
  public Ticker getTicker(Instrument instrument, Object... args) throws IOException {
    return KucoinAdapters.adaptTickerFull(instrument, getKucoin24hrStats(instrument)).build();
  }

  @Override
  public List<Ticker> getTickers(Params params) throws IOException {
    if (params instanceof CurrencyPairsParam) {
      Collection pairs = ((CurrencyPairsParam)params).getCurrencyPairs();
      if (pairs.size() == 1) {
        Iterator<CurrencyPair> iter = pairs.iterator();
        CurrencyPair currencyPair = iter.next();
        TickerResponse ticker = getKucoinTicker(currencyPair);
        return Arrays.asList(KucoinAdapters.adaptTicker(currencyPair, ticker));
      }
    }
    return KucoinAdapters.adaptAllTickers(getKucoinTickers());
  }

  @Override
  public OrderBook getOrderBook(Instrument instrument, Object... args) throws IOException {
    if (Arrays.asList(args).contains(PARAM_FULL_ORDERBOOK)) {
      return KucoinAdapters.adaptOrderBook(instrument, getKucoinOrderBookFull(instrument));
    } else {
      if (Arrays.asList(args).contains(PARAM_PARTIAL_SHALLOW_ORDERBOOK)) {
        return KucoinAdapters.adaptOrderBook(
            instrument, getKucoinOrderBookPartialShallow(instrument));
      }
      return KucoinAdapters.adaptOrderBook(instrument, getKucoinOrderBookPartial(instrument));
    }
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    return KucoinAdapters.adaptTrades(currencyPair, getKucoinTrades(currencyPair));
  }
}
