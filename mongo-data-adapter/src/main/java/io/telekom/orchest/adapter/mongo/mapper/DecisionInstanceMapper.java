package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link DecisionInstance} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.DecisionInstance}.
 */
@Mapper(componentModel = "spring")
public interface DecisionInstanceMapper {

  /**
   * Converts a domain DecisionInstance to a MongoDB DecisionInstance document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  DecisionInstance toDocument(
      io.telekom.orchest.api.core.adapters.data.model.DecisionInstance domain);

  /**
   * Converts a MongoDB DecisionInstance document to a domain DecisionInstance.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.DecisionInstance toDomain(
      DecisionInstance document);
}
