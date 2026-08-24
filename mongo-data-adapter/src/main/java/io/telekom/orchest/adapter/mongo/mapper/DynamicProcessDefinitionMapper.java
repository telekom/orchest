package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link DynamicProcessDefinition} and
 * the domain model {@link
 * io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition}.
 */
@Mapper(componentModel = "spring")
public interface DynamicProcessDefinitionMapper {

  /**
   * Converts a domain ProcessDefinition to a MongoDB ProcessDefinition document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  io.telekom.orchest.adapter.mongo.model.DynamicProcessDefinition toDocument(
      io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition domain);

  /**
   * Converts a MongoDB ProcessDefinition document to a domain ProcessDefinition.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition toDomain(
      io.telekom.orchest.adapter.mongo.model.DynamicProcessDefinition document);
}
