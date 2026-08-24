package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.Connector;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Connector} documents. Handles database operations for
 * connectors.
 */
@Repository
public interface MongoConnectorRepository extends MongoRepository<Connector, String> {

  /**
   * Finds a connector by its unique name.
   *
   * @param connectorName the connector name
   * @return the matching connector, or empty if not found
   */
  Optional<Connector> findByConnectorName(String connectorName);
}
