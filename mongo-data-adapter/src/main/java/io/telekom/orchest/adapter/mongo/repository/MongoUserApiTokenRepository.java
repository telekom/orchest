package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link UserApiToken} documents. Manages user API tokens
 * including lookup by hash and revocation status.
 */
@Repository
public interface MongoUserApiTokenRepository extends MongoRepository<UserApiToken, String> {

  /**
   * Finds a token by its hash value for authentication lookup.
   *
   * @param tokenHash the SHA-256 hash of the token
   * @return the matching token, or empty if not found
   */
  Optional<UserApiToken> findByTokenHash(String tokenHash);

  /**
   * Finds all active (non-revoked) tokens for a user.
   *
   * @param userId the user identifier
   * @return list of active tokens belonging to the user
   */
  List<UserApiToken> findAllByUserIdAndRevokedFalse(String userId);

  /**
   * Finds a specific token by token ID and user ID.
   *
   * @param tokenId the token identifier
   * @param userId the user identifier
   * @return the matching token, or empty if not found
   */
  Optional<UserApiToken> findByTokenIdAndUserId(String tokenId, String userId);

  /**
   * Finds all tokens (active and revoked) for a user.
   *
   * @param userId the user identifier
   * @return list of all tokens belonging to the user
   */
  List<UserApiToken> findAllByUserId(String userId);
}
