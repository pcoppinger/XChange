/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.request;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

/**
 * 订单创建对象
 *
 * @author 屈亮
 * @since 2018-09-17
 */
@Getter
@Builder
public class OrderCreateApiRequest {

  /** Unique order id selected by you to identify your order e.g. UUID */
  private final String clientOid;

  /** buy or sell */
  private final String side;

  /** a valid trading symbol code. e.g. XBTUSDTM */
  private final String symbol;

  /** [optional] limit or market (default is limit) */
  @Builder.Default private final String type = "limit";

  /** Leverage of the order */
  private final BigDecimal leverage;

  /** [optional] remark for the order, length cannot exceed 100 utf8 characters */
  private final String remark;

  /** [optional] Either down or up. Requires stopPrice and stopPriceType to be defined */
  @Builder.Default private final String stop = "";

  /** [optional] Either TP, IP or MP, Need to be defined if stop is specified */
  private final String stopPriceType;

  /** [optional] Need to be defined if stop is specified */
  private final BigDecimal stopPrice;

  /** [optional] A mark to reduce the position size only. Set to false by default.
   * Need to set the position size when reduceOnly is true. */
  @Builder.Default private final boolean reduceOnly = false;

  /** [optional] A mark to close the position. Set to false by default.
   * It will close all the positions when closeOrder is true. */
  @Builder.Default private final boolean closeOrder = false;

  /** [optional] A mark to forcefully hold the funds for an order, even though it's an order to
   * reduce the position size. This helps the order stay on the order book and not get canceled
   * when the position size changes. Set to false by default. */
  @Builder.Default private final boolean forceHold = false;

  /** price per base currency */
  private final BigDecimal price;

  /** amount of base currency to buy or sell */
  private final BigDecimal size;

  /** [optional] GTC, GTT, IOC, or FOK (default is GTC) */
  @Builder.Default private final String timeInForce = "GTC";

  /** [optional] ** Post only flag */
  private final boolean postOnly;

  /** [optional] Orders not displayed in order book */
  private final boolean hidden;

  /** [optional] Only visible portion of the order is displayed in the order book */
  private final boolean iceberg;

  /** [optional] The maximum visible size of an iceberg order */
  private final BigDecimal visibleSize;
}
