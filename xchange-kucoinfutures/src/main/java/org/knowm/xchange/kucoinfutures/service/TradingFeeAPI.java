package org.knowm.xchange.kucoinfutures.service;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.kucoinfutures.dto.response.KucoinResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TradeFeeResponse;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_KEY;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_PASSPHRASE;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_SIGN;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_TIMESTAMP;

@Path("api/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface TradingFeeAPI {

  /**
   * Get basic fee rate of users.
   *
   * @return basic trading fee information
   */
  @GET
  @Path("/base-fee")
  KucoinResponse<TradeFeeResponse> getBaseFee(
      @HeaderParam(API_HEADER_KEY) String apiKey,
      @HeaderParam(API_HEADER_SIGN) ParamsDigest signature,
      @HeaderParam(API_HEADER_TIMESTAMP) SynchronizedValueFactory<Long> nonce,
      @HeaderParam(API_HEADER_PASSPHRASE) String apiPassphrase)
      throws IOException;

  @GET
  @Path("/trade-fees")
  KucoinResponse<List<TradeFeeResponse>> getTradeFee(
      @HeaderParam(API_HEADER_KEY) String apiKey,
      @HeaderParam(API_HEADER_SIGN) ParamsDigest signature,
      @HeaderParam(API_HEADER_TIMESTAMP) SynchronizedValueFactory<Long> nonce,
      @HeaderParam(API_HEADER_PASSPHRASE) String apiPassphrase,
      @QueryParam("symbols") String symbols)
      throws IOException;
}
