package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.Incident;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link Incident} and the domain
 * model {@link io.telekom.orchest.api.core.adapters.data.model.Incident}.
 */
@Mapper(componentModel = "spring", uses = IncidentMapper.class)
public interface IncidentMapper {

  /**
   * Converts a domain Incident to a MongoDB Incident document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  Incident toDocument(io.telekom.orchest.api.core.adapters.data.model.Incident domain);

  /**
   * Converts a MongoDB Incident document to a domain Incident.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.Incident toDomain(Incident document);
}
