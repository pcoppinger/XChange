package org.knowm.xchange.kucoinfutures;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.NettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderChangeEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.StopOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class KucoinStreamingTradeService implements StreamingTradeService {

  private static final Logger logger =
          LoggerFactory.getLogger(KucoinStreamingTradeService.class);

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private final KucoinStreamingService service;
  private final Exchange exchange;

  private final Map<Instrument, Observable<OrderChangeEvent>> channels = new ConcurrentHashMap<>();

  public KucoinStreamingTradeService(Exchange exchange, KucoinStreamingService service) {
    this.exchange = exchange;
    this.service = service;
  }

  @Override
  public Observable<UserTrade> getUserTrades(Instrument instrument, Object... args) {
    return getOrderChangeEvents(instrument, args)
        .filter(event -> "private".equals(event.channelType) && "match".equals(event.data.type))
        .map(event -> KucoinStreamingAdapters.adaptUserTrade(exchange, event))
        .filter(userTrade -> instrument == null || instrument.equals(userTrade.getInstrument()));
  }

  @Override
  public Observable<Order> getOrderChanges(Instrument instrument, Object... args) {
    return getOrderChangeEvents(instrument, args)
        .filter(event -> "private".equals(event.channelType))
        .map(event -> KucoinStreamingAdapters.adaptOrder(exchange, event))
        .filter(order -> instrument == null || instrument.equals(order.getInstrument()));
  }

  @Override
  public Observable<StopOrder> getStopOrderChanges(Instrument instrument, Object... args) {
    String channelName = KucoinStreamingAdapters.adaptInstrumentToAdvancedOrdersTopic(instrument);
    return service
        .subscribeChannel(channelName, Boolean.TRUE, Boolean.TRUE)
        .subscribeOn(Schedulers.io())
        .doOnError(throwable -> logger.warn("error while subscribing to channel " + channelName, throwable))
        .map(node -> KucoinStreamingAdapters.adaptOrder(exchange, mapper.treeToValue(node, StopOrderEvent.class)));
  }

  private Observable<OrderChangeEvent> getOrderChangeEvents(Instrument instrument, Object... args) {
    final String channelName = KucoinStreamingAdapters.adaptInstrumentToOrderChangeTopic(instrument);
    return channels.computeIfAbsent(instrument, channel ->
      service
          .subscribeChannel(channelName, Boolean.TRUE, Boolean.TRUE)
          .subscribeOn(Schedulers.io())
          .doOnError(throwable -> logger.warn("error while subscribing to channel " + channelName, throwable))
          .map(node -> mapper.treeToValue(node, OrderChangeEvent.class)));
  }
}
