package org.knowm.xchange.kucoinfutures;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderMatchEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderBookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class KucoinStreamingMarketDataService implements StreamingMarketDataService {

  private static final Logger logger =
      LoggerFactory.getLogger(KucoinStreamingMarketDataService.class);

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private final KucoinStreamingService service;
  protected final Exchange exchange;

  private final Map<Instrument, Observable<OrderMatchEvent>> channels = new ConcurrentHashMap<>();

  public KucoinStreamingMarketDataService(Exchange exchange, KucoinStreamingService service) {
      this.exchange = exchange;
      this.service = service;
  }

  @Override
  public Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
    final StreamingOrderBook book = new StreamingOrderBook(instrument, exchange);
    final String channelName = KucoinStreamingAdapters.adaptInstrumentToOrderBookTopic(instrument);
    return service
        .subscribeChannel(channelName)
        .subscribeOn(Schedulers.io())
        .doOnError(throwable -> logger.warn("error while subscribing to channel " + channelName, throwable))
        .map(node -> book.event(mapper.treeToValue(node, OrderBookEvent.class)))
        .filter(ob -> ob.getTimeStamp() != null && ob.getTimeStamp().getTime() > 0);
  }

  @Override
  public Observable<Ticker> getTicker(Instrument instrument, Object... args) {
    return getOrderMatchEvents(instrument, args)
        .map(event -> KucoinStreamingAdapters.adaptTicker(exchange, event));
  }

  @Override
  public Observable<Trade> getTrades(Instrument instrument, Object... args) {
    return getOrderMatchEvents(instrument, args)
        .map(event -> KucoinStreamingAdapters.adaptTrade(exchange, event));
  }

  private Observable<OrderMatchEvent> getOrderMatchEvents(Instrument instrument, Object... args) {
    final String channelName = KucoinStreamingAdapters.adaptInstrumentToOrderMatchTopic(instrument);
    return channels.computeIfAbsent(instrument, channel ->
      service
          .subscribeChannel(channelName, Boolean.FALSE, Boolean.TRUE)
          .subscribeOn(Schedulers.io())
          .doOnError(throwable -> logger.warn("error while subscribing to channel " + channelName, throwable))
          .map(node -> mapper.treeToValue(node, OrderMatchEvent.class)));
  }
}
