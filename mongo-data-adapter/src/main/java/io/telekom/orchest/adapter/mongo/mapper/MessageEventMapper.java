package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.MessageEventStore;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link MessageEventStore} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.MessageEventStore}.
 */
@Mapper(componentModel = "spring")
public interface MessageEventMapper {

  /**
   * Converts a domain MessageEventStore to a MongoDB MessageEventStore document.
   *
   * @param domain The domain model object.
   * @return The MongoDB document object.
   */
  MessageEventStore toDocument(
      io.telekom.orchest.api.core.adapters.data.model.MessageEventStore domain);

  /**
   * Converts a MongoDB MessageEventStore document to a domain MessageEventStore.
   *
   * @param document The MongoDB document object.
   * @return The domain model object.
   */
  io.telekom.orchest.api.core.adapters.data.model.MessageEventStore toDomain(
      MessageEventStore document);
}
