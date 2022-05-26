package org.knowm.xchange.kucoinfutures.dto.ws.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenTunnelRequest extends BaseRequest {

  public final String newTunnelId;
  public final Boolean response;

  public OpenTunnelRequest() {
    super("openTunnel");
    this.newTunnelId = "tun" + id.toString();
    this.response = Boolean.FALSE;
  }

  public OpenTunnelRequest(@JsonProperty("response") Boolean response) {
    super("openTunnel");
    this.newTunnelId = "tun" + id.toString();
    this.response = response;
  }
}
