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

/** Tests webhook connector outbound HTTP calls and URL validation. */
@ExtendWith(MockitoExtension.class)
class WebhookConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private WebhookConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new WebhookConnectorHandler();
    Field f = WebhookConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Webhook Task");
  }

  @SuppressWarnings("unchecked")
  private void setupMethodChain() {
    when(restClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.webhook:1", handler.connectorType());
    assertEquals("WEBHOOK_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void postsBodyToWebhookUrl() {
    node.setInputMappings(
        List.of(
            new DataMapping("url", "https://hooks.example.com/abc"),
            new DataMapping("method", "POST"),
            new DataMapping("body", "{\"a\":1}")));
    node.setOutputMappings(new ArrayList<>());

    setupMethodChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"received\":true}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(restClient).method(HttpMethod.POST);
    verify(requestBodyUriSpec).uri("https://hooks.example.com/abc");
    verify(requestBodySpec).body("{\"a\":1}");
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("webhookResponse");
    assertEquals(200, responseData.get("statusCode"));
  }

  @Test
  void missingUrl_throws() {
    node.setInputMappings(List.of(new DataMapping("method", "POST")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
