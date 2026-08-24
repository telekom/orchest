package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between the MongoDB document {@link ProcessInstance} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.ProcessInstance}.
 */
@Mapper(componentModel = "spring")
public interface ProcessInstanceMapper {

  /**
   * Converts a domain ProcessInstance to a MongoDB ProcessInstance document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  ProcessInstance toDocument(
      io.telekom.orchest.api.core.adapters.data.model.ProcessInstance domain);

  /**
   * Converts a MongoDB ProcessInstance document to a domain ProcessInstance.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  @Mapping(target = "processDefinition", ignore = true)
  @Mapping(target = "currentMiSubProcessId", ignore = true)
  @Mapping(target = "currentMiInstanceIndex", ignore = true)
  io.telekom.orchest.api.core.adapters.data.model.ProcessInstance toDomain(
      ProcessInstance document);
}
