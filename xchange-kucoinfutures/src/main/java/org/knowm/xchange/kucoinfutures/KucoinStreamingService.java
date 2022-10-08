package org.knowm.xchange.kucoinfutures;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.kucoinfutures.dto.ws.request.OpenTunnelRequest;
import org.knowm.xchange.kucoinfutures.dto.ws.request.PingRequest;
import org.knowm.xchange.kucoinfutures.dto.ws.request.SubscribeRequest;
import org.knowm.xchange.kucoinfutures.dto.ws.request.UnsubscribeRequest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class KucoinStreamingService extends JsonNettyStreamingService {

  private Disposable pingTimer = null;

  public KucoinStreamingService(String apiUrl, String token, int pingInterval, int pingTimeout) {
    super(apiUrl + "?token=" + token);
    Observable.interval(pingInterval, TimeUnit.MILLISECONDS)
        .subscribe(
            t -> {
              if (this.isSocketOpen()) {
                this.sendObjectMessage(new PingRequest());
                if (pingTimer != null) {
                  pingTimer.dispose();
                }
                pingTimer = Observable.timer(pingTimeout, TimeUnit.MILLISECONDS)
                    .subscribe(tt -> this.disconnect());
              }
            });
  }

  @Override
  protected void handleMessage(JsonNode message) {
    if (message.has("type") && message.get("type").asText().equals("pong")) {
      if (pingTimer != null) {
        pingTimer.dispose();
        pingTimer = null;
      }
    } else {
      super.handleMessage(message);
    }
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {

    return message.has("topic") ? message.get("topic").asText() : null;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    Boolean privateChannel = (args.length >= 1 && args[0] instanceof Boolean) ? (Boolean) args[0] : Boolean.FALSE;
    Boolean response = (args.length >= 2 && args[1] instanceof Boolean) ? (Boolean) args[1] : Boolean.FALSE;
    return objectMapper.writeValueAsString(new SubscribeRequest(channelName, privateChannel, response));
  }

  @Override
  public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
    return objectMapper.writeValueAsString(new UnsubscribeRequest(channelName));
  }
}
