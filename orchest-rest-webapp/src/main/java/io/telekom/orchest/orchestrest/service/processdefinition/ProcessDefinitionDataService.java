package io.telekom.orchest.orchestrest.service.processdefinition;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Data access service for process definition repository operations. */
@Service
@RequiredArgsConstructor
public class ProcessDefinitionDataService {

  private final ProcessDefinitionRepository processDefinitionRepository;

  public Optional<ProcessDefinition> getProcessDefinition(String processDefinitionId) {
    return processDefinitionRepository.getLatestById(processDefinitionId);
  }

  public Optional<ProcessDefinition> getProcessDefinitionsWithVersion(
      String processDefinitionId, Integer version) {
    return processDefinitionRepository.getByIdAndVersion(processDefinitionId, version);
  }

  public List<ProcessDefinition> getProcessDefinitions(int page, int size) {
    return processDefinitionRepository.findAllDefinitions(page, size);
  }

  public List<ProcessDefinition> getProcessDefinitions(int page, int size, List<String> fields) {
    return processDefinitionRepository.findAllDefinitions(page, size, fields);
  }

  public List<ProcessDefinition> findDistinctProcessIds() {
    return processDefinitionRepository.findDistinctProcessIds();
  }

  public boolean deleteProcessDefinition(String processDefinitionId, Integer version) {
    return processDefinitionRepository.deleteByDefinitionIdAndVersion(processDefinitionId, version);
  }
}
