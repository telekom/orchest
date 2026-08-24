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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Slack connector handler. Calls the Slack Web API (e.g. {@code chat.postMessage}) using a Bearer
 * OAuth token. Supports plain-text and message-block messages for {@code chat.postMessage}, and
 * passes the {@code data.*} payload through for other Slack methods.
 */
@Slf4j
@Component
public class SlackConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final String SLACK_API_BASE_URL = "https://slack.com/api";
  private static final String DEFAULT_METHOD = "chat.postMessage";
  private static final String DEFAULT_RESULT_VARIABLE = "slackResponse";

  private final RestClient restClient;

  public SlackConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.slack:1";
  }

  @Override
  public String errorCode() {
    return "SLACK_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String token = evaluate("token", inputMappings, instance);
    String method =
        getDataMappingValue("method", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(DEFAULT_METHOD);
    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse(DEFAULT_RESULT_VARIABLE);

    if (token == null || token.isBlank()) {
      throw new ConnectorException("OAuth token is required for Slack connector");
    }

    String requestBody = buildRequestBody(method, inputMappings, instance);
    String apiUrl = String.format("%s/%s", SLACK_API_BASE_URL, method);

    SlackResponse response = callSlack(apiUrl, token, requestBody);
    Map<String, Object> parsed = parseBody(response.body());
    if (parsed.containsKey("ok") && Boolean.FALSE.equals(parsed.get("ok"))) {
      throw new ConnectorException(
          "Slack API returned an error: " + parsed.getOrDefault("error", "unknown"));
    }

    log.info(
        "Slack connector executed successfully for node: {}, method: {}, status: {}",
        node.getName(),
        method,
        response.statusCode());
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.statusCode());
    responseData.put("body", parsed);
    return Map.of(resultVariable, responseData);
  }

  private String buildRequestBody(
      String method, List<DataMapping> inputMappings, ProcessInstance instance) {
    Map<String, Object> payload = new HashMap<>();
    if (DEFAULT_METHOD.equals(method)) {
      String channel = evaluate("data.channel", inputMappings, instance);
      if (channel == null || channel.isBlank()) {
        throw new ConnectorException("Channel is required for Slack chat.postMessage");
      }
      payload.put("channel", channel);

      String messageType =
          getDataMappingValue("data.messageType", inputMappings)
              .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
              .map(Object::toString)
              .orElse("plainText");
      if ("messageBlock".equals(messageType)) {
        Object blocks =
            getDataMappingValue("data.blockContent", inputMappings)
                .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
                .orElseThrow(
                    () ->
                        new ConnectorException(
                            "Message block content is required for Slack message block"));
        payload.put("blocks", blocks);
      } else {
        String text = evaluate("data.text", inputMappings, instance);
        if (text == null || text.isBlank()) {
          throw new ConnectorException("Message text is required for Slack chat.postMessage");
        }
        payload.put("text", text);
      }
    } else {
      // Generic pass-through: collect every data.* input into the request payload.
      for (DataMapping mapping : inputMappings) {
        String name = mapping.getName();
        if (name != null && name.startsWith("data.") && !"data.messageType".equals(name)) {
          Object value =
              VariablesUtils.getEvaluatedVariable(mapping.getValue(), instance.getVariables());
          payload.put(name.substring("data.".length()), value);
        }
      }
    }
    return JsonMapper.writeToJson(payload);
  }

  private SlackResponse callSlack(String url, String token, String body) {
    try {
      var responseEntity =
          restClient
              .post()
              .uri(url)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
                    throw new RestClientException(
                        "Slack API error: " + response.getStatusCode() + " - " + errorBody);
                  })
              .toEntity(String.class);
      return new SlackResponse(responseEntity.getStatusCode().value(), responseEntity.getBody());
    } catch (RestClientException e) {
      throw new ConnectorException("Slack connector execution failed: " + e.getMessage(), e);
    }
  }

  private Map<String, Object> parseBody(String body) {
    if (body == null || body.isBlank()) {
      return new HashMap<>();
    }
    try {
      return JsonMapper.readFromJson(
          body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      Map<String, Object> raw = new HashMap<>();
      raw.put("raw", body);
      return raw;
    }
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private record SlackResponse(int statusCode, String body) {}
}
