package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.DecisionDefinition;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link DecisionDefinition} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition}.
 */
@Mapper(componentModel = "spring")
public interface DecisionDefinitionMapper {

  /**
   * Converts a domain DecisionDefinition to a MongoDB DecisionDefinition document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  DecisionDefinition toDocument(
      io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition domain);

  /**
   * Converts a MongoDB DecisionDefinition document to a domain DecisionDefinition.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition toDomain(
      DecisionDefinition document);
}
