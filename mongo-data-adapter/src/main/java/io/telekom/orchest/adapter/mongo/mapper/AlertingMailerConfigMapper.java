package io.telekom.orchest.adapter.mongo.mapper;

import io.telekom.orchest.adapter.mongo.model.AlertingMailerConfig;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the MongoDB document {@link AlertingMailerConfig} and the
 * domain model {@link io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig}.
 */
@Mapper(componentModel = "spring", uses = AlertMapper.class)
public interface AlertingMailerConfigMapper {

  /**
   * Converts a domain AlertingMailerConfig to a MongoDB AlertingMailerConfig document.
   *
   * @param domain the domain model object
   * @return the MongoDB document object
   */
  AlertingMailerConfig toDocument(
      io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig domain);

  /**
   * Converts a MongoDB AlertingMailerConfig document to a domain AlertingMailerConfig.
   *
   * @param document the MongoDB document object
   * @return the domain model object
   */
  io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig toDomain(
      AlertingMailerConfig document);
}
