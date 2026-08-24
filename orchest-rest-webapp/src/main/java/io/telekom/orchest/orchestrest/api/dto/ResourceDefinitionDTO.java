package io.telekom.orchest.orchestrest.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data transfer object representing a BPMN or DMN resource definition in API responses. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDefinitionDTO {

  /** Unique identifier of the resource definition. */
  private String definitionId;

  /** The raw XML content of the BPMN/DMN resource. */
  private String resourceXML;

  /** Version number of this definition. */
  private Integer version;

  /** Timestamp when this definition version was deployed. */
  private OffsetDateTime createdAt;

  /** Additional metadata extracted from the resource (e.g. element names, documentation). */
  private Map<String, Object> resourceMetadata;
}
