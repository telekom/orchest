package io.telekom.orchest.client.annotations.value;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.client.annotations.ActivatedJob;
import java.util.Map;
import lombok.Builder;

/**
 * Default implementation of {@link ActivatedJob}. Holds the runtime state of a job received from
 * the engine.
 */
@Builder
public class ActivatedJobValue implements ActivatedJob {

  private Map<String, Object> variables;
  private int retries;
  private String processInstanceId;
  private ServiceTaskNode nodeInformation;
  private int processDefinitionVersion;
  private String processDefinitionId;

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
