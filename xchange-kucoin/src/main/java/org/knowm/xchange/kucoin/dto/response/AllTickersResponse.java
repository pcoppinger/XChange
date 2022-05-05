package org.knowm.xchange.kucoin.dto.response;

import lombok.Data;

@Data
public class AllTickersResponse {
  private long time;
  private AllTickersTickerResponse[] ticker;
}
