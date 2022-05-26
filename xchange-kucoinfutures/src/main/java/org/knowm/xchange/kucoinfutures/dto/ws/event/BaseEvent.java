package org.knowm.xchange.kucoinfutures.dto.ws.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.atomic.AtomicInteger;

class BaseEvent {

  public final String topic;
  public final String subject;

  protected BaseEvent(String topic,
                      String subject) {

    this.topic = topic;
    this.subject = subject;
  }
}
