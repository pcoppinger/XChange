package org.knowm.xchange.kucoinfutures.service;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.kucoinfutures.dto.response.HistOrdersResponse;
import org.knowm.xchange.kucoinfutures.dto.response.KucoinResponse;
import org.knowm.xchange.kucoinfutures.dto.response.Pagination;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_KEY;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_PASSPHRASE;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_SIGN;
import static org.knowm.xchange.kucoinfutures.service.APIConstants.API_HEADER_TIMESTAMP;

@Path("/api/v1/hist-orders")
@Produces(MediaType.APPLICATION_JSON)
public interface HistOrdersAPI {

  /**
   * Get a list of recent fills.
   *
   * @param symbol [optional] Limit list of fills to this orderId
   * @param side [optional] buy or sell
   * @param startAt [optional] Start time. unix timestamp calculated in milliseconds, the creation
   *     time queried shall posterior to the start time.
   * @param endAt [optional] End time. unix timestamp calculated in milliseconds, the creation time
   *     queried shall prior to the end time.
   * @param pageSize [optional] The page size.
   * @param currentPage [optional] The page to select.
   * @return Trades.
   */
  @GET
  KucoinResponse<Pagination<HistOrdersResponse>> queryHistOrders(
      @HeaderParam(API_HEADER_KEY) String apiKey,
      @HeaderParam(API_HEADER_SIGN) ParamsDigest signature,
      @HeaderParam(API_HEADER_TIMESTAMP) SynchronizedValueFactory<Long> nonce,
      @HeaderParam(API_HEADER_PASSPHRASE) String apiPassphrase,
      @QueryParam("symbol") String symbol,
      @QueryParam("side") String side,
      @QueryParam("startAt") Long startAt,
      @QueryParam("endAt") Long endAt,
      @QueryParam("pageSize") Integer pageSize,
      @QueryParam("currentPage") Integer currentPage)
      throws IOException;
}
