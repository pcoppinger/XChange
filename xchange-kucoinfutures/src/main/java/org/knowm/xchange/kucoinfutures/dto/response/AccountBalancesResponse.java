/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;

/** Created by tao.mao on 2018/11/15. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBalancesResponse {

  private BigDecimal accountEquity;

  private BigDecimal unrealisedPNL;

  private BigDecimal marginBalance;

  private BigDecimal positionMargin;

  private BigDecimal orderMargin;

  private BigDecimal frozenFunds;

  private BigDecimal availableBalance;

  private String currency;
}
