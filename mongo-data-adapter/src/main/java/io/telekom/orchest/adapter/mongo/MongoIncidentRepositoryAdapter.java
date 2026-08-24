package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.IncidentMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoIncidentRepository;
import io.telekom.orchest.api.core.adapters.data.model.Incident;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link IncidentRepository}. Delegates to Spring Data MongoDB for
 * incident persistence and retrieval.
 */
@Component
@RequiredArgsConstructor
public class MongoIncidentRepositoryAdapter implements IncidentRepository {
  private final MongoIncidentRepository mongoIncidentRepository;
  private final IncidentMapper mapper;

  /**
   * Finds all incidents associated with the given process instance.
   *
   * @param processInstanceId the process instance identifier
   * @return list of matching incidents
   */
  @Override
  public List<Incident> findByProcessInstanceId(String processInstanceId) {
    return mongoIncidentRepository.findByProcessInstanceId(processInstanceId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds all incidents associated with the given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching incidents
   */
  @Override
  public List<Incident> findByProcessDefinitionId(String processDefinitionId) {
    return mongoIncidentRepository.findByProcessDefinitionId(processDefinitionId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Persists an incident to the database.
   *
   * @param incident the incident to save
   * @return the persisted incident
   */
  @Override
  public Incident save(Incident incident) {
    return mapper.toDomain(mongoIncidentRepository.save(mapper.toDocument(incident)));
  }
}
