package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.SignalEvent;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link SignalEvent} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.SignalEvent}.
 */
@Mapper(componentModel = "spring")
public interface SignalEventMapper {

  /**
   * Converts a domain SignalEvent to a MongoDB SignalEvent document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  SignalEvent toDocument(io.telekom.orchest.api.core.adapters.data.model.SignalEvent domain);

  /**
   * Converts a MongoDB SignalEvent document to a domain SignalEvent.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.SignalEvent toDomain(SignalEvent document);
}
