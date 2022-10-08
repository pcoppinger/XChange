package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class OrderMatchEvent extends BaseEvent {

  public final Data data;

  public OrderMatchEvent(@JsonProperty("subject") String subject,
                         @JsonProperty("topic") String topic,
                         @JsonProperty("data") Data data) {

    super(subject, topic);
    this.data = data;
  }

  public static class Data {

    public final String symbol;
    public final Long sequence;
    public final String side;
    public final BigDecimal matchSize;
    public final BigDecimal size;
    public final BigDecimal price;
    public final String takerOrderId;
    public final String takerUserId;
    public final String makerOrderId;
    public final String makerUserId;
    public final String tradeId;
    public final Long ts;

    public Data(@JsonProperty("symbol") String symbol,
                @JsonProperty("sequence") Long sequence,
                @JsonProperty("side") String side,
                @JsonProperty("matchSize") BigDecimal matchSize,
                @JsonProperty("size") BigDecimal size,
                @JsonProperty("price") BigDecimal price,
                @JsonProperty("takerOrderId") String takerOrderId,
                @JsonProperty("takerUserId") String takerUserId,
                @JsonProperty("makerOrderId") String makerOrderId,
                @JsonProperty("makerUserId") String makerUserId,
                @JsonProperty("tradeId") String tradeId,
                @JsonProperty("ts") Long ts) {

      this.symbol = symbol;
      this.sequence = sequence;
      this.side = side;
      this.matchSize = matchSize;
      this.size = size;
      this.price = price;
      this.takerOrderId = takerOrderId;
      this.takerUserId = takerUserId;
      this.makerOrderId = makerOrderId;
      this.makerUserId = makerUserId;
      this.tradeId = tradeId;
      this.ts = ts;

    }
  }
}
