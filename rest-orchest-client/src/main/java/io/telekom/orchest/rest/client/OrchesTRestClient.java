package io.telekom.orchest.rest.client;

import io.telekom.orchest.client.api.*;
import io.telekom.orchest.client.invoker.ApiClient;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Pure Java client for the OrchesT REST API. HTTP is implemented with {@link
 * java.net.http.HttpClient} (Java 11+); JSON uses Jackson via the generated {@link ApiClient}.
 * Framework-agnostic: use from plain Java, Spring, Ktor, or any other environment.
 *
 * <p>All operations from the OpenAPI spec are available via the API accessors below. Use {@link
 * #create(String, TokenAuthInjector)} to inject a token (or custom auth header) into every API
 * request. Use {@link #builder(String)} or {@link OrchesTClientConfig} to set timeouts for REST
 * calls. Use {@link #create(HttpClient.Builder, String, TokenAuthInjector)} when you need a custom
 * {@code HttpClient} (SSL, proxy, etc.).
 */
public final class OrchesTRestClient {

  private final ApiClient apiClient;
  private final ProcessInstanceApi processInstanceApi;
  private final ProcessDefinitionApi processDefinitionApi;
  private final ProcessDefinitionEnvironmentApi processDefinitionEnvironmentApi;
  private final ProcessDefinitionSensitiveVariables processDefinitionSensitiveVariablesApi;
  private final UserTasks userTasksApi;
  private final VariablesApi variablesApi;
  private final RateLimitApi rateLimitApi;
  private final DeploymentApprovals deploymentApprovalsApi;
  private final DecisionDefinitionApi decisionDefinitionApi;
  private final DecisionInstanceApi decisionInstanceApi;
  private final ConnectorsApi connectorsApi;
  private final DynamicProcessInstanceApi dynamicProcessInstanceApi;
  private final Eventing eventingApi;
  private final FeelPlaygroundApi feelPlaygroundApi;
  private final IncidentManagementApi incidentManagementApi;
  private final KafkaInspectionApi kafkaInspectionApi;
  private final OrchestChatApi orchestChatApi;
  private final ProcessStateApi processStateApi;
  private final StatisticsApi statisticsApi;
  private final UserApiTokenApi userApiTokenApi;
  private final AuditTrailApi auditTrailApi;

  private OrchesTRestClient(ApiClient apiClient) {
    this.apiClient = apiClient;
    this.processInstanceApi = new ProcessInstanceApi(apiClient);
    this.processDefinitionApi = new ProcessDefinitionApi(apiClient);
    this.processDefinitionEnvironmentApi = new ProcessDefinitionEnvironmentApi(apiClient);
    this.processDefinitionSensitiveVariablesApi =
        new ProcessDefinitionSensitiveVariables(apiClient);
    this.userTasksApi = new UserTasks(apiClient);
    this.variablesApi = new VariablesApi(apiClient);
    this.rateLimitApi = new RateLimitApi(apiClient);
    this.deploymentApprovalsApi = new DeploymentApprovals(apiClient);
    this.decisionDefinitionApi = new DecisionDefinitionApi(apiClient);
    this.decisionInstanceApi = new DecisionInstanceApi(apiClient);
    this.connectorsApi = new ConnectorsApi(apiClient);
    this.dynamicProcessInstanceApi = new DynamicProcessInstanceApi(apiClient);
    this.eventingApi = new Eventing(apiClient);
    this.feelPlaygroundApi = new FeelPlaygroundApi(apiClient);
    this.incidentManagementApi = new IncidentManagementApi(apiClient);
    this.kafkaInspectionApi = new KafkaInspectionApi(apiClient);
    this.orchestChatApi = new OrchestChatApi(apiClient);
    this.processStateApi = new ProcessStateApi(apiClient);
    this.statisticsApi = new StatisticsApi(apiClient);
    this.userApiTokenApi = new UserApiTokenApi(apiClient);
    this.auditTrailApi = new AuditTrailApi(apiClient);
  }

  /**
   * Create a client with the given base URL (e.g. {@code https://api-orchest.example.com/orchest}).
   */
  public static OrchesTRestClient create(String baseUrl) {
    return create(baseUrl, OrchesTClientConfig.defaults());
  }

  /**
   * Create a client with the given base URL and token auth injector. The injector is applied to
   * every request for all APIs (e.g. Bearer token or custom header).
   *
   * @param baseUrl base URL of the OrchesT API
   * @param authInjector e.g. {@link TokenAuthInjector#bearer(String)} or {@link
   *     TokenAuthInjector#header(String, String)}
   */
  public static OrchesTRestClient create(String baseUrl, TokenAuthInjector authInjector) {
    return create(baseUrl, authInjector, OrchesTClientConfig.defaults());
  }

  /**
   * Create a client with the given base URL and config for timeouts.
   *
   * @param baseUrl base URL of the OrchesT API
   * @param config timeout settings; use {@link OrchesTClientConfig#defaults()} or {@link
   *     OrchesTClientConfig#builder()}
   */
  public static OrchesTRestClient create(String baseUrl, OrchesTClientConfig config) {
    ApiClient apiClient = buildApiClient(config, null);
    apiClient.updateBaseUri(baseUrl);
    return new OrchesTRestClient(apiClient);
  }

  /**
   * Create a client with the given base URL, token auth injector, and config for timeouts.
   *
   * @param baseUrl base URL of the OrchesT API
   * @param authInjector e.g. {@link TokenAuthInjector#bearer(String)} or {@link
   *     TokenAuthInjector#header(String, String)}
   * @param config timeout settings; use {@link OrchesTClientConfig#defaults()} or {@link
   *     OrchesTClientConfig#builder()}
   */
  public static OrchesTRestClient create(
      String baseUrl, TokenAuthInjector authInjector, OrchesTClientConfig config) {
    ApiClient apiClient = buildApiClient(config, authInjector);
    apiClient.updateBaseUri(baseUrl);
    return new OrchesTRestClient(apiClient);
  }

  /**
   * Create a builder to configure base URL, optional auth, and timeouts, then build the client.
   *
   * @param baseUrl base URL of the OrchesT API
   */
  public static Builder builder(String baseUrl) {
    return new Builder(baseUrl);
  }

  private static ApiClient buildApiClient(
      OrchesTClientConfig config, TokenAuthInjector authInjector) {
    ApiClient apiClient = new ApiClient();
    apiClient.setConnectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()));
    apiClient.setReadTimeout(Duration.ofMillis(config.getReadTimeoutMs()));
    if (authInjector != null) {
      apiClient.setRequestInterceptor(authInjector.toRequestInterceptor());
    }
    return apiClient;
  }

  /**
   * Create a client with a custom {@link ApiClient}. Use this to set auth, timeouts, or your own
   * {@link HttpClient.Builder}.
   */
  public static OrchesTRestClient create(ApiClient apiClient) {
    return new OrchesTRestClient(apiClient);
  }

  /**
   * Create a client with a custom {@link HttpClient.Builder} and base URL (full URL, including
   * scheme and path).
   */
  public static OrchesTRestClient create(HttpClient.Builder httpClientBuilder, String baseUrl) {
    return create(httpClientBuilder, baseUrl, null);
  }

  /**
   * Create a client with a custom {@link HttpClient.Builder}, base URL, and token auth injector.
   */
  public static OrchesTRestClient create(
      HttpClient.Builder httpClientBuilder, String baseUrl, TokenAuthInjector authInjector) {
    ApiClient client = new ApiClient();
    client.setHttpClientBuilder(httpClientBuilder);
    client.updateBaseUri(baseUrl);
    if (authInjector != null) {
      client.setRequestInterceptor(authInjector.toRequestInterceptor());
    }
    return new OrchesTRestClient(client);
  }

  /** Builder for OrchesTClient with configurable base URL, optional auth, and timeouts. */
  public static final class Builder {
    private final String baseUrl;
    private TokenAuthInjector authInjector;
    private OrchesTClientConfig config;

    private Builder(String baseUrl) {
      this.baseUrl = baseUrl;
      this.config = OrchesTClientConfig.defaults();
    }

    /** Set token auth injector (e.g. Bearer token or custom header) for all API requests. */
    public Builder auth(TokenAuthInjector authInjector) {
      this.authInjector = authInjector;
      return this;
    }

    /** Set full config for timeouts. Replaces any previous config or timeout settings. */
    public Builder config(OrchesTClientConfig config) {
      this.config = config;
      return this;
    }

    /** Connect timeout (time to establish connection). Default 10 seconds. */
    public Builder connectTimeout(long duration, TimeUnit unit) {
      this.config =
          OrchesTClientConfig.builder()
              .connectTimeout(duration, unit)
              .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
              .writeTimeout(config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
              .build();
      return this;
    }

    /** Read timeout (time between each read from server). Default 30 seconds. */
    public Builder readTimeout(long duration, TimeUnit unit) {
      this.config =
          OrchesTClientConfig.builder()
              .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
              .readTimeout(duration, unit)
              .writeTimeout(config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
              .build();
      return this;
    }

    /**
     * Write timeout (legacy field; not applied by the built-in HTTP client). Default 30 seconds.
     */
    public Builder writeTimeout(long duration, TimeUnit unit) {
      this.config =
          OrchesTClientConfig.builder()
              .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
              .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
              .writeTimeout(duration, unit)
              .build();
      return this;
    }

    public OrchesTRestClient build() {
      return create(baseUrl, authInjector, config);
    }
  }

  /**
   * Returns the underlying API client for advanced configuration.
   *
   * @return the API client instance
   */
  public ApiClient getApiClient() {
    return apiClient;
  }

  /**
   * Returns the process instance API for managing process instances.
   *
   * @return process instance API
   */
  public ProcessInstanceApi getProcessInstanceApi() {
    return processInstanceApi;
  }

  /**
   * Returns the process definition API for deploying and querying process definitions.
   *
   * @return process definition API
   */
  public ProcessDefinitionApi getProcessDefinitionApi() {
    return processDefinitionApi;
  }

  /**
   * Returns the process definition environment API.
   *
   * @return process definition environment API
   */
  public ProcessDefinitionEnvironmentApi getProcessDefinitionEnvironmentApi() {
    return processDefinitionEnvironmentApi;
  }

  /**
   * Returns the user tasks API for claiming and completing user tasks.
   *
   * @return user tasks API
   */
  public UserTasks getUserTasksApi() {
    return userTasksApi;
  }

  /**
   * Returns the variables API for reading and writing process variables.
   *
   * @return variables API
   */
  public VariablesApi getVariablesApi() {
    return variablesApi;
  }

  /**
   * Returns the rate limit API for configuring rate limits.
   *
   * @return rate limit API
   */
  public RateLimitApi getRateLimitApi() {
    return rateLimitApi;
  }

  /**
   * Returns the deployment approvals API.
   *
   * @return deployment approvals API
   */
  public DeploymentApprovals getDeploymentApprovalsApi() {
    return deploymentApprovalsApi;
  }

  /**
   * Returns the decision definition API for deploying and querying DMN decisions.
   *
   * @return decision definition API
   */
  public DecisionDefinitionApi getDecisionDefinitionApi() {
    return decisionDefinitionApi;
  }

  /**
   * Returns the decision instance API for querying decision evaluation results.
   *
   * @return decision instance API
   */
  public DecisionInstanceApi getDecisionInstanceApi() {
    return decisionInstanceApi;
  }

  /**
   * Returns the connectors API for managing connector configurations.
   *
   * @return connectors API
   */
  public ConnectorsApi getConnectorsApi() {
    return connectorsApi;
  }

  /**
   * Returns the dynamic process instance API for ad-hoc process operations.
   *
   * @return dynamic process instance API
   */
  public DynamicProcessInstanceApi getDynamicProcessInstanceApi() {
    return dynamicProcessInstanceApi;
  }

  /**
   * Returns the eventing API for publishing and subscribing to events.
   *
   * @return eventing API
   */
  public Eventing getEventingApi() {
    return eventingApi;
  }

  /**
   * Returns the FEEL playground API for evaluating FEEL expressions.
   *
   * @return FEEL playground API
   */
  public FeelPlaygroundApi getFeelPlaygroundApi() {
    return feelPlaygroundApi;
  }

  /**
   * Returns the incident management API for handling process incidents.
   *
   * @return incident management API
   */
  public IncidentManagementApi getIncidentManagementApi() {
    return incidentManagementApi;
  }

  /**
   * Returns the Kafka inspection API for viewing Kafka topic state.
   *
   * @return Kafka inspection API
   */
  public KafkaInspectionApi getKafkaInspectionApi() {
    return kafkaInspectionApi;
  }

  /**
   * Returns the OrchesT chat API for AI-assisted interactions.
   *
   * @return OrchesT chat API
   */
  public OrchestChatApi getOrchestChatApi() {
    return orchestChatApi;
  }

  /**
   * Returns the process state API for querying process execution state.
   *
   * @return process state API
   */
  public ProcessStateApi getProcessStateApi() {
    return processStateApi;
  }

  /**
   * Returns the statistics API for retrieving platform metrics.
   *
   * @return statistics API
   */
  public StatisticsApi getStatisticsApi() {
    return statisticsApi;
  }

  /**
   * Returns the user API token API for managing API tokens.
   *
   * @return user API token API
   */
  public UserApiTokenApi getUserApiTokenApi() {
    return userApiTokenApi;
  }

  /**
   * Returns the audit trail API for querying audit logs.
   *
   * @return audit trail API
   */
  public AuditTrailApi getAuditTrailApi() {
    return auditTrailApi;
  }

  /**
   * Returns the process definition sensitive variables API.
   *
   * @return sensitive variables API
   */
  public ProcessDefinitionSensitiveVariables getProcessDefinitionSensitiveVariablesApi() {
    return processDefinitionSensitiveVariablesApi;
  }
}
