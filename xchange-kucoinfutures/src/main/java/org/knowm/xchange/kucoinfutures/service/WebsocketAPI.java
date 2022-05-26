package org.knowm.xchange.kucoinfutures.service;

import java.io.IOException;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.kucoinfutures.dto.response.KucoinResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WebsocketResponse;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_KEY;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_SIGN;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_TIMESTAMP;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface WebsocketAPI {
  /** Get connection details (URL + token) for subscribing to public websocket feeds */
  @POST
  @Path("/bullet-public")
  KucoinResponse<WebsocketResponse> getPublicWebsocketDetails() throws IOException;

  /** Get connection details (URL + token) for subscribing to private websocket feeds */
  @POST
  @Path("/bullet-private")
  KucoinResponse<WebsocketResponse> getPrivateWebsocketDetails(
      @HeaderParam(API_HEADER_KEY) String apiKey,
      @HeaderParam(API_HEADER_SIGN) ParamsDigest signature,
      @HeaderParam(API_HEADER_TIMESTAMP) SynchronizedValueFactory<Long> nonce,
      @HeaderParam(APIConstants.API_HEADER_PASSPHRASE) String apiPassphrase)
      throws IOException;
}
