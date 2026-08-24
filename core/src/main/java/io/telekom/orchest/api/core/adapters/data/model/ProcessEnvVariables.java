package io.telekom.orchest.api.core.adapters.data.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model representing an environment variable bound to a process definition. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEnvVariables {

  /** Unique document identifier. */
  private String id;

  /** The process definition this variable is associated with. */
  private String processDefinitionId;

  /** Variable name. */
  private String name;

  /** Variable value (may be encrypted if type is SECRET). */
  private String value;

  /** Whether this variable is a secret or plain text. */
  private VariableType type;

  /** Timestamp when this variable was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when this variable was last updated. */
  private OffsetDateTime updatedAt;

  /** Classification of environment variable storage. */
  public enum VariableType {
    /** Sensitive value, stored encrypted. */
    SECRET,
    /** Non-sensitive value, stored as-is. */
    PLAIN_TEXT
  }
}
