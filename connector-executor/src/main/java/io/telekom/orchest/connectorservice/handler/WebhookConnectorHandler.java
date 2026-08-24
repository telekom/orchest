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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Webhook connector handler. Invokes an external webhook endpoint (outbound HTTP call) and returns
 * the response. Note: Camunda's webhook element template is primarily an inbound trigger; in the
 * execute-and-resume connector-service model this handler implements the outbound "call a webhook"
 * use case (POST a JSON payload to a configured URL).
 */
@Slf4j
@Component
public class WebhookConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final String DEFAULT_RESULT_VARIABLE = "webhookResponse";

  private final RestClient restClient;

  public WebhookConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.webhook:1";
  }

  @Override
  public String errorCode() {
    return "WEBHOOK_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String url =
        firstNonNull(
            evaluate("url", inputMappings, instance),
            evaluate("webhookUrl", inputMappings, instance));
    if (url == null || url.isBlank()) {
      throw new ConnectorException("Webhook URL is required for Webhook connector");
    }
    String method =
        getDataMappingValue("method", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse("POST");
    String headersStr = evaluate("headers", inputMappings, instance);
    String body =
        getDataMappingValue("body", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(obj -> obj instanceof String string ? string : JsonMapper.writeToJson(obj))
            .orElse(null);
    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse(DEFAULT_RESULT_VARIABLE);

    WebhookResponse response = call(url, method, headersStr, body);
    log.info(
        "Webhook connector executed successfully for node: {}, status: {}",
        node.getName(),
        response.statusCode());

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.statusCode());
    responseData.put("body", response.body());
    try {
      if (response.body() != null && !response.body().isBlank()) {
        responseData.put("bodyJson", JsonMapper.readTree(response.body()));
      }
    } catch (Exception e) {
      log.debug("Webhook response body is not JSON, storing as string");
    }
    return Map.of(resultVariable, responseData);
  }

  private WebhookResponse call(String url, String method, String headersStr, String body) {
    HttpMethod httpMethod = parseMethod(method);
    try {
      RestClient.RequestBodySpec spec =
          restClient
              .method(httpMethod)
              .uri(url)
              .headers(headers -> applyHeaders(headers, headersStr));
      if (body != null
          && !body.isBlank()
          && (httpMethod == HttpMethod.POST
              || httpMethod == HttpMethod.PUT
              || httpMethod == HttpMethod.PATCH)) {
        spec.body(body);
      }
      var responseEntity =
          spec.retrieve()
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
                    throw new RestClientException(
                        "Webhook error: " + response.getStatusCode() + " - " + errorBody);
                  })
              .toEntity(String.class);
      return new WebhookResponse(responseEntity.getStatusCode().value(), responseEntity.getBody());
    } catch (RestClientException e) {
      throw new ConnectorException("Webhook connector execution failed: " + e.getMessage(), e);
    }
  }

  private void applyHeaders(HttpHeaders headers, String headersStr) {
    if (!headers.containsHeader(HttpHeaders.CONTENT_TYPE)) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    if (headersStr == null || headersStr.isBlank()) {
      return;
    }
    try {
      if (headersStr.trim().startsWith("{")) {
        Map<String, Object> parsed =
            JsonMapper.readFromJson(
                headersStr,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        parsed.forEach(
            (key, value) -> {
              if (value != null) {
                headers.set(key, value.toString());
              }
            });
      }
    } catch (Exception e) {
      log.warn("Failed to parse webhook headers: {}", headersStr, e);
    }
  }

  private HttpMethod parseMethod(String method) {
    if (method == null || method.isBlank()) {
      return HttpMethod.POST;
    }
    try {
      return HttpMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
      return HttpMethod.POST;
    }
  }

  private String firstNonNull(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private record WebhookResponse(int statusCode, String body) {}
}
