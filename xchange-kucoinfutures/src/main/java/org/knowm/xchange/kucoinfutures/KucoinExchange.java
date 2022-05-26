package org.knowm.xchange.kucoinfutures;

import static org.knowm.xchange.kucoinfutures.KucoinExceptionClassifier.classifyingExceptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingTradeService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kucoinfutures.dto.response.ContractResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WebsocketResponse;
import org.knowm.xchange.kucoinfutures.dto.response.WithdrawalQuotaResponse;

public class KucoinExchange extends BaseExchange implements StreamingExchange {

  /**
   * Use with {@link ExchangeSpecification#getExchangeSpecificParametersItem(String)} to specify
   * that connection should be made to the Kucoin sandbox instead of the live API.
   */
  public static final String PARAM_SANDBOX = "Use_Sandbox";

  static final String SANDBOX_URI = "https://api-sandbox-futures.kucoin.com";
  static final String PROD_URI = "https://api-futures.kucoin.com";

  private static ResilienceRegistries RESILIENCE_REGISTRIES;

  private KucoinStreamingService streamingService;
  private KucoinStreamingMarketDataService streamingMarketDataService;
  private KucoinStreamingTradeService streamingTradeService;

  private void concludeHostParams(ExchangeSpecification exchangeSpecification) {
    if (exchangeSpecification.getExchangeSpecificParameters() != null) {
      if (Boolean.TRUE.equals(
          exchangeSpecification.getExchangeSpecificParametersItem(PARAM_SANDBOX))) {
        logger.debug("Connecting to sandbox");
        exchangeSpecification.setSslUri(KucoinExchange.SANDBOX_URI);
        try {
          URL url = new URL(KucoinExchange.SANDBOX_URI);
          exchangeSpecification.setHost(url.getHost());
        } catch (MalformedURLException exception) {
          logger.error("Kucoin sandbox host exception: {}", exception.getMessage());
        }
      } else {
        logger.debug("Connecting to live");
      }
    }
  }

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {
    super.applySpecification(exchangeSpecification);
    concludeHostParams(exchangeSpecification);
  }

  @Override
  protected void initServices() {
    concludeHostParams(exchangeSpecification);
    this.marketDataService = new KucoinMarketDataService(this, getResilienceRegistries());
    this.accountService = new KucoinAccountService(this, getResilienceRegistries());
    this.tradeService = new KucoinTradeService(this, getResilienceRegistries());

    try {
      WebsocketResponse response = getPrivateWebsocketConnectionDetails();
      WebsocketResponse.InstanceServer server = response.getInstanceServers().get(0);

      this.streamingService = new KucoinStreamingService(server.getEndpoint(), response.getToken(),
              server.getPingInterval(), server.getPingTimeout());
      this.streamingMarketDataService = new KucoinStreamingMarketDataService(this, streamingService);
      this.streamingTradeService = new KucoinStreamingTradeService(this, streamingService);
    } catch (IOException e) {

    }
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri(PROD_URI);
    try {
      URL url = new URL(KucoinExchange.PROD_URI);
      exchangeSpecification.setHost(url.getHost());
    } catch (MalformedURLException exception) {
      logger.error("Kucoin host exception: {}", exception.getMessage());
    }
    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("Kucoin");
    exchangeSpecification.setExchangeDescription("Kucoin is a bitcoin and altcoin exchange.");
    return exchangeSpecification;
  }

  @Override
  public ResilienceRegistries getResilienceRegistries() {
    if (RESILIENCE_REGISTRIES == null) {
      RESILIENCE_REGISTRIES = KucoinResilience.createRegistries();
    }
    return RESILIENCE_REGISTRIES;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {

    List<ContractResponse> contractsResponse = getMarketDataService().getKucoinContracts();

    HashMap<String, WithdrawalQuotaResponse> currencies = new HashMap<>();
    for (ContractResponse contract : contractsResponse) {
      CurrencyPair pair = new CurrencyPair(contract.getBaseCurrency(), contract.getQuoteCurrency());
      if (exchangeMetaData.getCurrencyPairs().containsKey(pair)) {
        if (!currencies.containsKey(contract.getBaseCurrency())) {
          WithdrawalQuotaResponse response = getMarketDataService().getKucoinWithdrawalQuota(contract.getBaseCurrency());
          if (response != null) {
            currencies.put(contract.getBaseCurrency(), response);
          }
        }
        if (!currencies.containsKey(contract.getQuoteCurrency())) {
          WithdrawalQuotaResponse response = getMarketDataService().getKucoinWithdrawalQuota(contract.getQuoteCurrency());
          if (response != null) {
            currencies.put(contract.getQuoteCurrency(), response);
          }
        }
      }
    }

    this.exchangeMetaData = KucoinAdapters.adaptMetadata(this.exchangeMetaData, contractsResponse, currencies.values());
  }

  public WebsocketResponse getPublicWebsocketConnectionDetails() throws IOException {
    return classifyingExceptions(getAccountService().websocketAPI::getPublicWebsocketDetails);
  }

  public WebsocketResponse getPrivateWebsocketConnectionDetails() throws IOException {
    getAccountService().checkAuthenticated();

    return classifyingExceptions(
            () ->
                    getAccountService()
                            .websocketAPI
                            .getPrivateWebsocketDetails(
                                    getAccountService().apiKey,
                                    getAccountService().digest,
                                    getAccountService().nonceFactory,
                                    getAccountService().passphrase));
  }

  @Override
  public Completable connect(ProductSubscription... args) {
    return streamingService.connect();
  }

  @Override
  public Completable disconnect() {
    return streamingService.disconnect();
  }

  @Override
  public boolean isAlive() {
    return streamingService.isSocketOpen();
  }

  @Override
  public Observable<Throwable> reconnectFailure() {
    return streamingService.subscribeReconnectFailure();
  }

  @Override
  public Observable<Object> connectionSuccess() {
    return streamingService.subscribeConnectionSuccess();
  }

  @Override
  public StreamingMarketDataService getStreamingMarketDataService() {
    return streamingMarketDataService;
  }

  @Override
  public StreamingTradeService getStreamingTradeService() {
    return streamingTradeService;
  }

  @Override
  public void useCompressedMessages(boolean compressedMessages) {
    streamingService.useCompressedMessages(compressedMessages);
  }
  @Override
  public KucoinMarketDataService getMarketDataService() {
    return (KucoinMarketDataService) super.getMarketDataService();
  }

  @Override
  public KucoinTradeService getTradeService() {
    return (KucoinTradeService) super.getTradeService();
  }

  @Override
  public KucoinAccountService getAccountService() {
    return (KucoinAccountService) super.getAccountService();
  }
}
