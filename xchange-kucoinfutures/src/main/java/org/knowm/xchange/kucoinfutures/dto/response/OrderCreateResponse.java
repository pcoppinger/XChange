/** Copyright 2019 Mek Global Limited. */
package org.knowm.xchange.kucoinfutures.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreateResponse {

  private String orderId;
}
