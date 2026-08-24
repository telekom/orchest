package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessState;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link ProcessState} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.ProcessState}.
 */
@Mapper(componentModel = "spring")
public interface ProcessStateMapper {

  /**
   * Converts a domain ProcessState to a MongoDB ProcessState document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  ProcessState toDocument(io.telekom.orchest.api.core.adapters.data.model.ProcessState domain);

  /**
   * Converts a MongoDB ProcessState document to a domain ProcessState.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.ProcessState toDomain(ProcessState document);
}
