/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;

/** Created by chenshiwei on 2019/1/10. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerResponse {

  private String sequence;

  private String symbol;

  private String side;

  private BigDecimal size;

  private BigDecimal price;

  private BigDecimal bestBidSize;

  private BigDecimal bestBidPrice;

  private BigDecimal bestAskSize;

  private BigDecimal bestAskPrice;

  private String tradeId;

  private long ts;
}
