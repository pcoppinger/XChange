package org.knowm.xchange.kucoinfutures.dto.ws.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscribeRequest extends BaseRequest {

  public final String topic;
  public final Boolean privateChannel;
  public final Boolean response;

  public SubscribeRequest(@JsonProperty("topic") String topic) {
    super("subscribe");
    this.topic = topic;
    this.privateChannel = Boolean.FALSE;
    this.response = Boolean.FALSE;
  }

  public SubscribeRequest(@JsonProperty("topic") String topic,
                          @JsonProperty("privateChannel") Boolean privateChannel,
                          @JsonProperty("response") Boolean response) {
    super("subscribe");
    this.topic = topic;
    this.privateChannel = privateChannel;
    this.response = response;
  }
}
