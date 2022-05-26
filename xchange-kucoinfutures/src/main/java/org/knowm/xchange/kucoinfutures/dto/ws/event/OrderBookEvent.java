package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBookEvent extends BaseEvent {

  public final String type;

  public final Data data;

  public OrderBookEvent(@JsonProperty("topic") String topic,
                        @JsonProperty("subject") String subject,
                        @JsonProperty("type") String type,
                        @JsonProperty("data") Data data) {

    super(topic, subject);
    this.type = type;
    this.data = data;
  }

  public static class Data {

    public final Long sequence;
    public final String change;
    public final Long timestamp;

    public Data(@JsonProperty("sequence") Long sequence,
                @JsonProperty("change") String change,
                @JsonProperty("timestamp") Long timestamp) {
      this.sequence = sequence;
      this.change = change;
      this.timestamp = timestamp;
    }
  }
}
