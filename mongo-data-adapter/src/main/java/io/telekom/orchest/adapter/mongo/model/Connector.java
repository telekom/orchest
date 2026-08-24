package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a Connector. Connectors are reusable components with configuration
 * data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Connector {

  /** Unique identifier for the connector. */
  @Id private String id;

  /** The unique name of the connector. */
  private String connectorName;

  /** Configuration data for the connector (JSON-like map). */
  private Map<String, Object> connectorData;

  /** Timestamp when the connector was created. */
  @CreatedDate private OffsetDateTime createdAt;
}
