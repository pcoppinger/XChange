package org.knowm.xchange.kucoinfutures;

import static java.util.stream.Collectors.toCollection;
import static org.knowm.xchange.dto.Order.OrderStatus.*;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;
import static org.knowm.xchange.dto.trade.StopOrder.Intention.STOP_LOSS;
import static org.knowm.xchange.dto.trade.StopOrder.Intention.TAKE_PROFIT;
import static org.knowm.xchange.kucoinfutures.dto.KucoinOrderFlags.HIDDEN;
import static org.knowm.xchange.kucoinfutures.dto.KucoinOrderFlags.ICEBERG;
import static org.knowm.xchange.kucoinfutures.dto.KucoinOrderFlags.POST_ONLY;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.IOrderFlags;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Status;
import org.knowm.xchange.dto.account.FundingRecord.Type;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.FeeTier;
import org.knowm.xchange.dto.meta.WalletHealth;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.KucoinOrderFlags;
import org.knowm.xchange.kucoinfutures.dto.request.OrderCreateApiRequest;
import org.knowm.xchange.kucoinfutures.dto.response.AccountBalancesResponse;
import org.knowm.xchange.kucoinfutures.dto.response.AllTickersResponse;
import org.knowm.xchange.kucoinfutures.dto.response.ContractResponse;
import org.knowm.xchange.kucoinfutures.dto.response.DepositResponse;
import org.knowm.xchange.kucoinfutures.dto.response.HistOrdersResponse;
import org.knowm.xchange.kucoinfutures.dto.response.OrderBookResponse;
import org.knowm.xchange.kucoinfutures.dto.response.OrderResponse;
import org.knowm.xchange.kucoinfutures.dto.response.SymbolTickResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TickerResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TradeHistoryResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TradeResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WithdrawalQuotaResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WithdrawalResponse;

public class KucoinAdapters {

  private static final HashMap<Instrument, String> currencyToSymbol = new HashMap<>();
  private static final HashMap<String, Instrument> symbolToCurrency = new HashMap<>();

  private static BigDecimal multiply(BigDecimal a, BigDecimal b) {
    return (a != null && b != null) ? a.multiply(b) : null;
  }

  public static String adaptCurrencyPair(CurrencyPair pair) {
    return pair == null ? null : currencyToSymbol.get(pair);
  }

  public static CurrencyPair adaptCurrencyPair(String symbol) {
    return (CurrencyPair) symbolToCurrency.get(symbol);
  }

  public static String adaptInstrument(Instrument instrument) { return instrument == null ? null : currencyToSymbol.get(instrument); }

  public static Instrument adaptInstrument(String symbol) {
    return symbolToCurrency.get(symbol);
  }

  public static Ticker.Builder adaptTickerFull(Instrument instrument, SymbolTickResponse stats) {
    return new Ticker.Builder()
        .instrument(instrument)
        .bid(stats.getBuy())
        .ask(stats.getSell())
        .last(stats.getLast())
        .high(stats.getHigh())
        .low(stats.getLow())
        .volume(stats.getVol())
        .quoteVolume(stats.getVolValue())
        .open(stats.getOpen())
        .timestamp(new Date(stats.getTime()));
  }

  public static Ticker adaptTicker(CurrencyPair pair, TickerResponse stats) {
    return new Ticker.Builder()
            .instrument(pair)
            .bid(stats.getBestBidPrice())
            .ask(stats.getBestAskPrice())
            .last(stats.getPrice())
            .volume(stats.getSize())
            .timestamp(new Date(stats.getTs() / 1000000))
            .build();
  }

  public static List<Ticker> adaptAllTickers(AllTickersResponse allTickersResponse) {
    return Arrays.stream(allTickersResponse.getTicker())
        .map(
            ticker ->
                new Ticker.Builder()
                    .instrument(adaptCurrencyPair(ticker.getSymbol()))
                    .bid(ticker.getBuy())
                    .ask(ticker.getSell())
                    .last(ticker.getLast())
                    .high(ticker.getHigh())
                    .low(ticker.getLow())
                    .volume(ticker.getVol())
                    .quoteVolume(ticker.getVolValue())
                    .timestamp(new Date(allTickersResponse.getTime()))
                    .percentageChange(
                        ticker.getChangeRate().multiply(new BigDecimal("100"), new MathContext(8)))
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Imperfect implementation. Kucoin appears to enforce a base <strong>and</strong> quote min
   * <strong>and max</strong> amount that the XChange API current doesn't take account of.
   *
   * @param exchangeMetaData The static exchange metadata.
   * @param contractsResponse The Kucoin contracts
   * @param currencies The Kucoin currencies
   * @return Exchange metadata.
   */
  public static ExchangeMetaData adaptMetadata(
      ExchangeMetaData exchangeMetaData,
      List<ContractResponse> contractsResponse,
      Collection<WithdrawalQuotaResponse> currencies) {

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = exchangeMetaData.getCurrencyPairs();
    Map<Currency, CurrencyMetaData> currencyMetaDataMap = exchangeMetaData.getCurrencies();
    Map<String, CurrencyMetaData> stringCurrencyMetaDataMap = adaptCurrencyMetaData(currencies);

    symbolToCurrency.clear();
    currencyToSymbol.clear();

    for (ContractResponse contract : contractsResponse) {

      CurrencyPair pair = new CurrencyPair(contract.getBaseCurrency(), contract.getQuoteCurrency());
      symbolToCurrency.put(contract.getSymbol(), pair);
      currencyToSymbol.put(pair, contract.getSymbol());

      CurrencyPairMetaData staticMetaData = exchangeMetaData.getCurrencyPairs().get(pair);

      BigDecimal minSize = contract.getLotSize();
      BigDecimal maxSize = contract.getMaxOrderQty();
      BigDecimal minQuoteSize = contract.getTickSize();
      BigDecimal maxQuoteSize = contract.getMaxPrice();
      int baseScale = contract.getMultiplier().stripTrailingZeros().scale();
      int priceScale = contract.getTickSize().stripTrailingZeros().scale();
      FeeTier[] feeTiers = staticMetaData != null ? staticMetaData.getFeeTiers() : null;
      Currency feeCurrency = new Currency(contract.getSettleCurrency());

      CurrencyPairMetaData cpmd =
          new CurrencyPairMetaData(
              contract.getTakerFeeRate(),
              minSize,
              maxSize,
              minQuoteSize,
              maxQuoteSize,
              baseScale,
              priceScale,
              null,
              feeTiers,
              contract.getMultiplier(),
              feeCurrency,
              true);
      currencyPairs.put(pair, cpmd);

      if (!currencyMetaDataMap.containsKey(pair.base))
        currencyMetaDataMap.put(pair.base, stringCurrencyMetaDataMap.get(pair.base.getCurrencyCode()));
      if (!currencyMetaDataMap.containsKey(pair.counter))
        currencyMetaDataMap.put(pair.counter, stringCurrencyMetaDataMap.get(pair.counter.getCurrencyCode()));
    }

    return new ExchangeMetaData(
        currencyPairs,
        currencyMetaDataMap,
        exchangeMetaData.getPublicRateLimits(),
        exchangeMetaData.getPrivateRateLimits(),
        true);
  }

  static HashMap<String, CurrencyMetaData> adaptCurrencyMetaData(Collection<WithdrawalQuotaResponse> currenciesResponse) {
    HashMap<String, CurrencyMetaData> stringCurrencyMetaDataMap = new HashMap<>();
    for (WithdrawalQuotaResponse currency : currenciesResponse) {
      BigDecimal precision = currency.getPrecision();
      BigDecimal withdrawalMinFee = null;
      BigDecimal withdrawalMinSize = null;
      if (currency.getWithdrawalMinFee() != null) {
        withdrawalMinFee = new BigDecimal(currency.getWithdrawalMinFee());
      }
      if (currency.getWithdrawalMinSize() != null) {
        withdrawalMinSize = new BigDecimal(currency.getWithdrawalMinSize());
      }
      WalletHealth walletHealth = getWalletHealth(currency);
      CurrencyMetaData currencyMetaData =
          new CurrencyMetaData(
              precision.intValue(), withdrawalMinFee, withdrawalMinSize, walletHealth);
      stringCurrencyMetaDataMap.put(currency.getCurrency(), currencyMetaData);
    }
    return stringCurrencyMetaDataMap;
  }

  /**
   * @param wallet currency response which holds wallet status information
   * @return WalletHealth
   */
  private static WalletHealth getWalletHealth(WithdrawalQuotaResponse wallet) {
    WalletHealth walletHealth = WalletHealth.ONLINE;
    if (!wallet.isWithdrawEnabled()) {
      walletHealth = WalletHealth.WITHDRAWALS_DISABLED;
    }
    return walletHealth;
  }

  public static OrderBook adaptOrderBook(Exchange exchange, Instrument instrument, OrderBookResponse kc) {
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);
    Date timestamp = new Date(kc.getTime());
    List<LimitOrder> asks =
        kc.getAsks().stream()
            .map(PriceAndSize::new)
            .sorted(Ordering.natural().onResultOf(s -> s != null ? s.price : null))
            .map(s -> adaptLimitOrder(instrument, cpmd, ASK, s, timestamp))
            .collect(toCollection(LinkedList::new));
    List<LimitOrder> bids =
        kc.getBids().stream()
            .map(PriceAndSize::new)
            .sorted(Ordering.natural().onResultOf((PriceAndSize s) -> s != null ? s.price : null).reversed())
            .map(s -> adaptLimitOrder(instrument, cpmd, BID, s, timestamp))
            .collect(toCollection(LinkedList::new));
    return new OrderBook(timestamp, asks, bids);
  }

  public static LimitOrder adaptLimitOrder(Instrument instrument,
                                           CurrencyPairMetaData cpmd,
                                           OrderType orderType,
                                           PriceAndSize priceAndSize,
                                           Date timestamp) {
    return new LimitOrder.Builder(orderType, instrument)
        .timestamp(timestamp)
        .limitPrice(priceAndSize.price.setScale(cpmd.getPriceScale(), RoundingMode.HALF_EVEN))
        .originalAmount(multiply(priceAndSize.size, cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
        .orderStatus(NEW)
        .build();
  }

  public static Trades adaptTrades(Instrument instrument,
                                   CurrencyPairMetaData cpmd,
                                   List<TradeHistoryResponse> kucoinTrades) {
    return new Trades(
        kucoinTrades.stream().map(o -> adaptTrade(instrument, cpmd, o)).collect(Collectors.toList()),
        TradeSortType.SortByTimestamp);
  }

  public static Balance adaptBalance(AccountBalancesResponse a) {
    return new Balance(Currency.getInstance(a.getCurrency()), a.getAccountEquity(), a.getAvailableBalance());
  }

  private static Trade adaptTrade(Instrument instrument,
                                  CurrencyPairMetaData cpmd,
                                  TradeHistoryResponse trade) {
    return new Trade.Builder()
        .instrument(instrument)
        .originalAmount(multiply(trade.getSize(), cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
        .price(trade.getPrice().setScale(cpmd.getPriceScale(), RoundingMode.HALF_EVEN))
        .timestamp(new Date(Long.parseLong(trade.getSequence())))
        .type(adaptSide(trade.getSide()))
        .build();
  }

  public static OrderType adaptSide(String side) {
    return "sell".equals(side) ? ASK : BID;
  }

  private static String adaptSide(OrderType type) {
    return type.equals(ASK) ? "sell" : "buy";
  }

  public static String adaptIntention(OrderType type, StopOrder.Intention intention) {
    if (intention.equals(TAKE_PROFIT)) {
      return type.equals(BID) ? "up" : "down";
    } else {
      return type.equals(BID) ? "down" : "up";
    }
  }

  public static StopOrder.Intention adaptIntention(String type, String direction) {
    if (type.equals("buy")) {
      return direction.equals("up") ? TAKE_PROFIT : STOP_LOSS;
    } else {
      return direction.equals("up") ? STOP_LOSS : TAKE_PROFIT;
    }
  }

  public static Order adaptOrder(Exchange exchange, OrderResponse order) {

    OrderType orderType = adaptSide(order.getSide());
    CurrencyPair currencyPair = adaptCurrencyPair(order.getSymbol());

    OrderStatus status;
    if (order.isCancelExist()) {
      if (order.getDealSize().signum() == 0) {
        status = CANCELED;
      } else {
        status = PARTIALLY_CANCELED;
      }
    } else if (order.isActive()) {
      if (order.getDealSize().signum() == 0) {
        status = NEW;
      } else {
        status = PARTIALLY_FILLED;
      }
    } else if (order.getDealSize().equals(order.getSize())) {
      status = FILLED;
    } else if (order.getDealSize().equals(BigDecimal.ZERO)) {
      status = CANCELED;
    } else {
      status = PARTIALLY_CANCELED;
    }

    Order.Builder builder;
    if (StringUtils.isNotEmpty(order.getStop())) {
      BigDecimal limitPrice = order.getPrice();
      if (limitPrice != null && limitPrice.compareTo(BigDecimal.ZERO) == 0) {
        limitPrice = null;
      }
      builder =
          new StopOrder.Builder(orderType, currencyPair)
              .intention(adaptIntention(order.getType(), order.getStopPriceType()))
              .stopPrice(order.getStopPrice())
              .limitPrice(limitPrice)
              .stopTriggered(order.isStopTriggered());
    } else {
      builder = new LimitOrder.Builder(orderType, currencyPair)
              .limitPrice(order.getPrice());
    }
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair);
    builder =
        builder
            .averagePrice(
                order.getDealSize().compareTo(BigDecimal.ZERO) == 0
                    ? MoreObjects.firstNonNull(order.getPrice(), order.getStopPrice())
                    : order.getDealValue().divide(order.getDealSize(), RoundingMode.HALF_UP))
            .cumulativeAmount(order.getDealSize().multiply(cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
//          .fee(order.getFee())
            .id(order.getId())
            .orderStatus(status)
            .originalAmount(order.getSize().multiply(cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
            .remainingAmount(order.getSize().subtract(order.getFilledSize()).multiply(cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
            .timestamp(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .endAt(order.getEndAt())
            .userReference(order.getRemark());

    if (StringUtils.isNotEmpty(order.getTimeInForce())) {
      builder.flag(TimeInForce.getTimeInForce(order.getTimeInForce()));
    }

    return builder instanceof StopOrder.Builder
        ? ((StopOrder.Builder) builder).build()
        : ((LimitOrder.Builder) builder).build();
  }

  public static UserTrade adaptUserTrade(Exchange exchange, TradeResponse trade) {
    CurrencyPair currencyPair = adaptCurrencyPair(trade.getSymbol());
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair);
    return new UserTrade.Builder()
        .currencyPair(currencyPair)
        .feeAmount(trade.getFee())
        .feeCurrency(Currency.getInstance(trade.getFeeCurrency()))
        .id(trade.getTradeId())
        .orderId(trade.getOrderId())
        .originalAmount(trade.getSize().multiply(cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
        .price(trade.getPrice().setScale(cpmd.getPriceScale(), RoundingMode.HALF_EVEN))
        .timestamp(trade.getTradeCreatedAt())
        .type(adaptSide(trade.getSide()))
        .build();
  }

  public static UserTrade adaptHistOrder(Exchange exchange, HistOrdersResponse histOrder) {
    CurrencyPair currencyPair = adaptCurrencyPair(histOrder.getSymbol());
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair);
    return new UserTrade.Builder()
        .currencyPair(currencyPair)
        .feeAmount(histOrder.getFee().setScale(cpmd.getPriceScale(), RoundingMode.HALF_EVEN))
        .feeCurrency(currencyPair.base)
        .id(histOrder.getId())
        .originalAmount(histOrder.getAmount().multiply(cpmd.getAmountStepSize()).setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN))
        .price(histOrder.getPrice().setScale(cpmd.getPriceScale(), RoundingMode.HALF_EVEN))
        .timestamp(histOrder.getTradeCreatedAt())
        .type(adaptSide(histOrder.getSide()))
        .build();
  }

  public static OrderCreateApiRequest adaptLimitOrder(Exchange exchange, LimitOrder limitOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(exchange, limitOrder))
        .type("limit")
        .price(limitOrder.getLimitPrice())
        .postOnly(limitOrder.hasFlag(POST_ONLY))
        .hidden(limitOrder.hasFlag(HIDDEN))
        .iceberg(limitOrder.hasFlag(ICEBERG))
        .build();
  }

  public static OrderCreateApiRequest adaptStopOrder(Exchange exchange, StopOrder stopOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(exchange, stopOrder))
        .type(stopOrder.getLimitPrice() == null ? "market" : "limit")
        .price(stopOrder.getLimitPrice())
        .stop(adaptIntention(stopOrder.getType(), stopOrder.getIntention()))
        .stopPriceType("TP")
        .stopPrice(stopOrder.getStopPrice())
        .build();
  }

  public static OrderCreateApiRequest adaptMarketOrder(Exchange exchange, MarketOrder marketOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(exchange, marketOrder))
        .type("market")
        .build();
  }

  /**
   * Returns {@code Object} instead of the Lombok builder in order to avoid a Lombok limitation with
   * Javadoc.
   */
  private static Object adaptOrder(Exchange exchange, Order order) {
    OrderCreateApiRequest.OrderCreateApiRequestBuilder request = OrderCreateApiRequest.builder();
    boolean hasClientId = false;
    for (IOrderFlags flag : order.getOrderFlags()) {
      if (flag instanceof KucoinOrderFlags) {
        if (flag.equals(POST_ONLY)) {
          request.postOnly(true);
        } else if (flag.equals(HIDDEN)) {
          request.hidden(true);
        } else if (flag.equals(ICEBERG)) {
          request.iceberg(true);
        } else {
          request.clientOid(((KucoinOrderFlags) flag).getClientId());
          hasClientId = true;
        }
      } else if (flag instanceof TimeInForce) {
        request.timeInForce(((TimeInForce) flag).name());
      }
    }
    if (order.getId() != null) {
      request.clientOid(order.getId());
      hasClientId = true;
    }
    if (!hasClientId) {
      request.clientOid(UUID.randomUUID().toString());
    }
    CurrencyPairMetaData cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(order.getInstrument());
    return request
        .symbol(adaptCurrencyPair((CurrencyPair) order.getInstrument()))
        .size(order.getOriginalAmount().divide(cpmd.getAmountStepSize(), RoundingMode.HALF_EVEN))
        .side(adaptSide(order.getType()))
        .leverage(new BigDecimal(order.getLeverage()))
        .remark(order.getUserReference());
  }

  public static final class PriceAndSize {

    final BigDecimal price;
    final BigDecimal size;

    PriceAndSize(List<String> data) {
      this.price = new BigDecimal(data.get(0));
      this.size = new BigDecimal(data.get(1));
    }
  }

  public static FundingRecord adaptFundingRecord(WithdrawalResponse wr) {
    FundingRecord.Builder b = new FundingRecord.Builder();
    return b.setAddress(wr.getAddress())
        .setAmount(wr.getAmount())
        .setCurrency(Currency.getInstance(wr.getCurrency()))
        .setFee(wr.getFee())
        .setType(Type.WITHDRAWAL)
        .setStatus(convertStatus(wr.getStatus()))
        .setInternalId(wr.getId())
        .setBlockchainTransactionHash(wr.getWalletTxId())
        .setDescription(wr.getMemo())
        .setDate(wr.getCreatedAt())
        .build();
  }

  private static Status convertStatus(String status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case "WALLET_PROCESSING":
      case "PROCESSING":
        return Status.PROCESSING;
      case "SUCCESS":
        return Status.COMPLETE;
      case "FAILURE":
        return Status.FAILED;
      default:
        throw new ExchangeException("Not supported status: " + status);
    }
  }

  public static FundingRecord adaptFundingRecord(DepositResponse dr) {
    FundingRecord.Builder b = new FundingRecord.Builder();
    return b.setAddress(dr.getAddress())
        .setAmount(dr.getAmount())
        .setCurrency(Currency.getInstance(dr.getCurrency()))
        .setFee(dr.getFee())
        .setType(Type.DEPOSIT)
        .setStatus(convertStatus(dr.getStatus()))
        .setBlockchainTransactionHash(dr.getWalletTxId())
        .setDescription(dr.getMemo())
        .setDate(dr.getCreatedAt())
        .build();
  }
}
