package org.knowm.xchange.kucoinfutures;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.ws.event.StopOrderEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.TradeEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderBookEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.TickerEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderEvent;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class KucoinStreamingAdapters {

  private static BigDecimal multiply(BigDecimal a, BigDecimal b) {
    return (a != null && b != null) ? a.multiply(b) : null;
  }

  public static String adaptInstrumentToBookTopic(Instrument instrument) {
    return "/contractMarket/level2:" + KucoinAdapters.adaptInstrument(instrument);
  }

  public static String adaptInstrumentToTradeTopic(Instrument instrument) {
    return "/contractMarket/tradeOrders";
  }

  public static String adaptInstrumentToTickerTopic(Instrument instrument) {
    return "/contractMarket/execution:" + KucoinAdapters.adaptInstrument(instrument);
  }

  public static String adaptInstrumentToAdvancedOrderTopic(Instrument instrument) {
    return "/contractMarket/advancedOrders";
  }

  public static UserTrade adaptUserTrade(Exchange exchange, OrderEvent event) {
    return toUserTrade(exchange, event.data);
  }

  public static Order adaptOrder(Exchange exchange, OrderEvent event) {
    if ("limit".equals(event.data.orderType)) {
      return toLimitOrder(exchange, event.data);
    } else {
      return toMarketOrder(exchange, event.data);
    }
  }

  public static StopOrder adaptOrder(Exchange exchange, StopOrderEvent event) {

    return toStopOrder(exchange, event.data);
  }

  public static Ticker adaptTickerEvent(Exchange exchange, TickerEvent event) {
    long seconds = TimeUnit.NANOSECONDS.toSeconds(event.data.ts);
    long nanos = event.data.ts - TimeUnit.SECONDS.toNanos(seconds);

    Instrument instrument = KucoinAdapters.adaptInstrument(event.data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new Ticker.Builder()
        .instrument(instrument)
        .ask(event.data.bestAskPrice)
        .askSize(multiply(event.data.bestAskSize, cpmd.getAmountStepSize()))
        .bid(event.data.bestBidPrice)
        .bidSize(multiply(event.data.bestBidSize, cpmd.getAmountStepSize()))
        .timestamp(Date.from(Instant.ofEpochSecond(seconds, nanos)))
        .build();
  }

  public static Ticker adaptTickerEvent(Exchange exchange, TradeEvent event) {
    long seconds = TimeUnit.NANOSECONDS.toSeconds(event.data.ts);
    long nanos = event.data.ts - TimeUnit.SECONDS.toNanos(seconds);

    Instrument instrument = KucoinAdapters.adaptInstrument(event.data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new Ticker.Builder()
            .instrument(instrument)
            .last(event.data.price)
            .volume(multiply(event.data.matchSize, cpmd.getAmountStepSize()))
            .timestamp(Date.from(Instant.ofEpochSecond(seconds, nanos)))
            .build();
  }

  protected static LimitOrder toLimitOrder(OrderBookEvent event, Instrument instrument) throws IllegalArgumentException {

    String [] parts = event.data.change.split(",");
    if (parts.length == 3) {
      return new LimitOrder.Builder(KucoinAdapters.adaptSide(parts[1]), instrument)
              .limitPrice(new BigDecimal(parts[0]))
              .originalAmount(new BigDecimal(parts[2]))
              .build();
    }
    throw new IllegalArgumentException("Cannot parse order book event data '" + event.data.change + "'");
  }

  protected static StopOrder toStopOrder(Exchange exchange, StopOrderEvent.Data data) {

    long ts_seconds = TimeUnit.NANOSECONDS.toSeconds(data.ts);
    long ts_nanos = data.ts - TimeUnit.SECONDS.toNanos(ts_seconds);
    long created_seconds = TimeUnit.MILLISECONDS.toSeconds(data.createdAt);

    Instrument instrument = KucoinAdapters.adaptInstrument(data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new StopOrder.Builder(
            KucoinAdapters.adaptSide(data.side),
            KucoinAdapters.adaptCurrencyPair(data.symbol))
            .id(data.orderId)
            .orderStatus(adaptStatus(data))
            .originalAmount(multiply(data.size, cpmd.getAmountStepSize()))
            .averagePrice(data.orderPrice)
            .limitPrice(data.orderPrice)
            .stopPrice(data.stopPrice)
            .stopTriggered(data.triggerSuccess)
            .userReference(data.error)
            .timestamp(Date.from(Instant.ofEpochSecond(created_seconds)))
            .updatedAt(Date.from(Instant.ofEpochSecond(ts_seconds, ts_nanos)))
            .endAt("cancel".equals(data.type) ? Date.from(Instant.ofEpochSecond(ts_seconds, ts_nanos)) : null)
            .intention(adaptIntention(data))
            .build();
  }

  protected static LimitOrder toLimitOrder(Exchange exchange, OrderEvent.Data data) {

    long update_seconds = TimeUnit.NANOSECONDS.toSeconds(data.ts);
    long update_nanos = data.ts - TimeUnit.SECONDS.toNanos(update_seconds);
    long order_seconds = TimeUnit.NANOSECONDS.toSeconds(data.orderTime);
    long order_nanos = data.orderTime - TimeUnit.SECONDS.toNanos(order_seconds);

    Instrument instrument = KucoinAdapters.adaptInstrument(data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new LimitOrder.Builder(
            KucoinAdapters.adaptSide(data.side),
            KucoinAdapters.adaptCurrencyPair(data.symbol))
        .id(data.orderId)
        .originalAmount(multiply(data.size, cpmd.getAmountStepSize()))
        .timestamp(Date.from(Instant.ofEpochSecond(order_seconds, order_nanos)))
        .orderStatus(adaptStatus(data))
        .cumulativeAmount(multiply(data.filledSize, cpmd.getAmountStepSize()))
        .averagePrice(data.price)
        .updatedAt(Date.from(Instant.ofEpochSecond(update_seconds, update_nanos)))
        .endAt("done".equals(data.status) ? Date.from(Instant.ofEpochSecond(update_seconds, update_nanos)) : null)
        .limitPrice(data.price)
        .build();
  }

  protected static MarketOrder toMarketOrder(Exchange exchange, OrderEvent.Data data) {

    long update_seconds = TimeUnit.NANOSECONDS.toSeconds(data.ts);
    long update_nanos = data.ts - TimeUnit.SECONDS.toNanos(update_seconds);
    long order_seconds = TimeUnit.NANOSECONDS.toSeconds(data.orderTime);
    long order_nanos = data.orderTime - TimeUnit.SECONDS.toNanos(order_seconds);

    Instrument instrument = KucoinAdapters.adaptInstrument(data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new MarketOrder.Builder(
            KucoinAdapters.adaptSide(data.side),
            KucoinAdapters.adaptCurrencyPair(data.symbol))
            .id(data.orderId)
            .originalAmount(multiply(data.size, cpmd.getAmountStepSize()))
            .timestamp(Date.from(Instant.ofEpochSecond(order_seconds, order_nanos)))
            .orderStatus(adaptStatus(data))
            .cumulativeAmount(multiply(data.filledSize, cpmd.getAmountStepSize()))
            .averagePrice(data.price)
            .updatedAt(Date.from(Instant.ofEpochSecond(update_seconds, update_nanos)))
            .endAt("done".equals(data.status) ? Date.from(Instant.ofEpochSecond(update_seconds, update_nanos)) : null)
            .build();
  }

  protected static UserTrade toUserTrade(Exchange exchange, OrderEvent.Data data) {
    long ts_seconds = TimeUnit.NANOSECONDS.toSeconds(data.ts);
    long ts_nanos = data.ts - TimeUnit.SECONDS.toNanos(ts_seconds);

    Instrument instrument = KucoinAdapters.adaptInstrument(data.symbol);
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

    return new UserTrade.Builder()
            .type(KucoinAdapters.adaptSide(data.side))
            .originalAmount(multiply(data.size, cpmd.getAmountStepSize()))
            .currencyPair(KucoinAdapters.adaptCurrencyPair(data.symbol))
            .price(data.price)
            .timestamp(Date.from(Instant.ofEpochSecond(ts_seconds, ts_nanos)))
            .id(data.tradeId)
            .orderId(data.orderId)
            .orderUserReference(data.clientOid)
            .build();
  }

  protected static Order.OrderStatus adaptStatus(StopOrderEvent.Data data) {
    if ("open".equals(data.type)) {
      return Order.OrderStatus.NEW;
    } else if ("triggered".equals(data.type)) {
      return Order.OrderStatus.OPEN;
    } else if ("cancel".equals(data.type)) {
      return Order.OrderStatus.CANCELED;
    } else {
      return Order.OrderStatus.UNKNOWN;
    }
  }

  protected static StopOrder.Intention adaptIntention(StopOrderEvent.Data data) {
    if ("buy".equals(data.side)) {
      return "up".equals(data.stop) ? StopOrder.Intention.TAKE_PROFIT : StopOrder.Intention.STOP_LOSS;
    } else {
      return "up".equals(data.stop) ? StopOrder.Intention.STOP_LOSS : StopOrder.Intention.TAKE_PROFIT;
    }
  }

  protected static Order.OrderStatus adaptStatus(OrderEvent.Data data) {
    if ("open".equals(data.type)) {
      return Order.OrderStatus.NEW;
    } else if ("match".equals(data.type)) {
      return Order.OrderStatus.OPEN;
    } else if ("filled".equals(data.type)) {
        return Order.OrderStatus.FILLED;
    } else if ("canceled".equals(data.type)) {
      if (data.filledSize.equals(BigDecimal.ZERO)) {
        return Order.OrderStatus.CANCELED;
      } else {
        return Order.OrderStatus.PARTIALLY_CANCELED;
      }
    } else if ("update".equals(data.type)) {
      if (data.filledSize.equals(BigDecimal.ZERO)) {
        return Order.OrderStatus.OPEN;
      } else {
        return Order.OrderStatus.PARTIALLY_FILLED;
      }
    } else {
      return Order.OrderStatus.UNKNOWN;
    }
  }
}
