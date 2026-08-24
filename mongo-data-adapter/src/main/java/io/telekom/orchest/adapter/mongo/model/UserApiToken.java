package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a user-issued API token. Stores the hashed token, its owner, roles,
 * and revocation state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class UserApiToken {

  /** Unique database identifier. */
  @Id private String id;

  /** Public token identifier (non-secret). */
  private String tokenId;

  /** Human-readable name given to the token by the user. */
  private String name;

  /** Cryptographic hash of the token value. */
  private String tokenHash;

  /** Short prefix of the token for display/identification. */
  private String tokenPrefix;

  /** The ID of the user who owns this token. */
  private String userId;

  /** Roles granted to this token. */
  private List<String> roles;

  /** Timestamp when the token was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when the token expires. */
  private OffsetDateTime expiresAt;

  /** Timestamp when the token was last used for authentication. */
  private OffsetDateTime lastUsedAt;

  /** Whether the token has been revoked. */
  private boolean revoked;

  /** Timestamp when the token was revoked (if applicable). */
  private OffsetDateTime revokedAt;
}
