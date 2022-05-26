package org.knowm.xchange.kucoinfutures;

import static org.knowm.xchange.kucoinfutures.KucoinExceptionClassifier.classifyingExceptions;
import static org.knowm.xchange.kucoinfutures.KucoinResilience.PRIVATE_REST_ENDPOINT_RATE_LIMITER;

import java.io.IOException;
import java.util.List;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.kucoinfutures.dto.request.ApplyWithdrawApiRequest;
import org.knowm.xchange.kucoinfutures.dto.request.CreateAccountRequest;
import org.knowm.xchange.kucoinfutures.dto.request.InnerTransferRequest;
import org.knowm.xchange.kucoinfutures.dto.response.AccountBalancesResponse;
import org.knowm.xchange.kucoinfutures.dto.response.AccountLedgersResponse;
import org.knowm.xchange.kucoinfutures.dto.response.ApplyWithdrawResponse;
import org.knowm.xchange.kucoinfutures.dto.response.DepositAddressResponse;
import org.knowm.xchange.kucoinfutures.dto.response.DepositResponse;
import org.knowm.xchange.kucoinfutures.dto.response.InternalTransferResponse;
import org.knowm.xchange.kucoinfutures.dto.response.Pagination;
import org.knowm.xchange.kucoinfutures.dto.response.WithdrawalResponse;

public class KucoinAccountServiceRaw extends KucoinBaseService {

  protected KucoinAccountServiceRaw(
          KucoinExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public AccountBalancesResponse getKucoinAccount() throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        accountApi.getAccount(
                            apiKey, digest, nonceFactory, passphrase, null))
                .withRetry(retry("accountList"))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public Void createKucoinAccount(String currency, String type) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        accountApi.createAccount(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            CreateAccountRequest.builder().currency(currency).type(type).build()))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public ApplyWithdrawResponse applyWithdraw(ApplyWithdrawApiRequest req) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        withdrawalAPI.applyWithdraw(apiKey, digest, nonceFactory, passphrase, req))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public InternalTransferResponse innerTransfer(InnerTransferRequest req) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () -> accountApi.innerTransfer(apiKey, digest, nonceFactory, passphrase, req))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  @Deprecated
  public Pagination<AccountLedgersResponse> getAccountLedgers(
      String accountId, Long startAt, Long endAt, Integer pageSize, Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        accountApi.getAccountLedgers(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            accountId,
                            startAt,
                            endAt,
                            pageSize,
                            currentPage))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public Pagination<AccountLedgersResponse> getAccountLedgersWithParams(
      String currency,
      String direction,
      String bizType,
      Long startAt,
      Long endAt,
      Integer pageSize,
      Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        accountApi.getAccountLedgersWithParams(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency,
                            direction,
                            bizType,
                            startAt,
                            endAt,
                            pageSize,
                            currentPage))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public Pagination<WithdrawalResponse> getWithdrawalsList(
      String currency,
      String status,
      Long startAt,
      Long endAt,
      Integer pageSize,
      Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        withdrawalAPI.getWithdrawalsList(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency,
                            status,
                            startAt,
                            endAt,
                            pageSize,
                            currentPage))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public Pagination<DepositResponse> getDepositList(
      String currency,
      String status,
      Long startAt,
      Long endAt,
      Integer pageSize,
      Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        depositAPI.getDepositList(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency,
                            status,
                            startAt,
                            endAt,
                            pageSize,
                            currentPage))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public DepositAddressResponse createDepositAddress(String currency, String chain)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        depositAPI.createDepositAddress(
                            apiKey, digest, nonceFactory, passphrase, currency, chain))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public DepositAddressResponse getDepositAddress(String currency, String chain)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        depositAPI.getDepositAddress(
                            apiKey, digest, nonceFactory, passphrase, currency, chain))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }

  public List<DepositAddressResponse> getDepositAddresses(String currency) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            decorateApiCall(
                    () ->
                        depositAPI.getDepositAddresses(
                            apiKey, digest, nonceFactory, passphrase, currency))
                .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
                .call());
  }
}
