package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.PendingTask;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link PendingTask} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.PendingTask}.
 */
@Mapper(componentModel = "spring")
public interface PendingTaskMapper {

  /**
   * Converts a domain PendingTask to a MongoDB PendingTask document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  PendingTask toDocument(io.telekom.orchest.api.core.adapters.data.model.PendingTask domain);

  /**
   * Converts a MongoDB PendingTask document to a domain PendingTask.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.PendingTask toDomain(PendingTask document);
}
