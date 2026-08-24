package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessDefinition;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link ProcessDefinition} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition}.
 */
@Mapper(componentModel = "spring")
public interface ProcessDefinitionMapper {

  /**
   * Converts a domain ProcessDefinition to a MongoDB ProcessDefinition document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  ProcessDefinition toDocument(
      io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition domain);

  /**
   * Converts a MongoDB ProcessDefinition document to a domain ProcessDefinition.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition toDomain(
      ProcessDefinition document);
}
