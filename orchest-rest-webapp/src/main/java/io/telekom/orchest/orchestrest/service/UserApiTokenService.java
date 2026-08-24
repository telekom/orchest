package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.adapter.mongo.repository.MongoUserApiTokenRepository;
import io.telekom.orchest.orchestrest.configurations.security.ApiTokenProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for creating, validating, listing, and revoking user API tokens. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApiTokenService {

  private static final String TOKEN_PREFIX = "orchest_";
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final MongoUserApiTokenRepository tokenRepository;
  private final ApiTokenProperties properties;

  public record CreateTokenResult(String rawToken, UserApiToken token) {}

  public CreateTokenResult createToken(
      String userId, List<String> roles, String name, Integer ttlInDays) {
    String rawToken = generateRawToken();
    String tokenHash = hashToken(rawToken);
    String tokenPrefix = rawToken.substring(0, Math.min(rawToken.length(), 12)) + "...";

    OffsetDateTime expiresAt = null;
    if (ttlInDays != null && ttlInDays > 0) {
      int cappedDays = Math.min(ttlInDays, properties.getMaxTtlDays());
      expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(cappedDays);
    }

    UserApiToken token =
        UserApiToken.builder()
            .tokenId(UUID.randomUUID().toString())
            .name(name)
            .tokenHash(tokenHash)
            .tokenPrefix(tokenPrefix)
            .userId(userId)
            .roles(roles)
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
            .expiresAt(expiresAt)
            .revoked(false)
            .build();

    UserApiToken saved = tokenRepository.save(token);
    log.info("API token created for user={} name={} tokenId={}", userId, name, saved.getTokenId());
    return new CreateTokenResult(rawToken, saved);
  }

  public List<UserApiToken> listTokens(String userId) {
    return tokenRepository.findAllByUserIdAndRevokedFalse(userId);
  }

  public Optional<UserApiToken> getToken(String userId, String tokenId) {
    return tokenRepository.findByTokenIdAndUserId(tokenId, userId);
  }

  public void revokeToken(String userId, String tokenId) {
    tokenRepository
        .findByTokenIdAndUserId(tokenId, userId)
        .ifPresent(
            token -> {
              if (!token.isRevoked()) {
                token.setRevoked(true);
                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                tokenRepository.save(token);
                log.info("API token revoked: tokenId={} user={}", tokenId, userId);
              }
            });
  }

  public void revokeAllTokens(String userId) {
    List<UserApiToken> tokens = tokenRepository.findAllByUserIdAndRevokedFalse(userId);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    tokens.forEach(
        token -> {
          token.setRevoked(true);
          token.setRevokedAt(now);
        });
    tokenRepository.saveAll(tokens);
    log.info("All API tokens revoked for user={} count={}", userId, tokens.size());
  }

  public Optional<UserApiToken> validateToken(String rawToken) {
    String hash = hashToken(rawToken);
    return tokenRepository
        .findByTokenHash(hash)
        .filter(token -> !token.isRevoked())
        .filter(
            token ->
                token.getExpiresAt() == null
                    || token.getExpiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)));
  }

  public void updateLastUsed(String tokenId) {
    tokenRepository
        .findById(tokenId)
        .ifPresent(
            token -> {
              token.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
              tokenRepository.save(token);
            });
  }

  private String generateRawToken() {
    byte[] bytes = new byte[properties.getTokenByteLength()];
    SECURE_RANDOM.nextBytes(bytes);
    return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
