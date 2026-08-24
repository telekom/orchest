package io.telekom.orchest.connectorservice.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Tests REST/HTTP-JSON connector execution including GET/POST requests, retries, and validation.
 */
@ExtendWith(MockitoExtension.class)
class RestConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private RestConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new RestConnectorHandler();
    Field restClientField = RestConnectorHandler.class.getDeclaredField("restClient");
    restClientField.setAccessible(true);
    restClientField.set(handler, restClient);

    instance = new ProcessInstance("pi-123", "pd-456", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Test REST Task");
  }

  @SuppressWarnings("unchecked")
  private void setupRestClientMethodChain() {
    when(restClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void connectorTypeAndErrorCode() {
    assertEquals("io.orchest.http-json:1", handler.connectorType());
    assertEquals("REST_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void getRequest_returnsResponseInDefaultVariable() {
    node.setInputMappings(
        List.of(
            new DataMapping("url", "http://api.example.com/data"),
            new DataMapping("method", "GET")));
    node.setOutputMappings(new ArrayList<>());

    setupRestClientMethodChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"status\":\"ok\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(restClient).method(HttpMethod.GET);
    verify(requestBodyUriSpec).uri("http://api.example.com/data");
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("restResponse");
    assertNotNull(responseData);
    assertEquals(200, responseData.get("statusCode"));
    assertEquals("{\"status\":\"ok\"}", responseData.get("body"));
  }

  @Test
  void postRequest_includesBodyAndCustomResultVariable() {
    node.setInputMappings(
        List.of(
            new DataMapping("url", "http://api.example.com/create"),
            new DataMapping("method", "POST"),
            new DataMapping("body", "{\"name\":\"test\"}")));
    node.setOutputMappings(List.of(new DataMapping("resultVariable", "myResult")));

    setupRestClientMethodChain();
    when(responseSpec.toEntity(String.class))
        .thenReturn(ResponseEntity.status(201).body("{\"id\":\"123\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(restClient).method(HttpMethod.POST);
    verify(requestBodySpec).body("{\"name\":\"test\"}");
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("myResult");
    assertEquals(201, responseData.get("statusCode"));
  }

  @Test
  void retriesOnServerErrorThenSucceeds() {
    node.setInputMappings(
        List.of(
            new DataMapping("url", "http://api.example.com/flaky"),
            new DataMapping("method", "GET")));
    node.setOutputMappings(List.of(new DataMapping("retryBackOff", "2")));

    setupRestClientMethodChain();
    when(responseSpec.toEntity(String.class))
        .thenThrow(new RestClientException("Server error: 500"))
        .thenReturn(ResponseEntity.ok("recovered"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(responseSpec, times(2)).toEntity(String.class);
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("restResponse");
    assertEquals("recovered", responseData.get("body"));
  }

  @Test
  void missingUrl_throwsConnectorException() {
    node.setInputMappings(List.of(new DataMapping("method", "GET")));
    node.setOutputMappings(new ArrayList<>());

    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
