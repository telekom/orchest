package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.TimedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between the MongoDB document {@link TimedEvent} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.TimedEvent}.
 */
@Mapper(componentModel = "spring")
public interface TimedEventMapper {

  /**
   * Converts a domain TimedEvent to a MongoDB TimedEvent document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  TimedEvent toDocument(io.telekom.orchest.api.core.adapters.data.model.TimedEvent domain);

  /**
   * Converts a MongoDB TimedEvent document to a domain TimedEvent. Maps the {@code linkedEvent}
   * field to {@code isLinkedEvent} in the domain model.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  @Mapping(source = "linkedEvent", target = "isLinkedEvent")
  io.telekom.orchest.api.core.adapters.data.model.TimedEvent toDomain(TimedEvent document);
}
