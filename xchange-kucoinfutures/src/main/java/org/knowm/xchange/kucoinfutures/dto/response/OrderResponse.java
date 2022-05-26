/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

  private String id;

  private String symbol;

  private String type;

  public String getType() {
    return this.type == null ? null : this.type.toLowerCase();
  }

  private String side;

  public String getSide() {
    return this.side == null ? null : this.side.toLowerCase();
  }

  private BigDecimal price;

  private BigDecimal size;

  private BigDecimal value;

  private BigDecimal dealValue;

  private BigDecimal dealSize;

  private String stp;

  private String stop;

  public String getStop() {
    return this.stop == null ? null : this.stop.toLowerCase();
  }

  private String stopPriceType;

  @JsonProperty("stopTriggered")
  private boolean stopTriggered;

  private BigDecimal stopPrice;

  private String timeInForce;

  @JsonProperty("postOnly")
  private boolean postOnly;

  @JsonProperty("hidden")
  private boolean hidden;

  @JsonProperty("iceberg")
  private boolean iceberg;

  private BigDecimal leverage;

  @JsonProperty("forceHold")
  private boolean forceHold;

  @JsonProperty("closeOrder")
  private boolean closeOrder;

  private BigDecimal visibleSize;

  private String clientOid;

  private String remark;

  private String tags;

  @JsonProperty("isActive")
  private boolean isActive;

  @JsonProperty("cancelExist")
  private boolean cancelExist;

  private Date createdAt;

  private Date updatedAt;

  private Date endAt;

  private Long orderTime;

  private String settleCurrency;

  private String status;

  private BigDecimal filledSize;

  private BigDecimal filledValue;

  @JsonProperty("reduceOnly")
  private boolean reduceOnly;
}
