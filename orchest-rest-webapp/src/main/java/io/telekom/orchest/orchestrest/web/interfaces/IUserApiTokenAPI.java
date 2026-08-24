package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/apiTokens")
@Tag(
    name = "API Tokens",
    description = "Create, list, and revoke personal API tokens for programmatic access")
/** REST API interface for user API token management. */
public interface IUserApiTokenAPI {

  record CreateApiTokenRequest(@NotBlank @Size(max = 100) String name, Integer ttlInDays) {}

  record ApiTokenResponse(
      String tokenId,
      String name,
      String tokenPrefix,
      List<String> roles,
      String createdAt,
      String expiresAt,
      String lastUsedAt,
      boolean revoked) {}

  record CreateApiTokenResponse(
      String tokenId,
      String name,
      String token,
      List<String> roles,
      String createdAt,
      String expiresAt) {}

  @PostMapping
  @Operation(
      summary = "Create a new API token",
      description =
          "Generates a new token with an optional TTL. The full token value is returned only once — store it securely.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Token created — includes the full token value (shown only once)")
      })
  Mono<ResponseDTO<CreateApiTokenResponse>> createToken(
      @RequestBody @Valid CreateApiTokenRequest request);

  @GetMapping
  @Operation(
      summary = "List all tokens for the current user",
      description =
          "Returns metadata for all tokens (active and revoked). The full token value is never returned after creation.")
  Mono<ResponseDTO<List<ApiTokenResponse>>> listTokens();

  @GetMapping("/{tokenId}")
  @Operation(
      summary = "Get token details",
      responses = {
        @ApiResponse(responseCode = "200", description = "Token found"),
        @ApiResponse(responseCode = "404", description = "Token not found")
      })
  Mono<ResponseDTO<ApiTokenResponse>> getToken(
      @Parameter(description = "Token ID") @PathVariable String tokenId);

  @DeleteMapping("/{tokenId}")
  @Operation(
      summary = "Revoke a token",
      description = "Permanently revokes a token — it can no longer be used for authentication")
  Mono<ResponseDTO<String>> revokeToken(
      @Parameter(description = "Token ID to revoke") @PathVariable String tokenId);

  @DeleteMapping
  @Operation(
      summary = "Revoke all tokens",
      description = "Permanently revokes all tokens for the current user")
  Mono<ResponseDTO<String>> revokeAllTokens();
}
