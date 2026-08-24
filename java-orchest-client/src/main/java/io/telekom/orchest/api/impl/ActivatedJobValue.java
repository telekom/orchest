package io.telekom.orchest.api.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.ActivatedJob;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Default implementation of {@link ActivatedJob}. Holds the runtime state of a job received from
 * the engine.
 */
@Getter
@Builder
public class ActivatedJobValue implements ActivatedJob {

  private final Map<String, Object> variables;
  private final int retries;
  private final String processInstanceId;
  private final ServiceTaskNode nodeInformation;
  private final int processDefinitionVersion;
  private final String processDefinitionId;

  @Override
  public long getKey() {
    return 0;
  }

  @Override
  public String getType() {
    return nodeInformation.getWorkerType();
  }

  @Override
  public String getProcessInstanceKey() {
    return processInstanceId;
  }

  @Override
  public String getBpmnProcessId() {
    return processDefinitionId;
  }

  @Override
  public int getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  @Override
  public long getProcessDefinitionKey() {
    return 0;
  }

  @Override
  public Map<String, String> getCustomHeaders() {
    return Map.of();
  }

  @Override
  public String getWorker() {
    return getType();
  }

  @Override
  public int getRetries() {
    return retries;
  }

  @Override
  public long getDeadline() {
    return 0;
  }

  @Override
  public String getVariables() {
    return toJson();
  }

  @Override
  public Map<String, Object> getVariablesMap() {
    return variables;
  }

  @Override
  public <T> T getVariablesAsType(Class<T> variableType) {
    return JsonMapper.convertValue(variables, variableType);
  }

  @Override
  public <T> T getVariablesAsType(String key, Class<T> variableType) {
    return JsonMapper.convertValue(variables.get(key), variableType);
  }

  @Override
  public <T> T getVariablesAsType(String key, TypeReference<T> toValueTypeRef) {
    return JsonMapper.convertValue(variables.get(key), toValueTypeRef);
  }

  @Override
  public String toJson() {
    return JsonMapper.writeToJson(variables);
  }
}
