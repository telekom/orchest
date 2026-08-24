package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.ActivityState;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link ActivityState} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.ActivityState}.
 */
@Mapper(componentModel = "spring")
public interface ActivityStateMapper {

  /**
   * Converts a domain ActivityState to a MongoDB ActivityState document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  ActivityState toDocument(io.telekom.orchest.api.core.adapters.data.model.ActivityState domain);

  /**
   * Converts a MongoDB ActivityState document to a domain ActivityState.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.ActivityState toDomain(ActivityState document);
}
