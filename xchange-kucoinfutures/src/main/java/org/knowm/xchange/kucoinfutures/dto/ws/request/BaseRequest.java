package org.knowm.xchange.kucoinfutures.dto.ws.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.atomic.AtomicInteger;

class BaseRequest {
  private static final AtomicInteger refCount = new AtomicInteger();

  public final String type;
  public final Integer id;

  protected BaseRequest(@JsonProperty("type") String type) {
    this.type = type;
    this.id = refCount.incrementAndGet();
  }

}
