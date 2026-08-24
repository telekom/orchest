package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.WorkerRegistry;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link WorkerRegistry} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry}.
 */
@Mapper(componentModel = "spring")
public interface WorkerRegistryMapper {

  /**
   * Converts a domain WorkerRegistry to a MongoDB WorkerRegistry document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  WorkerRegistry toDocument(io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry domain);

  /**
   * Converts a MongoDB WorkerRegistry document to a domain WorkerRegistry.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry toDomain(WorkerRegistry document);
}
