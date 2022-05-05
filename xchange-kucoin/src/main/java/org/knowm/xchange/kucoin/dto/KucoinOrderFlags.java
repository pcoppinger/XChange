package org.knowm.xchange.kucoin.dto;

import org.knowm.xchange.kucoin.KucoinTradeService;

/** https://docs.kucoin.com/#place-a-new-order */
public enum KucoinOrderFlags implements KucoinTradeService.KucoinOrderFlagsInterface {

  /** Post only flag, invalid when timeInForce is IOC or FOK */
  POST_ONLY
  /** Orders not displayed in order book */
  ,
  HIDDEN
  /** Only visible portion of the order is displayed in the order book */
  ,
  ICEBERG;

  private final String clientId;

  KucoinOrderFlags(String clientId) { this.clientId = clientId; }
  KucoinOrderFlags() { this.clientId = null; }

  public final String getClientId() { return clientId; }
}
