package org.knowm.xchange.kucoinfutures.service;

import org.knowm.xchange.service.trade.params.orders.DefaultQueryOrderParam;

public class QueryOrderParamClientOid extends DefaultQueryOrderParam {
    public QueryOrderParamClientOid(String clientOid) {
        super(clientOid);
    }
}
