package io.telekom.orchest.orchestrest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object summarizing process definition information. Used for listing definitions and
 * checking version status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionInfo {

  /** The resource definition ID. */
  private String definitionId;

  /** The version of the definition. */
  private int version;

  /** Flag indicating if this is a new version deployment. */
  private boolean isNewVersion;
}
