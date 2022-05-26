package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawalQuotaResponse {

  private String currency;

  private String chainId;

  private BigDecimal limitAmount;

  private BigDecimal remainAmount;

  private BigDecimal usedAmount;

  private BigDecimal availableAmount;

  private BigDecimal withdrawMinFee;

  private BigDecimal innerWithdrawMinFee;

  private BigDecimal withdrawMinSize;

  @JsonProperty("isWithdrawEnabled")
  private boolean isWithdrawEnabled;

  private BigDecimal precision;

  private String withdrawalMinSize;

  private String withdrawalMinFee;
}
