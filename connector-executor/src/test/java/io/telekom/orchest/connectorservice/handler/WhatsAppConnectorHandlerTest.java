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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/** Tests WhatsApp connector message sending via the Cloud API and input validation. */
@ExtendWith(MockitoExtension.class)
class WhatsAppConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private WhatsAppConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new WhatsAppConnectorHandler();
    Field f = WhatsAppConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "WhatsApp Task");
  }

  @SuppressWarnings("unchecked")
  private void setupPostChain() {
    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.whatsapp:1", handler.connectorType());
    assertEquals("WHATSAPP_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void sendMessage_usingJsonBindingNames() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("senderPhoneId", "12345"),
            new DataMapping("recipientPhoneNumber", "+491234567890"),
            new DataMapping("messageBody", "Hello from WhatsApp")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class))
        .thenReturn(ResponseEntity.ok("{\"messages\":[{\"id\":\"wamid.1\"}]}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://graph.facebook.com/v18.0/12345/messages");
    verify(requestBodySpec).header("Authorization", "Bearer test-token");
    assertNotNull(output.get("whatsAppResponse"));
  }

  @Test
  void sendMessage_fallsBackToLegacyPropertyNames() {
    node.setInputMappings(
        List.of(
            new DataMapping("token", "test-token"),
            new DataMapping("phoneNumberId", "12345"),
            new DataMapping("to", "+491234567890"),
            new DataMapping("text.body", "Hello legacy")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class))
        .thenReturn(ResponseEntity.ok("{\"messages\":[{\"id\":\"wamid.2\"}]}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://graph.facebook.com/v18.0/12345/messages");
    assertNotNull(output.get("whatsAppResponse"));
  }

  @Test
  void missingToken_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("senderPhoneId", "12345"),
            new DataMapping("recipientPhoneNumber", "+491234567890"),
            new DataMapping("messageBody", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingPhoneNumberId_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("recipientPhoneNumber", "+491234567890"),
            new DataMapping("messageBody", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingRecipient_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("senderPhoneId", "12345"),
            new DataMapping("messageBody", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingBody_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("senderPhoneId", "12345"),
            new DataMapping("recipientPhoneNumber", "+491234567890")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
