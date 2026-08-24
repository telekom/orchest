package io.telekom.orchest.orchestrest.configurations.ai;

import io.telekom.orchest.api.core.adapters.data.model.Incident;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** AI tool functions that provide process instance and incident data to the chat model. */
@RequiredArgsConstructor
public class ProcessInstanceTools {

  private final ProcessInstanceRepository processInstanceRepository;
  private final IncidentRepository incidentRepository;

  @Tool(description = "get processInstance by instanceId")
  public Optional<ProcessInstance> getProcessInstanceByProcessInstanceId(
      @ToolParam(description = "processInstanceId from the use prompt") String instanceId) {
    return processInstanceRepository.getById(instanceId);
  }

  @Tool(description = "To get all the incidents by processInstanceId or processDefinitionId")
  public List<Incident> getIncidentByInstanceId(
      @ToolParam(description = "processInstanceId from the use prompt", required = false)
          String instanceId,
      @ToolParam(description = "processDefinitionId from the use prompt", required = false)
          String processDefinitionId) {
    if (instanceId != null) {
      return incidentRepository.findByProcessInstanceId(instanceId);
    } else if (processDefinitionId != null) {
      return incidentRepository.findByProcessInstanceId(processDefinitionId);
    }
    return Collections.emptyList();
  }
}
