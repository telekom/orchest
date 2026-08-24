package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.adapter.mongo.repository.MongoUserApiTokenRepository;
import io.telekom.orchest.orchestrest.configurations.security.ApiTokenProperties;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link io.telekom.orchest.orchestrest.service.UserApiTokenService}. */
@ExtendWith(MockitoExtension.class)
class UserApiTokenServiceTest {

  @Mock private MongoUserApiTokenRepository tokenRepository;

  private ApiTokenProperties properties;
  private UserApiTokenService service;

  @BeforeEach
  void setUp() {
    properties = new ApiTokenProperties();
    properties.setEnabled(true);
    properties.setMaxTtlDays(365);
    properties.setTokenByteLength(32);
    service = new UserApiTokenService(tokenRepository, properties);
  }

  @Test
  @DisplayName("createToken generates token with orchest_ prefix and persists hash")
  void createToken_generatesCorrectFormat() {
    when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UserApiTokenService.CreateTokenResult result =
        service.createToken("user@example.com", List.of("ORCHEST_ADMIN"), "My Token", 90);

    assertThat(result.rawToken()).startsWith("orchest_");
    assertThat(result.rawToken().length()).isGreaterThan(20);

    ArgumentCaptor<UserApiToken> captor = ArgumentCaptor.forClass(UserApiToken.class);
    verify(tokenRepository).save(captor.capture());

    UserApiToken saved = captor.getValue();
    assertThat(saved.getTokenHash()).isNotEmpty();
    assertThat(saved.getTokenHash()).isNotEqualTo(result.rawToken());
    assertThat(saved.getUserId()).isEqualTo("user@example.com");
    assertThat(saved.getRoles()).containsExactly("ORCHEST_ADMIN");
    assertThat(saved.getName()).isEqualTo("My Token");
    assertThat(saved.getExpiresAt()).isNotNull();
    assertThat(saved.isRevoked()).isFalse();
    assertThat(saved.getTokenPrefix()).startsWith("orchest_");
  }

  @Test
  @DisplayName("createToken with null TTL creates non-expiring token")
  void createToken_nullTtl_neverExpires() {
    when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.createToken("user@example.com", List.of("ORCHEST_READ_ONLY"), "No Expiry", null);

    ArgumentCaptor<UserApiToken> captor = ArgumentCaptor.forClass(UserApiToken.class);
    verify(tokenRepository).save(captor.capture());
    assertThat(captor.getValue().getExpiresAt()).isNull();
  }

  @Test
  @DisplayName("createToken caps TTL at max configured days")
  void createToken_cappedTtl() {
    when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.createToken("user@example.com", List.of("ORCHEST_ADMIN"), "Long Token", 9999);

    ArgumentCaptor<UserApiToken> captor = ArgumentCaptor.forClass(UserApiToken.class);
    verify(tokenRepository).save(captor.capture());

    OffsetDateTime maxExpected = OffsetDateTime.now(ZoneOffset.UTC).plusDays(365).plusMinutes(1);
    assertThat(captor.getValue().getExpiresAt()).isBefore(maxExpected);
  }

  @Test
  @DisplayName("validateToken returns token when valid")
  void validateToken_valid() {
    when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    UserApiTokenService.CreateTokenResult created =
        service.createToken("user@example.com", List.of("ORCHEST_ADMIN"), "Test", 30);

    UserApiToken stored =
        UserApiToken.builder()
            .tokenId("tid")
            .userId("user@example.com")
            .roles(List.of("ORCHEST_ADMIN"))
            .revoked(false)
            .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(30))
            .build();
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));

    Optional<UserApiToken> result = service.validateToken(created.rawToken());
    assertThat(result).isPresent();
    assertThat(result.get().getUserId()).isEqualTo("user@example.com");
  }

  @Test
  @DisplayName("validateToken returns empty for expired token")
  void validateToken_expired() {
    UserApiToken expired =
        UserApiToken.builder()
            .tokenId("tid")
            .revoked(false)
            .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1))
            .build();
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

    Optional<UserApiToken> result = service.validateToken("orchest_sometoken");
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("validateToken returns empty for revoked token")
  void validateToken_revoked() {
    UserApiToken revoked =
        UserApiToken.builder()
            .tokenId("tid")
            .revoked(true)
            .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(30))
            .build();
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revoked));

    Optional<UserApiToken> result = service.validateToken("orchest_sometoken");
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("validateToken returns empty for unknown token")
  void validateToken_notFound() {
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    Optional<UserApiToken> result = service.validateToken("orchest_unknown");
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("revokeToken sets revoked flag")
  void revokeToken() {
    UserApiToken token =
        UserApiToken.builder().tokenId("tid").userId("user@example.com").revoked(false).build();
    when(tokenRepository.findByTokenIdAndUserId("tid", "user@example.com"))
        .thenReturn(Optional.of(token));
    when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.revokeToken("user@example.com", "tid");

    ArgumentCaptor<UserApiToken> captor = ArgumentCaptor.forClass(UserApiToken.class);
    verify(tokenRepository).save(captor.capture());
    assertThat(captor.getValue().isRevoked()).isTrue();
    assertThat(captor.getValue().getRevokedAt()).isNotNull();
  }

  @Test
  @DisplayName("revokeToken on already revoked is no-op")
  void revokeToken_alreadyRevoked() {
    UserApiToken token =
        UserApiToken.builder()
            .tokenId("tid")
            .userId("user@example.com")
            .revoked(true)
            .revokedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1))
            .build();
    when(tokenRepository.findByTokenIdAndUserId("tid", "user@example.com"))
        .thenReturn(Optional.of(token));

    service.revokeToken("user@example.com", "tid");

    verify(tokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("listTokens returns only user's active tokens")
  void listTokens() {
    List<UserApiToken> tokens =
        List.of(
            UserApiToken.builder().tokenId("t1").userId("user@example.com").build(),
            UserApiToken.builder().tokenId("t2").userId("user@example.com").build());
    when(tokenRepository.findAllByUserIdAndRevokedFalse("user@example.com")).thenReturn(tokens);

    List<UserApiToken> result = service.listTokens("user@example.com");
    assertThat(result).hasSize(2);
  }
}
