package io.telekom.orchest.adapter.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing the active/inactive state of a process definition. Used to enable
 * or disable process execution at runtime.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ProcessState {
  /** Unique database identifier. */
  @Id private String id;

  /** The process definition ID this state applies to. */
  private String processId;

  /** Whether the process is currently active (enabled for execution). */
  private boolean status;
}
