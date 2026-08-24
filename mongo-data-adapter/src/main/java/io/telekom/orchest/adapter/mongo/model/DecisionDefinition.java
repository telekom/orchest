package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.dmn.Decision;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a DMN Decision Definition. Stores the structure and logic of a
 * deployed decision model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class DecisionDefinition {

  /** Unique database identifier. */
  @Id private String id;

  /** The logical identifier of the definition group. */
  private String definitionId;

  /** List of individual decision IDs contained within this definition. */
  private List<String> decisionIds;

  /** Version of the definition. */
  private Integer version;

  /** Name of the decision definition. */
  private String name;

  /** Namespace of the decision definition. */
  private String namespace;

  /** Raw XML content of the DMN file. */
  private String definitionXML;

  /** Map of parsed decisions within this definition. */
  @Builder.Default private Map<String, Decision> decisions = new HashMap<>();

  /** Timestamp when the definition was created/deployed. */
  @CreatedDate private OffsetDateTime createdAt;
}
