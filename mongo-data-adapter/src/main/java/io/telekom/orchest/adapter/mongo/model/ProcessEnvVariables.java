package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an environment variable bound to a process definition. Supports
 * both plain-text and secret variable types.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ProcessEnvVariables {

  /** Unique database identifier. */
  @Id private String id;

  /** The process definition this variable is associated with. */
  private String processDefinitionId;

  /** The name of the environment variable. */
  private String name;

  /** The value of the environment variable (may be encrypted for secrets). */
  private String value;

  /** Whether this variable is a secret or plain text. */
  private VariableType type;

  /** Timestamp when this variable was created. */
  @CreatedDate private OffsetDateTime createdAt;

  /** Timestamp when this variable was last modified. */
  @LastModifiedDate private OffsetDateTime updatedAt;

  /** Enum representing the storage type of the variable. */
  public enum VariableType {
    SECRET,
    PLAIN_TEXT
  }
}
