package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.kucoinfutures.dto.ws.event.BaseEvent;

import java.math.BigDecimal;

public class TickerEvent extends BaseEvent {

  public final Data data;

  public TickerEvent(@JsonProperty("subject") String subject,
                     @JsonProperty("topic") String topic,
                     @JsonProperty("data") Data data) {

    super(subject, topic);
    this.data = data;
  }

  public static class Data {

    public final String symbol;
    public final BigDecimal bestBidPrice;
    public final BigDecimal bestAskPrice;
    public final BigDecimal bestBidSize;
    public final BigDecimal bestAskSize;
    public final Long ts;

    public Data(@JsonProperty("symbol") String symbol,
                @JsonProperty("bestBidPrice") BigDecimal bestBidPrice,
                @JsonProperty("bestAskPrice") BigDecimal bestAskPrice,
                @JsonProperty("bestBidSize") BigDecimal bestBidSize,
                @JsonProperty("bestAskSize") BigDecimal bestAskSize,
                @JsonProperty("ts") Long ts) {

      this.symbol = symbol;
      this.bestBidPrice = bestBidPrice;
      this.bestAskPrice = bestAskPrice;
      this.bestBidSize = bestBidSize;
      this.bestAskSize = bestAskSize;
      this.ts = ts;
    }
  }
}
