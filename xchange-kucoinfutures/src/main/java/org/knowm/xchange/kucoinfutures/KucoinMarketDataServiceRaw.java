package org.knowm.xchange.kucoinfutures;

import static org.knowm.xchange.kucoinfutures.KucoinExceptionClassifier.classifyingExceptions;
import static org.knowm.xchange.kucoinfutures.KucoinResilience.PRIVATE_REST_ENDPOINT_RATE_LIMITER;
import static org.knowm.xchange.kucoinfutures.KucoinResilience.PUBLIC_REST_ENDPOINT_RATE_LIMITER;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.KlineIntervalType;
import org.knowm.xchange.kucoinfutures.dto.response.AllTickersResponse;
import org.knowm.xchange.kucoinfutures.dto.response.ContractResponse;
import org.knowm.xchange.kucoinfutures.dto.response.KucoinKline;
import org.knowm.xchange.kucoinfutures.dto.response.OrderBookResponse;
import org.knowm.xchange.kucoinfutures.dto.response.SymbolTickResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TickerResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TradeFeeResponse;
import org.knowm.xchange.kucoinfutures.dto.response.TradeHistoryResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WithdrawalQuotaResponse;

public class KucoinMarketDataServiceRaw extends KucoinBaseService {

  protected KucoinMarketDataServiceRaw(
          KucoinExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public TickerResponse getKucoinTicker(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(() -> symbolApi.getTicker(org.knowm.xchange.kucoinfutures.KucoinAdapters.adaptCurrencyPair(pair)))
                .withRetry(retry("ticker"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public AllTickersResponse getKucoinTickers() throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(symbolApi::getTickers)
                .withRetry(retry("tickers"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public SymbolTickResponse getKucoin24hrStats(Instrument instrument) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(() -> symbolApi.getMarketStats(org.knowm.xchange.kucoinfutures.KucoinAdapters.adaptInstrument(instrument)))
                .withRetry(retry("24hrStats"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public Map<String, BigDecimal> getKucoinPrices() throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(symbolApi::getPrices)
                .withRetry(retry("prices"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public List<TradeFeeResponse> getKucoinTradeFee(String symbols) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        tradingFeeAPI.getTradeFee(
                            apiKey, digest, nonceFactory, passphrase, symbols))
                .withRetry(retry("tradeFee"))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public List<ContractResponse> getKucoinContracts() throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(symbolApi::getOpenContracts)
                .withRetry(retry("contracts"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public WithdrawalQuotaResponse getKucoinWithdrawalQuota(String currency) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () -> symbolApi.getWitdrawalQuota(apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency))
                .withRetry(retry("withdrawalQuota"))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public OrderBookResponse getKucoinOrderBookPartial(Instrument instrument) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        orderBookApi.getPartOrderBookAggregated(
                            org.knowm.xchange.kucoinfutures.KucoinAdapters.adaptInstrument(instrument)))
                .withRetry(retry("partialOrderBook"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public OrderBookResponse getKucoinOrderBookPartialShallow(Instrument instrument) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        orderBookApi.getPartOrderBookShallowAggregated(KucoinAdapters.adaptInstrument(instrument)))
                .withRetry(retry("partialShallowOrderBook"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public OrderBookResponse getKucoinOrderBookFull(Instrument instrument) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        orderBookApi.getFullOrderBookAggregated(
                            org.knowm.xchange.kucoinfutures.KucoinAdapters.adaptInstrument(instrument),
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase))
                .withRetry(retry("fullOrderBook"))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public List<TradeHistoryResponse> getKucoinTrades(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () -> historyApi.getTradeHistories(org.knowm.xchange.kucoinfutures.KucoinAdapters.adaptCurrencyPair(pair)))
                .withRetry(retry("tradeHistories"))
                .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public List<KucoinKline> getKucoinKlines(
      CurrencyPair pair, Long startTime, Long endTime, KlineIntervalType type) throws IOException {
    List<Object[]> raw =
        classifyingExceptions(
            () ->
                decorateApiCall(
                        () ->
                            historyApi.getKlines(
                                KucoinAdapters.adaptCurrencyPair(pair),
                                startTime,
                                endTime,
                                type.code()))
                    .withRetry(retry("klines"))
                    .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
                    .call());

    return raw.stream().map(obj -> new KucoinKline(pair, type, obj)).collect(Collectors.toList());
  }
}
