// package io.telekom.orchest.client.annotations.value;
//
//
// import annotations.io.telekom.orchest.client.ActivatedJob;
// import org.springframework.cglib.core.MethodInfo;
//
// import java.time.Duration;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;
//
// public class OrchestWorkerValue implements ActivatedJob {
//  private String type;
//  private String name;
//  private Duration timeout;
//  private Integer maxJobsActive;
//  private Duration requestTimeout;
//  private Duration pollInterval;
//  private Boolean autoComplete;
//  private List<String> fetchVariables;
//  private Boolean enabled;
//  private MethodInfo methodInfo;
//  private List<String> tenantIds;
//  private Boolean forceFetchAllVariables;
//  private Boolean streamEnabled;
//  private Duration streamTimeout;
//
//
//  @Override
//  public long getKey() {
//    return k;
//  }
//
//  @Override
//  public String getType() {
//    return "";
//  }
//
//  @Override
//  public long getProcessInstanceKey() {
//    return 0;
//  }
//
//  @Override
//  public String getBpmnProcessId() {
//    return "";
//  }
//
//  @Override
//  public int getProcessDefinitionVersion() {
//    return 0;
//  }
//
//  @Override
//  public long getProcessDefinitionKey() {
//    return 0;
//  }
//
//  @Override
//  public String getElementId() {
//    return "";
//  }
//
//  @Override
//  public long getElementInstanceKey() {
//    return 0;
//  }
//
//  @Override
//  public Map<String, String> getCustomHeaders() {
//    return Map.of();
//  }
//
//  @Override
//  public String getWorker() {
//    return "";
//  }
//
//  @Override
//  public int getRetries() {
//    return 0;
//  }
//
//  @Override
//  public long getDeadline() {
//    return 0;
//  }
//
//  @Override
//  public String getVariables() {
//    return "";
//  }
//
//  @Override
//  public Map<String, Object> getVariablesAsMap() {
//    return Map.of();
//  }
//
//  @Override
//  public <T> T getVariablesAsType(Class<T> variableType) {
//    return null;
//  }
//
//  @Override
//  public String toJson() {
//    return "";
//  }
// }
