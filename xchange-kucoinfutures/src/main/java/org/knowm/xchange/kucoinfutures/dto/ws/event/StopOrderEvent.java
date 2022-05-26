package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class StopOrderEvent extends BaseEvent {

  public final String userId;
  public final Data data;

  public StopOrderEvent(@JsonProperty("userId") String userId,
                        @JsonProperty("subject") String subject,
                        @JsonProperty("topic") String topic,
                        @JsonProperty("data") Data data) {

    super(subject, topic);
    this.userId = userId;
    this.data = data;
  }

  public static class Data {

    public final String orderId;
    public final String symbol;
    public final String type;
    public final String orderType;
    public final String side;
    public final BigDecimal size;
    public final BigDecimal orderPrice;
    public final String stop;
    public final BigDecimal stopPrice;
    public final String stopPriceType;
    public final Boolean triggerSuccess;
    public final String error;
    public final Long createdAt;
    public final Long ts;

    public Data(@JsonProperty("orderId") String orderId,
                @JsonProperty("symbol") String symbol,
                @JsonProperty("type") String type,
                @JsonProperty("orderType") String orderType,
                @JsonProperty("side") String side,
                @JsonProperty("size") BigDecimal size,
                @JsonProperty("orderPrice") BigDecimal orderPrice,
                @JsonProperty("stop") String stop,
                @JsonProperty("stopPrice") BigDecimal stopPrice,
                @JsonProperty("stopPriceType") String stopPriceType,
                @JsonProperty("triggerSuccess") Boolean triggerSuccess,
                @JsonProperty("error") String error,
                @JsonProperty("createdAt") Long createdAt,
                @JsonProperty("ts") Long ts) {

      this.orderId = orderId;
      this.symbol = symbol;
      this.type = type;
      this.orderType = orderType;
      this.side = side;
      this.size = size;
      this.orderPrice = orderPrice;
      this.stop = stop;
      this.stopPrice = stopPrice;
      this.stopPriceType = stopPriceType;
      this.triggerSuccess = triggerSuccess;
      this.error = error;
      this.createdAt = createdAt;
      this.ts = ts;

    }
  }
}
