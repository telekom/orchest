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
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Azure OpenAI connector handler. Calls Azure-hosted OpenAI deployments for chat completions,
 * completions, and completions extensions operations.
 */
@Slf4j
@Component
public class AzureOpenAiConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 120;
  private static final String DEFAULT_RESULT_VARIABLE = "azureOpenAiConnectorResponse";

  private final RestClient restClient;

  public AzureOpenAiConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.azure-openai:1";
  }

  @Override
  public String errorCode() {
    return "AZURE_OPENAI_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String apiKey = evaluate("authentication.value", inputMappings, instance);
    if (apiKey == null || apiKey.isBlank()) {
      throw new ConnectorException("Azure OpenAI API key is required");
    }

    String operation = evaluate("operation", inputMappings, instance);
    if (operation == null || operation.isBlank()) {
      operation = "chatCompletion";
    }

    String url = evaluate("url", inputMappings, instance);
    if (url == null || url.isBlank()) {
      url = buildUrlFromParts(operation, inputMappings, instance);
    }
    if (url == null || url.isBlank()) {
      throw new ConnectorException("Azure OpenAI URL could not be resolved");
    }

    String body = evaluateBody("body", inputMappings, instance);
    if (body == null || body.isBlank()) {
      body = buildBodyFromParts(operation, inputMappings, instance);
    }
    if (body == null || body.isBlank()) {
      throw new ConnectorException("Azure OpenAI request body is required");
    }

    String resultVariable = DEFAULT_RESULT_VARIABLE;
    try {
      resultVariable = getDataMappingValue("resultVariable", outputMappings).get();
    } catch (Exception e) {
      // use default
    }

    AzureOpenAiResponse response = call(url, apiKey, body);
    log.info(
        "Azure OpenAI connector executed for node: {}, operation: {}, status: {}",
        node.getName(),
        operation,
        response.statusCode());

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.statusCode());
    try {
      responseData.put("body", JsonMapper.readTree(response.body()));
    } catch (Exception e) {
      responseData.put("body", response.body());
    }
    return Map.of(resultVariable, responseData);
  }

  private String buildUrlFromParts(
      String operation, List<DataMapping> inputMappings, ProcessInstance instance) {
    String prefix =
        switch (operation) {
          case "completion" -> "completion";
          case "completionsExtension" -> "completionsExtension";
          default -> "chatCompletion";
        };

    String resourceName = evaluate(prefix + "_resourceName", inputMappings, instance);
    String deploymentId = evaluate(prefix + "_deploymentId", inputMappings, instance);
    String apiVersion = evaluate(prefix + "_apiVersion", inputMappings, instance);

    if (resourceName == null || deploymentId == null) {
      return null;
    }
    if (apiVersion == null || apiVersion.isBlank()) {
      apiVersion = "2024-02-01";
    }

    String endpoint = "completion".equals(operation) ? "completions" : "chat/completions";
    return "https://"
        + resourceName
        + ".openai.azure.com/openai/deployments/"
        + deploymentId
        + "/"
        + endpoint
        + "?api-version="
        + apiVersion;
  }

  private String buildBodyFromParts(
      String operation, List<DataMapping> inputMappings, ProcessInstance instance) {
    if ("chatCompletion".equals(operation) || "completionsExtension".equals(operation)) {
      String prefix =
          "chatCompletion".equals(operation) ? "chatCompletion" : "completionsExtension";
      String messageContent = evaluate(prefix + "_messageContent", inputMappings, instance);
      String messageRole = evaluate(prefix + "_messageRole", inputMappings, instance);
      if (messageRole == null) messageRole = "user";

      if (messageContent == null || messageContent.isBlank()) {
        return null;
      }

      Map<String, Object> payload = new HashMap<>();
      payload.put("messages", List.of(Map.of("role", messageRole, "content", messageContent)));

      String temperature = evaluate(prefix + "_temperature", inputMappings, instance);
      if (temperature != null) payload.put("temperature", safeDouble(temperature, 1.0));
      String maxTokens = evaluate(prefix + "_maxTokens", inputMappings, instance);
      if (maxTokens != null) payload.put("max_tokens", safeInt(maxTokens, 800));

      return JsonMapper.writeToJson(payload);
    }

    // completion operation
    String prompt = evaluate("completion_prompt", inputMappings, instance);
    if (prompt == null || prompt.isBlank()) return null;

    Map<String, Object> payload = new HashMap<>();
    payload.put("prompt", prompt);
    String maxTokens = evaluate("completion_maxTokens", inputMappings, instance);
    if (maxTokens != null) payload.put("max_tokens", safeInt(maxTokens, 16));
    String temperature = evaluate("completion_temperature", inputMappings, instance);
    if (temperature != null) payload.put("temperature", safeDouble(temperature, 1.0));

    return JsonMapper.writeToJson(payload);
  }

  private AzureOpenAiResponse call(String url, String apiKey, String body) {
    try {
      AtomicReference<String> errorBody = new AtomicReference<>();
      var responseEntity =
          restClient
              .post()
              .uri(url)
              .header("api-key", apiKey)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) -> {
                    try {
                      errorBody.set(
                          new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                    } catch (Exception e) {
                      log.debug("Could not read error response body", e);
                    }
                    log.error(
                        "Azure OpenAI API error: {} - {}",
                        response.getStatusCode(),
                        errorBody.get());
                  })
              .toEntity(String.class);
      if (errorBody.get() != null) {
        return new AzureOpenAiResponse(responseEntity.getStatusCode().value(), errorBody.get());
      }
      return new AzureOpenAiResponse(
          responseEntity.getStatusCode().value(), responseEntity.getBody());

    } catch (RestClientException e) {
      throw new ConnectorException("Azure OpenAI connector execution failed: " + e.getMessage(), e);
    }
  }

  private int safeInt(String value, int fallback) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private double safeDouble(String value, double fallback) {
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private String evaluateBody(
      String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(obj -> obj instanceof String s ? s : JsonMapper.writeToJson(obj))
        .orElse(null);
  }

  private record AzureOpenAiResponse(int statusCode, String body) {}
}
