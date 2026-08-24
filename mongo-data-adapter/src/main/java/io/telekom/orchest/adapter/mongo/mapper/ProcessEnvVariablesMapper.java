package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ProcessEnvVariables;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link ProcessEnvVariables} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables}.
 */
@Mapper(componentModel = "spring")
public interface ProcessEnvVariablesMapper {

  /**
   * Converts a domain ProcessEnvVariables to a MongoDB ProcessEnvVariables document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  ProcessEnvVariables toDocument(
      io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables domain);

  /**
   * Converts a MongoDB ProcessEnvVariables document to a domain ProcessEnvVariables.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables toDomain(
      ProcessEnvVariables document);
}
