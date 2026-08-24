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

/** WhatsApp connector handler. Sends messages through the WhatsApp Cloud API (Meta Graph API). */
@Slf4j
@Component
public class WhatsAppConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
  private static final String GRAPH_API_BASE_URL = "https://graph.facebook.com";
  private static final String DEFAULT_API_VERSION = "v18.0";
  private static final String DEFAULT_RESULT_VARIABLE = "whatsAppResponse";

  private final RestClient restClient;

  public WhatsAppConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.whatsapp:1";
  }

  @Override
  public String errorCode() {
    return "WHATSAPP_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String accessToken = evaluate("authentication.token", inputMappings, instance);
    if (accessToken == null) {
      accessToken = evaluate("token", inputMappings, instance);
    }
    String phoneNumberId = evaluate("senderPhoneId", inputMappings, instance);
    if (phoneNumberId == null) {
      phoneNumberId = evaluate("phoneNumberId", inputMappings, instance);
    }
    String to = evaluate("recipientPhoneNumber", inputMappings, instance);
    if (to == null) {
      to = evaluate("to", inputMappings, instance);
    }
    String messageBody = evaluate("messageBody", inputMappings, instance);
    if (messageBody == null) {
      messageBody = evaluate("text.body", inputMappings, instance);
    }
    String apiVersion =
        getDataMappingValue("apiVersion", inputMappings)
            .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
            .map(Object::toString)
            .orElse(DEFAULT_API_VERSION);

    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse(DEFAULT_RESULT_VARIABLE);

    if (accessToken == null || accessToken.isBlank()) {
      throw new ConnectorException("Access token is required for WhatsApp connector");
    }
    if (phoneNumberId == null || phoneNumberId.isBlank()) {
      throw new ConnectorException("phoneNumberId is required for WhatsApp connector");
    }
    if (to == null || to.isBlank()) {
      throw new ConnectorException("Recipient 'to' is required for WhatsApp connector");
    }
    if (messageBody == null || messageBody.isBlank()) {
      throw new ConnectorException("Message body is required for WhatsApp connector");
    }

    String apiUrl =
        String.format("%s/%s/%s/messages", GRAPH_API_BASE_URL, apiVersion, phoneNumberId);
    String requestBody = buildRequestBody(to, messageBody);

    WhatsAppResponse response = sendMessage(apiUrl, accessToken, requestBody);
    log.info(
        "WhatsApp connector executed successfully for node: {}, status: {}",
        node.getName(),
        response.statusCode());
    return Map.of(resultVariable, buildResponseData(response));
  }

  private String buildRequestBody(String to, String body) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("messaging_product", "whatsapp");
    payload.put("recipient_type", "individual");
    payload.put("to", to);
    payload.put("type", "text");
    Map<String, Object> text = new HashMap<>();
    text.put("body", body);
    payload.put("text", text);
    return JsonMapper.writeToJson(payload);
  }

  private WhatsAppResponse sendMessage(String url, String accessToken, String body) {
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
                        "WhatsApp API error response: status={}, body={}",
                        response.getStatusCode(),
                        errorBody);
                    throw new RestClientException(
                        "WhatsApp API error: " + response.getStatusCode() + " - " + errorBody);
                  })
              .toEntity(String.class);
      return new WhatsAppResponse(responseEntity.getStatusCode().value(), responseEntity.getBody());
    } catch (RestClientException e) {
      throw new ConnectorException("WhatsApp connector execution failed: " + e.getMessage(), e);
    }
  }

  private Map<String, Object> buildResponseData(WhatsAppResponse response) {
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.statusCode());
    responseData.put("body", response.body());
    try {
      if (response.body() != null && !response.body().isBlank()) {
        responseData.put("bodyJson", JsonMapper.readTree(response.body()));
      }
    } catch (Exception e) {
      log.debug("WhatsApp response body is not JSON, storing as string");
    }
    return responseData;
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private record WhatsAppResponse(int statusCode, String body) {}
}
