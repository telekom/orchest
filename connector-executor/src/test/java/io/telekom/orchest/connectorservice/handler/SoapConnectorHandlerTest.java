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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/** Tests SOAP connector envelope construction, header inclusion, and input validation. */
@ExtendWith(MockitoExtension.class)
class SoapConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private SoapConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new SoapConnectorHandler();
    Field f = SoapConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "SOAP Task");
  }

  @SuppressWarnings("unchecked")
  private void setupPostChain() {
    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.soap:1", handler.connectorType());
    assertEquals("SOAP_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void execute_buildsEnvelopeFromTemplateBody() {
    node.setInputMappings(
        List.of(
            new DataMapping("serviceUrl", "http://example.com/ws"),
            new DataMapping("soapVersion.version", "1.1"),
            new DataMapping("soapVersion.soapAction", "urn:test"),
            new DataMapping("body.type", "template"),
            new DataMapping("body.template", "<GetData xmlns=\"urn:test\"/>")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("<soapenv:Envelope/>"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("http://example.com/ws");
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("soapResponse");
    assertNotNull(responseData);
    assertEquals(200, responseData.get("statusCode"));
  }

  @Test
  void execute_buildsEnvelopeFromJsonBody() {
    node.setInputMappings(
        List.of(
            new DataMapping("serviceUrl", "http://example.com/ws"),
            new DataMapping("soapVersion.version", "1.2"),
            new DataMapping("body.type", "json"),
            new DataMapping("body.json", "<GetData/>")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("<response/>"));

    handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("http://example.com/ws");
  }

  @Test
  void execute_includesHeaderWhenProvided() {
    node.setInputMappings(
        List.of(
            new DataMapping("serviceUrl", "http://example.com/ws"),
            new DataMapping("body.type", "template"),
            new DataMapping("body.template", "<Body/>"),
            new DataMapping("header.type", "template"),
            new DataMapping("header.template", "<Auth>token</Auth>")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("<response/>"));

    handler.execute(instance, node);

    verify(requestBodySpec)
        .body(
            (String)
                argThat(
                    arg ->
                        arg instanceof String s
                            && s.contains("<soapenv:Header><Auth>token</Auth></soapenv:Header>")
                            && s.contains("<soapenv:Body><Body/></soapenv:Body>")));
  }

  @Test
  void missingServiceUrl_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("body.type", "template"), new DataMapping("body.template", "<Test/>")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("Service URL is required"));
  }

  @Test
  void missingBody_throws() {
    node.setInputMappings(List.of(new DataMapping("serviceUrl", "http://example.com/ws")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("body content is required"));
  }
}
