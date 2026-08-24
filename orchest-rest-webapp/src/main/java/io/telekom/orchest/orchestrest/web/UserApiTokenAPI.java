package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.service.UserApiTokenService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IUserApiTokenAPI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for user API token management. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserApiTokenAPI implements IUserApiTokenAPI {

  private final UserApiTokenService tokenService;

  @Override
  public Mono<ResponseDTO<CreateApiTokenResponse>> createToken(CreateApiTokenRequest request) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx = ctx.get(LoggedInUserContext.CONTEXT_KEY);
          return Mono.fromCallable(
                  () -> {
                    UserApiTokenService.CreateTokenResult result =
                        tokenService.createToken(
                            userCtx.getUserId(),
                            userCtx.getRoles(),
                            request.name(),
                            request.ttlInDays());

                    UserApiToken token = result.token();
                    CreateApiTokenResponse response =
                        new CreateApiTokenResponse(
                            token.getTokenId(),
                            token.getName(),
                            result.rawToken(),
                            token.getRoles(),
                            token.getCreatedAt() != null ? token.getCreatedAt().toString() : null,
                            token.getExpiresAt() != null ? token.getExpiresAt().toString() : null);
                    return RestUtils.buildResponse(response, 201, "Token created successfully");
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<List<ApiTokenResponse>>> listTokens() {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx = ctx.get(LoggedInUserContext.CONTEXT_KEY);
          return Mono.fromCallable(
                  () -> {
                    List<ApiTokenResponse> tokens =
                        tokenService.listTokens(userCtx.getUserId()).stream()
                            .map(this::toResponse)
                            .toList();
                    return RestUtils.buildResponse(tokens, 200, "success");
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<ApiTokenResponse>> getToken(String tokenId) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx = ctx.get(LoggedInUserContext.CONTEXT_KEY);
          return Mono.fromCallable(
                  () ->
                      tokenService
                          .getToken(userCtx.getUserId(), tokenId)
                          .map(token -> RestUtils.buildResponse(toResponse(token), 200, "success"))
                          .orElse(RestUtils.buildMeta(404, "Token not found")))
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<String>> revokeToken(String tokenId) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx = ctx.get(LoggedInUserContext.CONTEXT_KEY);
          return Mono.fromCallable(
                  () -> {
                    tokenService.revokeToken(userCtx.getUserId(), tokenId);
                    return RestUtils.buildResponse("Token revoked", 200, "success");
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<String>> revokeAllTokens() {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx = ctx.get(LoggedInUserContext.CONTEXT_KEY);
          return Mono.fromCallable(
                  () -> {
                    tokenService.revokeAllTokens(userCtx.getUserId());
                    return RestUtils.buildResponse("All tokens revoked", 200, "success");
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  private ApiTokenResponse toResponse(UserApiToken token) {
    return new ApiTokenResponse(
        token.getTokenId(),
        token.getName(),
        token.getTokenPrefix(),
        token.getRoles(),
        token.getCreatedAt() != null ? token.getCreatedAt().toString() : null,
        token.getExpiresAt() != null ? token.getExpiresAt().toString() : null,
        token.getLastUsedAt() != null ? token.getLastUsedAt().toString() : null,
        token.isRevoked());
  }
}
