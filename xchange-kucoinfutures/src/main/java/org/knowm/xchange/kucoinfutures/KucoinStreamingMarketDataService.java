package org.knowm.xchange.kucoinfutures;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.ws.event.TradeEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderBookEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

class KucoinStreamingMarketDataService implements StreamingMarketDataService {

  private static final Logger logger =
      LoggerFactory.getLogger(KucoinStreamingMarketDataService.class);

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private final KucoinStreamingService service;
  private final Exchange exchange;

  public KucoinStreamingMarketDataService(Exchange exchange, KucoinStreamingService service) {
      this.exchange = exchange;
      this.service = service;
  }

  private static void updateOrderBook(Map<BigDecimal, LimitOrder> book, LimitOrder order) {
      if (order.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0) {
        book.put(order.getLimitPrice(), order);
      } else {
        book.remove(order.getLimitPrice());
      }
  }

  private static OrderBook handleOrderBookEvent(OrderBookEvent event,
                                                Instrument instrument,
                                                Map<BigDecimal, LimitOrder> bids,
                                                Map<BigDecimal, LimitOrder> asks) {

    updateOrderBook(bids, KucoinStreamingAdapters.toLimitOrder(event, instrument));

    long seconds = TimeUnit.NANOSECONDS.toSeconds(event.data.timestamp);
    long nanos = event.data.timestamp - TimeUnit.SECONDS.toNanos(seconds);

    return new OrderBook(Date.from(Instant.ofEpochSecond(seconds, nanos)),
            Lists.newArrayList(asks.values()), Lists.newArrayList(bids.values()));
  }

  @Override
  public Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
    final SortedMap<BigDecimal, LimitOrder> bids = Maps.newTreeMap((o1, o2) -> Math.negateExact(o1.compareTo(o2)));
    final SortedMap<BigDecimal, LimitOrder> asks = Maps.newTreeMap(BigDecimal::compareTo);
    String channelName = KucoinStreamingAdapters.adaptInstrumentToBookTopic(instrument);
    return service
        .subscribeChannel(channelName)
        .doOnError(throwable -> logger.warn("encountered error while subscribing to channel " + channelName, throwable))
        .map(node -> {
              OrderBookEvent event = mapper.treeToValue(node, OrderBookEvent.class);
              return handleOrderBookEvent(event, instrument, bids, asks);
        })
        .filter(orderBook -> !orderBook.getBids().isEmpty() && !orderBook.getAsks().isEmpty());
  }

  @Override
  public Observable<Ticker> getTicker(Instrument instrument, Object... args) {
    String channelName = KucoinStreamingAdapters.adaptInstrumentToTickerTopic(instrument);
    return service
            .subscribeChannel(channelName, Boolean.FALSE, Boolean.TRUE)
            .doOnError(throwable -> logger.warn("encountered error while subscribing to channel " + channelName, throwable))
            .map(node -> KucoinStreamingAdapters.adaptTickerEvent(exchange, mapper.treeToValue(node, TradeEvent.class)));
  }

  @Override
  public Observable<Trade> getTrades(Instrument instrument, Object... args) {
      String channelName = KucoinStreamingAdapters.adaptInstrumentToTradeTopic(instrument);
      return service
              .subscribeChannel(channelName, Boolean.FALSE, Boolean.TRUE)
              .doOnError(throwable -> logger.warn("encountered error while subscribing to channel " + channelName, throwable))
              .map(node -> KucoinStreamingAdapters.adaptUserTrade(exchange, mapper.treeToValue(node, OrderEvent.class)));
  }
}
