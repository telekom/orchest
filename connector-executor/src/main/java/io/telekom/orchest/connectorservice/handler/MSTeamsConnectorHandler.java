package io.telekom.orchest.connectorservice.handler;

import static io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils.getDataMappingValue;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.enginecore.bpmn.utils.VariablesUtils;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Microsoft Teams connector handler. Sends messages to Teams chats using the Microsoft Graph API
 * with OAuth (client credentials) authentication.
 */
@Slf4j
@Component
public class MSTeamsConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final long DEFAULT_RETRY_DELAY_MS = 1000;
  private static final String MICROSOFT_GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";
  private static final String MICROSOFT_LOGIN_BASE_URL = "https://login.microsoftonline.com";
  private static final String RESULT_VARIABLE_KEY = "resultVariable";
  private static final String DEFAULT_RESULT_VARIABLE = "teamsResponse";

  private final RestClient restClient;

  public MSTeamsConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.connector-microsoft-teams:1";
  }

  @Override
  public String errorCode() {
    return "MS_TEAMS_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String authType = evaluate("authentication.type", inputMappings, instance);
    String token = evaluate("authentication.token", inputMappings, instance);
    String clientId = evaluate("authentication.clientId", inputMappings, instance);
    String tenantId = evaluate("authentication.tenantId", inputMappings, instance);
    String clientSecret = evaluate("authentication.clientSecret", inputMappings, instance);

    String dataType = evaluate("data.type", inputMappings, instance);
    String dataMethod = evaluate("data.method", inputMappings, instance);
    String chatId = evaluate("data.chatId", inputMappings, instance);
    String content = evaluate("data.content", inputMappings, instance);
    String bodyType =
        getDataMappingValue("data.bodyType", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse("TEXT");

    String resultVariable = extractResultVariable(outputMappings, node);
    String retryBackOffStr = getDataMappingValue("retryBackOff", outputMappings).orElse(null);

    if (chatId == null || chatId.isBlank()) {
      throw new ConnectorException("Chat ID is required for Microsoft Teams connector");
    }
    if (content == null || content.isBlank()) {
      throw new ConnectorException("Message content is required for Microsoft Teams connector");
    }

    String accessToken = token;
    if ("refresh".equalsIgnoreCase(authType)) {
      if (clientId == null
          || clientId.isBlank()
          || tenantId == null
          || tenantId.isBlank()
          || clientSecret == null
          || clientSecret.isBlank()) {
        throw new ConnectorException("OAuth refresh requires clientId, tenantId, and clientSecret");
      }
      accessToken = refreshOAuthToken(tenantId, clientId, clientSecret);
      log.debug("Successfully refreshed OAuth token for Microsoft Teams connector");
    } else if (accessToken == null || accessToken.isBlank()) {
      throw new ConnectorException("Access token is required for Microsoft Teams connector");
    }

    int maxRetries = parseRetryConfig(retryBackOffStr);
    String apiUrl = buildGraphApiUrl(dataType, dataMethod, chatId);
    String requestBody = buildRequestBody(content, bodyType);

    TeamsResponse response = executeWithRetry(apiUrl, accessToken, requestBody, maxRetries);

    log.info(
        "Microsoft Teams connector executed successfully for node: {}, status: {}",
        node.getName(),
        response.getStatusCode());
    return Map.of(resultVariable, buildResponseData(response));
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private String extractResultVariable(List<DataMapping> outputMappings, BaseNode node) {
    String resultVariable = getDataMappingValue(RESULT_VARIABLE_KEY, outputMappings).orElse(null);
    if (resultVariable == null || resultVariable.isEmpty()) {
      Map<String, Object> properties = node.getProperties();
      if (properties != null && properties.containsKey(RESULT_VARIABLE_KEY)) {
        resultVariable = properties.get(RESULT_VARIABLE_KEY).toString();
      }
    }
    if (resultVariable == null || resultVariable.isEmpty()) {
      resultVariable = DEFAULT_RESULT_VARIABLE;
    }
    return resultVariable;
  }

  private String refreshOAuthToken(String tenantId, String clientId, String clientSecret) {
    String tokenUrl = String.format("%s/%s/oauth2/v2.0/token", MICROSOFT_LOGIN_BASE_URL, tenantId);

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "client_credentials");
    formData.add("client_id", clientId);
    formData.add("client_secret", clientSecret);
    formData.add("scope", "https://graph.microsoft.com/.default");

    try {
      log.debug("Refreshing OAuth token for tenant: {}", tenantId);
      var responseEntity =
          restClient
              .post()
              .uri(tokenUrl)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(formData)
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) -> {
                    String errorBody = "";
                    try {
                      errorBody =
                          new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                      log.debug("Could not read error response body", e);
                    }
                    log.error(
                        "OAuth token refresh failed: status={}, body={}",
                        response.getStatusCode(),
                        errorBody);
                    throw new RestClientException(
                        "OAuth token refresh failed: "
                            + response.getStatusCode()
                            + " - "
                            + errorBody);
                  })
              .toEntity(String.class);

      String responseBody = responseEntity.getBody();
      Map<String, Object> tokenResponse =
          JsonMapper.readFromJson(
              responseBody,
              new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
      if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
        throw new ConnectorException("Invalid OAuth token response: missing access_token");
      }
      return tokenResponse.get("access_token").toString();
    } catch (RestClientException e) {
      throw new ConnectorException("Failed to refresh OAuth token: " + e.getMessage(), e);
    }
  }

  private String buildGraphApiUrl(String dataType, String dataMethod, String chatId) {
    if ("chat".equalsIgnoreCase(dataType) && "sendMessageToChat".equalsIgnoreCase(dataMethod)) {
      return String.format("%s/chats/%s/messages", MICROSOFT_GRAPH_API_BASE_URL, chatId);
    }
    return String.format("%s/chats/%s/messages", MICROSOFT_GRAPH_API_BASE_URL, chatId);
  }

  private String buildRequestBody(String content, String bodyType) {
    Map<String, Object> body = new HashMap<>();
    Map<String, Object> bodyContent = new HashMap<>();

    String contentType = "text";
    if ("HTML".equalsIgnoreCase(bodyType)) {
      contentType = "html";
    }

    bodyContent.put("contentType", contentType);
    bodyContent.put("content", content);
    body.put("body", bodyContent);

    return JsonMapper.writeToJson(body);
  }

  private TeamsResponse executeWithRetry(
      String url, String accessToken, String body, int maxRetries) {
    RestClientException lastException = null;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        log.debug(
            "Executing Microsoft Teams API request to {} (attempt {}/{})",
            url,
            attempt,
            maxRetries);
        return executeHttpRequest(url, accessToken, body);
      } catch (RestClientException e) {
        lastException = e;
        log.warn("Microsoft Teams API request attempt {} failed: {}", attempt, e.getMessage());

        if (e.getMessage() != null && e.getMessage().contains("4")) {
          throw e;
        }

        if (attempt < maxRetries) {
          double exponent = attempt - 1.0;
          long delay = (long) (DEFAULT_RETRY_DELAY_MS * Math.pow(2.0, exponent));
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ConnectorException("Retry interrupted", ie);
          }
        }
      }
    }

    throw new ConnectorException(
        "Microsoft Teams API request failed after " + maxRetries + " attempts", lastException);
  }

  private TeamsResponse executeHttpRequest(String url, String accessToken, String body) {
    try {
      var responseEntity =
          restClient
              .post()
              .uri(url)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) -> {
                    String errorBody = "";
                    try {
                      errorBody =
                          new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                      log.debug("Could not read error response body", e);
                    }
                    log.error(
                        "Microsoft Teams API error response: status={}, body={}",
                        response.getStatusCode(),
                        errorBody);
                    throw new RestClientException(
                        "Microsoft Teams API error: "
                            + response.getStatusCode()
                            + " - "
                            + errorBody);
                  })
              .toEntity(String.class);

      int statusCode = responseEntity.getStatusCode().value();
      String responseBody = responseEntity.getBody() != null ? responseEntity.getBody() : "";
      Map<String, String> responseHeaders = new HashMap<>();
      responseEntity
          .getHeaders()
          .forEach(
              (key, values) -> {
                if (!values.isEmpty()) {
                  responseHeaders.put(key, String.join(", ", values));
                }
              });

      return new TeamsResponse(statusCode, responseBody, responseHeaders);
    } catch (RestClientException e) {
      throw e;
    } catch (Exception e) {
      throw new RestClientException(
          "Failed to execute Microsoft Teams API request: " + e.getMessage(), e);
    }
  }

  private int parseRetryConfig(String retryBackOffStr) {
    if (retryBackOffStr == null || retryBackOffStr.isBlank()) {
      return DEFAULT_MAX_RETRIES;
    }
    try {
      if (retryBackOffStr.matches("\\d+")) {
        return Integer.parseInt(retryBackOffStr);
      }
      Map<String, Object> config =
          JsonMapper.readFromJson(
              retryBackOffStr,
              new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
      Object maxRetries = config.get("maxRetries");
      if (maxRetries != null) {
        return Integer.parseInt(maxRetries.toString());
      }
    } catch (Exception e) {
      log.warn("Failed to parse retry configuration: {}", retryBackOffStr, e);
    }
    return DEFAULT_MAX_RETRIES;
  }

  private Map<String, Object> buildResponseData(TeamsResponse response) {
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.getStatusCode());
    responseData.put("body", response.getBody());
    responseData.put("headers", response.getHeaders());

    try {
      if (response.getBody() != null && !response.getBody().isBlank()) {
        Object parsedBody = JsonMapper.readTree(response.getBody());
        responseData.put("bodyJson", parsedBody);
      }
    } catch (Exception e) {
      log.debug("Response body is not JSON, storing as string");
    }
    return responseData;
  }

  private static class TeamsResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public TeamsResponse(int statusCode, String body, Map<String, String> headers) {
      this.statusCode = statusCode;
      this.body = body;
      this.headers = headers;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getBody() {
      return body;
    }

    public Map<String, String> getHeaders() {
      return headers;
    }
  }
}
