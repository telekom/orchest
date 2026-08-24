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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * REST / HTTP-JSON connector handler. Supports HTTP methods, authentication, custom headers,
 * request bodies, retry logic and error handling.
 */
@Slf4j
@Component
public class RestConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final long DEFAULT_RETRY_DELAY_MS = 1000;

  private final RestClient restClient;

  public RestConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.http-json:1";
  }

  @Override
  public String errorCode() {
    return "REST_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String url =
        getDataMappingValue("url", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(null);

    String method =
        getDataMappingValue("method", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse("GET");

    String authenticationType =
        getDataMappingValue("authentication.type", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(null);

    String username =
        getDataMappingValue("authentication.useName", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(null);

    String password =
        getDataMappingValue("authentication.password", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(null);

    String headersStr =
        getDataMappingValue("headers", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(null);

    String requestBody =
        getDataMappingValue("body", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(obj -> obj instanceof String string ? string : JsonMapper.writeToJson(obj))
            .orElse(null);

    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse("restResponse");
    String retryBackOffStr = getDataMappingValue("retryBackOff", outputMappings).orElse(null);

    if (url == null || url.isBlank()) {
      throw new ConnectorException("URL is required for REST connector");
    }

    int maxRetries = parseRetryConfig(retryBackOffStr);

    RestResponse response =
        executeWithRetry(
            url,
            method,
            authenticationType,
            username,
            password,
            headersStr,
            requestBody,
            maxRetries);

    log.info(
        "REST connector executed successfully for node: {}, status: {}",
        node.getName(),
        response.getStatusCode());
    return Map.of(resultVariable, buildResponseData(response));
  }

  private RestResponse executeWithRetry(
      String url,
      String method,
      String authType,
      String username,
      String password,
      String headersStr,
      String body,
      int maxRetries) {
    RestClientException lastException = null;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        log.debug(
            "Executing HTTP {} request to {} (attempt {}/{})", method, url, attempt, maxRetries);
        return executeHttpRequest(url, method, authType, username, password, headersStr, body);
      } catch (RestClientException e) {
        lastException = e;
        log.warn("HTTP request attempt {} failed: {}", attempt, e.getMessage());

        if (e.getMessage() != null && e.getMessage().contains("4")) {
          throw e;
        }

        if (attempt < maxRetries) {
          long delay = DEFAULT_RETRY_DELAY_MS * (long) Math.pow(2.0, attempt - 1);
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
        "HTTP request failed after " + maxRetries + " attempts", lastException);
  }

  private RestResponse executeHttpRequest(
      String url,
      String method,
      String authType,
      String username,
      String password,
      String headersStr,
      String body) {
    HttpMethod httpMethod = parseHttpMethod(method);
    RestClient.RequestBodySpec requestSpec =
        restClient
            .method(httpMethod)
            .uri(url)
            .headers(
                headers -> configureHeaders(headers, authType, username, password, headersStr));

    if (body != null && !body.isBlank() && supportsRequestBody(httpMethod)) {
      requestSpec.body(body);
    }

    try {
      var responseEntity =
          requestSpec
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
                        "HTTP error response: status={}, body={}",
                        response.getStatusCode(),
                        errorBody);
                    throw new RestClientException(
                        "HTTP error: " + response.getStatusCode() + " - " + errorBody);
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

      return new RestResponse(statusCode, responseBody, responseHeaders);
    } catch (RestClientException e) {
      throw e;
    } catch (Exception e) {
      throw new RestClientException("Failed to execute HTTP request: " + e.getMessage(), e);
    }
  }

  private void configureHeaders(
      HttpHeaders headers, String authType, String username, String password, String headersStr) {
    if (!headers.containsHeader(HttpHeaders.CONTENT_TYPE)) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }

    if ("basic".equalsIgnoreCase(authType) && username != null && password != null) {
      String auth = username + ":" + password;
      String encodedAuth =
          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
      headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
    }

    if (headersStr != null && !headersStr.isBlank()) {
      parseHeaders(headersStr)
          .forEach(
              (key, value) -> {
                if (value != null) {
                  headers.set(key, value);
                }
              });
    }
  }

  private Map<String, String> parseHeaders(String headersStr) {
    Map<String, String> headersMap = new HashMap<>();
    try {
      if (headersStr.trim().startsWith("{")) {
        Map<String, Object> parsed =
            JsonMapper.readFromJson(
                headersStr,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        parsed.forEach((key, value) -> headersMap.put(key, value != null ? value.toString() : ""));
      } else {
        String[] lines = headersStr.split("\n");
        for (String line : lines) {
          int colonIndex = line.indexOf(':');
          if (colonIndex > 0) {
            String key = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            headersMap.put(key, value);
          }
        }
      }
    } catch (Exception e) {
      log.warn("Failed to parse headers string: {}", headersStr, e);
    }
    return headersMap;
  }

  private HttpMethod parseHttpMethod(String method) {
    if (method == null || method.isBlank()) {
      return HttpMethod.GET;
    }
    try {
      return HttpMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid HTTP method: {}, defaulting to GET", method);
      return HttpMethod.GET;
    }
  }

  private boolean supportsRequestBody(HttpMethod method) {
    return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
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

  private Map<String, Object> buildResponseData(RestResponse response) {
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

  private static class RestResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public RestResponse(int statusCode, String body, Map<String, String> headers) {
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
