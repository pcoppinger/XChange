package org.knowm.xchange.kucoinfutures.dto.ws.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnsubscribeRequest extends BaseRequest {

  public final String topic;
  public final Boolean privateChannel;
  public final Boolean response;

  public UnsubscribeRequest(@JsonProperty("topic") String topic) {
    super("unsubscribe");
    this.topic = topic;
    this.privateChannel = Boolean.FALSE;
    this.response = Boolean.FALSE;
  }

  public UnsubscribeRequest(@JsonProperty("topic") String topic,
                            @JsonProperty("privateChannel") Boolean privateChannel,
                            @JsonProperty("response") Boolean response) {

    super("unsubscribe");
    this.topic = topic;
    this.privateChannel = privateChannel;
    this.response = response;
  }
}
