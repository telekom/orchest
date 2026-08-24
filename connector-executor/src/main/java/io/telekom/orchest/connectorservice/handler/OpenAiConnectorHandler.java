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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** OpenAI connector handler. Interacts with the OpenAI Chat Completions and Moderation APIs. */
@Slf4j
@Component
public class OpenAiConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 60;
  private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
  private static final String MODERATIONS_URL = "https://api.openai.com/v1/moderations";
  private static final String DEFAULT_MODEL = "gpt-5";
  private static final String DEFAULT_RESULT_VARIABLE = "openAiConnectorResponse";

  private final RestClient restClient;

  public OpenAiConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.openai:1";
  }

  @Override
  public String errorCode() {
    return "OPENAI_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String apiKey =
        firstNonNull(
            evaluate("authentication.token", inputMappings, instance),
            evaluate("apiKey", inputMappings, instance));
    if (apiKey == null || apiKey.isBlank()) {
      throw new ConnectorException("OpenAI API key is required");
    }
    String organization =
        firstNonNull(
            evaluate("internal_organization", inputMappings, instance),
            evaluate("organization", inputMappings, instance));
    String operation =
        getDataMappingValue("operation", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse("chat");
    String resultVariable = DEFAULT_RESULT_VARIABLE;

    try {
      resultVariable = getDataMappingValue("resultVariable", outputMappings).get();
    } catch (Exception e) {

    }

    String url;
    String requestBody;
    if ("moderation".equalsIgnoreCase(operation)) {
      url = MODERATIONS_URL;
      requestBody = buildModerationBody(inputMappings, instance);
    } else {
      url = CHAT_COMPLETIONS_URL;
      requestBody = buildChatBody(inputMappings, instance);
    }

    OpenAiResponse response = call(url, apiKey, organization, requestBody);
    log.info(
        "OpenAI connector executed successfully for node: {}, operation: {}, status: {}",
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

  private String buildChatBody(List<DataMapping> inputMappings, ProcessInstance instance) {
    String model =
        firstNonNull(
            evaluate("internal_custom_model", inputMappings, instance),
            evaluate("internal_model", inputMappings, instance),
            evaluate("model", inputMappings, instance));
    if (model == null || model.isBlank()) {
      model = DEFAULT_MODEL;
    }
    String prompt =
        firstNonNull(
            evaluate("internal_prompt", inputMappings, instance),
            evaluate("prompt", inputMappings, instance));
    if (prompt == null || prompt.isBlank()) {
      throw new ConnectorException("Prompt is required for OpenAI chat operation");
    }
    String systemMessage =
        firstNonNull(
            evaluate("internal_systemMessage", inputMappings, instance),
            evaluate("systemMessage", inputMappings, instance));
    String temperature =
        firstNonNull(
            evaluate("internal_temperature", inputMappings, instance),
            evaluate("temperature", inputMappings, instance));
    String choices =
        firstNonNull(
            evaluate("internal_choices", inputMappings, instance),
            evaluate("choices", inputMappings, instance));

    List<Map<String, Object>> messages = new ArrayList<>();
    if (systemMessage != null && !systemMessage.isBlank()) {
      messages.add(Map.of("role", "system", "content", systemMessage));
    }
    Object chatHistory =
        getDataMappingValue("internal_chatHistory", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .orElse(null);
    if (chatHistory instanceof List<?> history) {
      for (Object item : history) {
        if (item instanceof Map<?, ?> entry) {
          messages.add(toMessage(entry));
        }
      }
    }
    messages.add(Map.of("role", "user", "content", prompt));

    Map<String, Object> payload = new HashMap<>();
    payload.put("model", model);
    payload.put("messages", messages);
    payload.put("n", choices != null ? safeInt(choices, 1) : 1);
    payload.put("temperature", temperature != null ? safeDouble(temperature, 1.0) : 1.0);
    return JsonMapper.writeToJson(payload);
  }

  private String buildModerationBody(List<DataMapping> inputMappings, ProcessInstance instance) {
    String input =
        firstNonNull(
            evaluate("internal_textToAnalyze", inputMappings, instance),
            evaluate("input", inputMappings, instance));
    if (input == null || input.isBlank()) {
      throw new ConnectorException("Evaluation input is required for OpenAI moderation operation");
    }
    return JsonMapper.writeToJson(Map.of("input", input));
  }

  private OpenAiResponse call(String url, String apiKey, String organization, String body) {
    try {
      AtomicReference<String> errorBody = new AtomicReference<>();
      var responseEntity =
          restClient
              .post()
              .uri(url)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
              .headers(
                  headers -> {
                    if (organization != null && !organization.isBlank()) {
                      headers.set("OpenAI-Organization", organization);
                    }
                  })
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
                    log.warn("OpenAI API returned error response body: " + errorBody);
                  })
              .toEntity(String.class);
      if (errorBody.get() != null) {
        return new OpenAiResponse(responseEntity.getStatusCode().value(), errorBody.get());
      }

      return new OpenAiResponse(responseEntity.getStatusCode().value(), responseEntity.getBody());
    } catch (RestClientException e) {
      throw new ConnectorException("OpenAI connector execution failed: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMessage(Map<?, ?> entry) {
    return (Map<String, Object>) entry;
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

  private record OpenAiResponse(int statusCode, String body) {}
}
