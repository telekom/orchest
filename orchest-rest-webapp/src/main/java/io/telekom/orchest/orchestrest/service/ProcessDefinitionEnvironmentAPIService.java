package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessEnvVariablesRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/** Service for managing environment variables associated with process definitions. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionEnvironmentAPIService {

  private final ProcessEnvVariablesRepository processEnvVariablesRepository;

  public ProcessEnvVariables addProcessEnvVariable(ProcessEnvVariables processEnvVariables) {
    return processEnvVariablesRepository.save(processEnvVariables);
  }

  public ProcessEnvVariables deleteProcessEnvVariable(ProcessEnvVariables processEnvVariables) {
    processEnvVariablesRepository.remove(processEnvVariables);
    return processEnvVariables;
  }

  public ProcessEnvVariables updateProcessEnvVariable(ProcessEnvVariables processEnvVariables) {
    return processEnvVariablesRepository.save(processEnvVariables);
  }

  public List<ProcessEnvVariables> getProcessDefinition(String processDefinitionId) {
    return processEnvVariablesRepository.getByDefinitionIdId(processDefinitionId);
  }

  public Page<ProcessEnvVariables> getProcessDefinitions(int page, int size) {
    List<ProcessEnvVariables> processEnvVariables =
        processEnvVariablesRepository.getAll(page, size);
    return PageableExecutionUtils.getPage(
        processEnvVariables, Pageable.ofSize(size), processEnvVariables::size);
  }
}
