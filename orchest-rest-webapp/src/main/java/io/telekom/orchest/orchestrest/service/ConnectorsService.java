package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.adapter.mongo.model.Connector;
import io.telekom.orchest.adapter.mongo.repository.MongoConnectorRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Service for managing connectors. Connectors are reusable integration components that can be used
 * within processes. This service handles retrieval and storage of connector definitions.
 */
@Component
@RequiredArgsConstructor
public class ConnectorsService {

  private final MongoConnectorRepository connectorRepository;

  /**
   * Retrieves a paginated list of all connectors.
   *
   * @param page The page number (0-indexed).
   * @param size The number of items per page.
   * @return A page of connectors.
   */
  public Page<Connector> getConnectors(int page, int size) {
    return connectorRepository.findAll(PageRequest.of(page, size));
  }

  /**
   * Retrieves a specific connector by its name.
   *
   * @param connectorName The unique name of the connector.
   * @return An Optional containing the connector if found.
   */
  public Optional<Connector> getConnector(String connectorName) {
    return connectorRepository.findByConnectorName(connectorName);
  }

  /**
   * Uploads and saves a list of connectors. Creates new Connector objects from the provided data
   * map and persists them.
   *
   * @param connectorsData A list of maps, where each map contains connector properties (must
   *     include "name").
   * @return The list of saved connectors.
   */
  public List<Connector> upload(List<Map<String, Object>> connectorsData) {
    List<Connector> connectors =
        connectorsData.stream()
            .map(
                connector -> {
                  Connector newConnector = new Connector();
                  newConnector.setConnectorName(connector.get("name").toString());
                  newConnector.setConnectorData(connector);
                  return newConnector;
                })
            .toList();
    if (!connectorsData.isEmpty()) {
      return connectorRepository.saveAll(connectors);
    }
    return connectors;
  }
}
