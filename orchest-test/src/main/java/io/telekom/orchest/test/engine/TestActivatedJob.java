package io.telekom.orchest.test.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.client.annotations.ActivatedJob;
import java.util.HashMap;
import java.util.Map;

/**
 * A test-friendly implementation of {@link ActivatedJob} with a fluent builder API.
 *
 * <p>Use this to construct custom {@code ActivatedJob} instances for testing workers that need
 * specific metadata beyond simple variables.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * ActivatedJob job = TestActivatedJob.builder()
 *     .type("process-order")
 *     .processInstanceId("inst-123")
 *     .processDefinitionId("order-process")
 *     .processDefinitionVersion(1)
 *     .variable("orderId", "ORD-001")
 *     .variable("amount", 250.00)
 *     .retries(3)
 *     .build();
 *
 * WorkerTestResult result = workerTester.execute(job);
 * }</pre>
 */
public class TestActivatedJob implements ActivatedJob {

  private final String type;
  private final String processInstanceId;
  private final String processDefinitionId;
  private final int processDefinitionVersion;
  private final Map<String, Object> variables;
  private final Map<String, String> customHeaders;
  private final int retries;

  private TestActivatedJob(Builder builder) {
    this.type = builder.type;
    this.processInstanceId = builder.processInstanceId;
    this.processDefinitionId = builder.processDefinitionId;
    this.processDefinitionVersion = builder.processDefinitionVersion;
    this.variables = builder.variables;
    this.customHeaders = builder.customHeaders;
    this.retries = builder.retries;
  }

  /**
   * Creates a new builder for constructing a test activated job.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public long getKey() {
    return 0;
  }

  @Override
  public String getType() {
    return type;
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
    return customHeaders;
  }

  @Override
  public String getWorker() {
    return type;
  }

  @Override
  public int getRetries() {
    return retries;
  }

  @Override
  public long getDeadline() {
    return Long.MAX_VALUE;
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
  public <T> T getVariablesAsType(String key, Class<T> variableType) {
    return JsonMapper.convertValue(variables.get(key), variableType);
  }

  @Override
  public <T> T getVariablesAsType(String key, TypeReference<T> toValueTypeRef) {
    return JsonMapper.convertValue(variables.get(key), toValueTypeRef);
  }

  @Override
  public <T> T getVariablesAsType(Class<T> variableType) {
    return JsonMapper.convertValue(variables, variableType);
  }

  @Override
  public String toJson() {
    return JsonMapper.writeToJson(variables);
  }

  /** Fluent builder for {@link TestActivatedJob}. */
  public static class Builder {
    private String type = "test-worker";
    private String processInstanceId = "test-instance-" + System.nanoTime();
    private String processDefinitionId = "test-process";
    private int processDefinitionVersion = 1;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, String> customHeaders = new HashMap<>();
    private int retries = 3;

    /** Sets the worker type. */
    public Builder type(String type) {
      this.type = type;
      return this;
    }

    /** Sets the process instance ID. */
    public Builder processInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
      return this;
    }

    /** Sets the process definition ID. */
    public Builder processDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
      return this;
    }

    /** Sets the process definition version. */
    public Builder processDefinitionVersion(int version) {
      this.processDefinitionVersion = version;
      return this;
    }

    /** Sets all variables, replacing any previously set. */
    public Builder variables(Map<String, Object> variables) {
      this.variables = new HashMap<>(variables);
      return this;
    }

    /** Adds a single variable. */
    public Builder variable(String key, Object value) {
      this.variables.put(key, value);
      return this;
    }

    /** Sets all custom headers, replacing any previously set. */
    public Builder customHeaders(Map<String, String> headers) {
      this.customHeaders = new HashMap<>(headers);
      return this;
    }

    /** Adds a single custom header. */
    public Builder customHeader(String key, String value) {
      this.customHeaders.put(key, value);
      return this;
    }

    /** Sets the number of retries. */
    public Builder retries(int retries) {
      this.retries = retries;
      return this;
    }

    /**
     * Builds the {@link TestActivatedJob} instance.
     *
     * @return a new TestActivatedJob
     */
    public TestActivatedJob build() {
      return new TestActivatedJob(this);
    }
  }
}
