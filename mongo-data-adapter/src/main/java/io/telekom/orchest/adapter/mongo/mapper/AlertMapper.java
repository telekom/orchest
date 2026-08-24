package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link Alert} and the domain model
 * {@link io.telekom.orchest.api.core.adapters.data.model.Alert}.
 */
@Mapper(componentModel = "spring")
public interface AlertMapper {

  /**
   * Converts a domain Alert to a MongoDB Alert document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  Alert toDocument(io.telekom.orchest.api.core.adapters.data.model.Alert domain);

  /**
   * Converts a MongoDB Alert document to a domain Alert.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.Alert toDomain(Alert document);

  /**
   * Converts a domain AlertRecipients to a MongoDB Recipients document.
   *
   * @param domain the domain recipients object
   * @return the MongoDB recipients document
   */
  Alert.Recipients toDocument(AlertRecipients domain);

  /**
   * Converts a MongoDB Recipients document to a domain AlertRecipients.
   *
   * @param document the MongoDB recipients document
   * @return the domain recipients object
   */
  AlertRecipients toDomain(Alert.Recipients document);
}
