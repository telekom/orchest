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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * SOAP connector handler. Constructs a SOAP envelope (1.1 or 1.2) from input mappings and posts it
 * to the configured service URL with optional WS-Security (Basic Auth) and retry logic.
 */
@Slf4j
@Component
public class SoapConnectorHandler implements ConnectorHandler {

  private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_READ_TIMEOUT_SECONDS = 60;
  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final long DEFAULT_RETRY_DELAY_MS = 1000;

  private final RestClient restClient;

  public SoapConnectorHandler() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS));
    requestFactory.setReadTimeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS));
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public String connectorType() {
    return "io.orchest.soap:1";
  }

  @Override
  public String errorCode() {
    return "SOAP_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String serviceUrl = evaluate("serviceUrl", inputMappings, instance);
    if (serviceUrl == null || serviceUrl.isBlank()) {
      throw new ConnectorException("Service URL is required for SOAP connector");
    }

    String soapVersion = evaluate("soapVersion.version", inputMappings, instance);
    if (soapVersion == null || soapVersion.isBlank()) {
      soapVersion = "1.1";
    }

    String soapAction = evaluate("soapVersion.soapAction", inputMappings, instance);

    String authType = evaluate("authentication.authentication", inputMappings, instance);
    String username = evaluate("authentication.username", inputMappings, instance);
    String password = evaluate("authentication.password", inputMappings, instance);

    String bodyType = evaluate("body.type", inputMappings, instance);
    String bodyTemplate = evaluate("body.template", inputMappings, instance);
    String bodyContext = evaluate("body.context", inputMappings, instance);
    String bodyJson = evaluate("body.json", inputMappings, instance);

    String headerType = evaluate("header.type", inputMappings, instance);
    String headerTemplate = evaluate("header.template", inputMappings, instance);
    String headerContext = evaluate("header.context", inputMappings, instance);
    String headerJson = evaluate("header.json", inputMappings, instance);

    String namespaces = evaluate("namespaces", inputMappings, instance);

    String timeoutStr = evaluate("connectionTimeoutInSeconds", inputMappings, instance);

    String resultVariable =
        getDataMappingValue("resultVariable", outputMappings).orElse("soapResponse");
    String retryBackOffStr = getDataMappingValue("retryBackoff", outputMappings).orElse(null);

    String bodyContent = resolveBodyContent(bodyType, bodyTemplate, bodyContext, bodyJson);
    if (bodyContent == null || bodyContent.isBlank()) {
      throw new ConnectorException("SOAP body content is required");
    }

    String headerContent =
        resolveHeaderContent(headerType, headerTemplate, headerContext, headerJson);
    String envelope = buildEnvelope(soapVersion, namespaces, headerContent, bodyContent);

    int maxRetries = parseRetryConfig(retryBackOffStr);

    SoapResponse response =
        executeWithRetry(
            serviceUrl,
            soapVersion,
            soapAction,
            envelope,
            authType,
            username,
            password,
            maxRetries);

    log.info(
        "SOAP connector executed successfully for node: {}, status: {}",
        node.getName(),
        response.statusCode());

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("statusCode", response.statusCode());
    responseData.put("envelope", response.body());
    return Map.of(resultVariable, responseData);
  }

  private String resolveBodyContent(String bodyType, String template, String context, String json) {
    if ("template".equalsIgnoreCase(bodyType)) {
      if (template == null || template.isBlank()) {
        throw new ConnectorException("SOAP body template is required when body type is 'template'");
      }
      return template;
    }
    if (json != null && !json.isBlank()) {
      return json;
    }
    return template;
  }

  private String resolveHeaderContent(
      String headerType, String template, String context, String json) {
    if ("none".equalsIgnoreCase(headerType) || headerType == null) {
      return null;
    }
    if ("template".equalsIgnoreCase(headerType)) {
      return template;
    }
    if ("json".equalsIgnoreCase(headerType)) {
      return json;
    }
    return null;
  }

  private String buildEnvelope(
      String soapVersion, String namespaces, String headerContent, String bodyContent) {
    String nsUri =
        "1.2".equals(soapVersion)
            ? "http://www.w3.org/2003/05/soap-envelope"
            : "http://schemas.xmlsoap.org/soap/envelope/";

    StringBuilder sb = new StringBuilder();
    sb.append("<soapenv:Envelope xmlns:soapenv=\"").append(nsUri).append("\"");
    if (namespaces != null && !namespaces.isBlank()) {
      sb.append(" ").append(namespaces.trim());
    }
    sb.append(">");

    if (headerContent != null && !headerContent.isBlank()) {
      sb.append("<soapenv:Header>").append(headerContent).append("</soapenv:Header>");
    }

    sb.append("<soapenv:Body>").append(bodyContent).append("</soapenv:Body>");
    sb.append("</soapenv:Envelope>");
    return sb.toString();
  }

  private SoapResponse executeWithRetry(
      String url,
      String soapVersion,
      String soapAction,
      String envelope,
      String authType,
      String username,
      String password,
      int maxRetries) {
    RestClientException lastException = null;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        return executeSoapRequest(
            url, soapVersion, soapAction, envelope, authType, username, password);
      } catch (RestClientException e) {
        lastException = e;
        log.warn("SOAP request attempt {} failed: {}", attempt, e.getMessage());

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
        "SOAP request failed after " + maxRetries + " attempts", lastException);
  }

  private SoapResponse executeSoapRequest(
      String url,
      String soapVersion,
      String soapAction,
      String envelope,
      String authType,
      String username,
      String password) {
    MediaType contentType =
        "1.2".equals(soapVersion)
            ? MediaType.parseMediaType("application/soap+xml; charset=utf-8")
            : MediaType.TEXT_XML;

    try {
      var responseEntity =
          restClient
              .post()
              .uri(url)
              .contentType(contentType)
              .headers(
                  headers -> {
                    if ("1.1".equals(soapVersion) && soapAction != null && !soapAction.isBlank()) {
                      headers.set("SOAPAction", soapAction);
                    }
                    if ("usernameToken".equalsIgnoreCase(authType)
                        && username != null
                        && password != null) {
                      String auth = username + ":" + password;
                      String encodedAuth =
                          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                      headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                    }
                  })
              .body(envelope)
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
                        "SOAP error: " + response.getStatusCode() + " - " + errorBody);
                  })
              .toEntity(String.class);

      return new SoapResponse(responseEntity.getStatusCode().value(), responseEntity.getBody());
    } catch (RestClientException e) {
      throw e;
    } catch (Exception e) {
      throw new RestClientException("Failed to execute SOAP request: " + e.getMessage(), e);
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

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }

  private record SoapResponse(int statusCode, String body) {}
}
