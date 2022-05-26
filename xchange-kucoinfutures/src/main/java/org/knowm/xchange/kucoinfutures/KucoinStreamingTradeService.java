package org.knowm.xchange.kucoinfutures;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.StopOrderEvent;

class KucoinStreamingTradeService implements StreamingTradeService {

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private final KucoinStreamingService service;
  private final Exchange exchange;

  public KucoinStreamingTradeService(Exchange exchange, KucoinStreamingService service) {
    this.exchange = exchange;
    this.service = service;
  }

  @Override
  public Observable<UserTrade> getUserTrades(Instrument instrument, Object... args) {
    String channelName = KucoinStreamingAdapters.adaptInstrumentToTradeTopic(instrument);
    return service
        .subscribeChannel(channelName, Boolean.TRUE, Boolean.TRUE)
        .map(node -> mapper.treeToValue(node, OrderEvent.class))
        .filter(event -> "private".equals(event.channelType) && "match".equals(event.data.type))
        .map(event -> KucoinStreamingAdapters.adaptUserTrade(exchange, event))
        .filter(userTrade -> instrument == null || instrument.equals(userTrade.getInstrument()));
  }

  @Override
  public Observable<Order> getOrderChanges(Instrument instrument, Object... args) {
      String channelName = KucoinStreamingAdapters.adaptInstrumentToTradeTopic(instrument);
    return service
        .subscribeChannel(channelName, Boolean.TRUE, Boolean.TRUE)
        .filter(node -> node.get("channelType").textValue().equals("private"))
        .map(node -> KucoinStreamingAdapters.adaptOrder(exchange, mapper.treeToValue(node, OrderEvent.class)))
        .filter(order -> instrument == null || instrument.equals(order.getInstrument()));
  }

    @Override
    public Observable<StopOrder> getStopOrderChanges(Instrument instrument, Object... args) {
        String channelName = KucoinStreamingAdapters.adaptInstrumentToAdvancedOrderTopic(instrument);
        return service
                .subscribeChannel(channelName, Boolean.TRUE, Boolean.TRUE)
                .map(node -> KucoinStreamingAdapters.adaptOrder(exchange, mapper.treeToValue(node, StopOrderEvent.class)));
    }
}
