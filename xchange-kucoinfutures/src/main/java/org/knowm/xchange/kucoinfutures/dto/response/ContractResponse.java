/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Created by devin@kucoin.com on 2018-12-27. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractResponse {

  private String symbol;

  private String rootSymbol;

  private Timestamp firstOpenDate;

  private Timestamp expireDate;

  private Timestamp settleDate;

  private String baseCurrency;

  private String quoteCurrency;

  private String settleCurrency;

  private BigDecimal maxOrderQty;

  private BigDecimal maxPrice;

  private BigDecimal lotSize;

  private BigDecimal tickSize;

  private BigDecimal indexPriceTickSize;

  private BigDecimal multiplier;

  private BigDecimal initialMargin;

  private BigDecimal maintainMargin;

  private BigDecimal maxRiskLimit;

  private BigDecimal minRiskLimit;

  private BigDecimal riskStep;

  private BigDecimal makerFeeRate;

  private BigDecimal takerFeeRate;

  private BigDecimal takerFixFee;

  private BigDecimal makerFixFee;

  private BigDecimal settlementFee;

  @JsonProperty("isDeleverage")
  private boolean isDeleverage;

  @JsonProperty("isQuanto")
  private boolean isQuanto;

  @JsonProperty("isInverse")
  private boolean isInverse;

  private String markMethod;

  private String fairMethod;

  private String fundingBaseSymbol;

  private String fundingQuoteSymbol;

  private String fundingRateSymbol;

  private String indexSymbol;

  private String settlementSymbol;

  private String status;

  private BigDecimal fundingFeeRate;

  private BigDecimal predictedFundingFeeRate;

  private String openInterest;

  private BigDecimal turnoverOf24h;

  private BigDecimal volumeOf24h;

  private BigDecimal markPrice;

  private BigDecimal indexPrice;

  private BigDecimal lastTradePrice;

  private Long nextFundingRateTime;

  private Long maxLeverage;

  private String[] sourceExchanges;

  private String premiumsSymbol1M;

  private String premiumsSymbol8H;

  private String fundingBaseSymbol1M;

  private String fundingQuoteSymbol1M;

  private BigDecimal lowPrice;

  private BigDecimal highPrice;

  private BigDecimal priceChgPct;

  private BigDecimal priceChg;
}
