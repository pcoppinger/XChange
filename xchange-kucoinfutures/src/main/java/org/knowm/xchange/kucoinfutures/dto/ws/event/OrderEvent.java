package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent extends BaseEvent {

  public final String type;
  public final String channelType;
  public final Data data;

  public OrderEvent(@JsonProperty("topic") String topic,
                    @JsonProperty("subject") String subject,
                    @JsonProperty("type") String type,
                    @JsonProperty("channelType") String channelType,
                    @JsonProperty("data") Data data) {

    super(topic, subject);
    this.type = type;
    this.channelType = channelType;
    this.data = data;
  }

  public static class Data {

    public final String orderId;
    public final String symbol;
    public final String type;
    public final String status;
    public final BigDecimal matchSize;
    public final BigDecimal matchPrice;
    public final String orderType;
    public final String side;
    public final BigDecimal price;
    public final BigDecimal size;
    public final BigDecimal remainSize;
    public final BigDecimal filledSize;
    public final BigDecimal canceledSize;
    public final String tradeId;
    public final String clientOid;
    public final Long orderTime;
    public final BigDecimal oldSize;
    public final String liquidity;
    public final Long ts;
    
    public Data(@JsonProperty("orderId") String orderId,
                @JsonProperty("symbol") String symbol,
                @JsonProperty("type") String type,
                @JsonProperty("status") String status,
                @JsonProperty("matchSize") BigDecimal matchSize,
                @JsonProperty("matchPrice") BigDecimal matchPrice,
                @JsonProperty("orderType") String orderType,
                @JsonProperty("side") String side,
                @JsonProperty("price") BigDecimal price,
                @JsonProperty("size") BigDecimal size,
                @JsonProperty("remainSize") BigDecimal remainSize,
                @JsonProperty("filledSize") BigDecimal filledSize,
                @JsonProperty("canceledSize") BigDecimal canceledSize,
                @JsonProperty("tradeId") String tradeId,
                @JsonProperty("clientOid") String clientOid,
                @JsonProperty("orderTime") Long orderTime,
                @JsonProperty("oldSize") BigDecimal oldSize,
                @JsonProperty("liquidity") String liquidity,
                @JsonProperty("ts") Long ts) {

      this.orderId = orderId;
      this.symbol = symbol;
      this.type = type;
      this.status = status;
      this.matchSize = matchSize;
      this.matchPrice = matchPrice;
      this.orderType = orderType;
      this.side = side;
      this.price = price;
      this.size = size;
      this.remainSize = remainSize;
      this.filledSize = filledSize;
      this.canceledSize = canceledSize;
      this.tradeId = tradeId;
      this.clientOid = clientOid;
      this.orderTime = orderTime;
      this.oldSize = oldSize;
      this.liquidity = liquidity;
      this.ts = ts;
    }
  }
}
