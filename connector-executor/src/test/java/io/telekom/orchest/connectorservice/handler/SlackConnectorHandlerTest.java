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

/** Tests Slack connector message posting, error response handling, and input validation. */
@ExtendWith(MockitoExtension.class)
class SlackConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private SlackConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new SlackConnectorHandler();
    Field f = SlackConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Slack Task");
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
    assertEquals("io.orchest.slack:1", handler.connectorType());
    assertEquals("SLACK_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void postMessage_returnsParsedBody() {
    node.setInputMappings(
        List.of(
            new DataMapping("token", "xoxb-token"),
            new DataMapping("method", "chat.postMessage"),
            new DataMapping("data.messageType", "plainText"),
            new DataMapping("data.channel", "C123"),
            new DataMapping("data.text", "hello")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class))
        .thenReturn(ResponseEntity.ok("{\"ok\":true,\"ts\":\"1.2\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://slack.com/api/chat.postMessage");
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("slackResponse");
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) responseData.get("body");
    assertEquals(Boolean.TRUE, body.get("ok"));
  }

  @Test
  void postMessage_throwsWhenSlackReturnsNotOk() {
    node.setInputMappings(
        List.of(
            new DataMapping("token", "xoxb-token"),
            new DataMapping("data.channel", "C123"),
            new DataMapping("data.text", "hello")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class))
        .thenReturn(ResponseEntity.ok("{\"ok\":false,\"error\":\"channel_not_found\"}"));

    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("channel_not_found"));
  }

  @Test
  void missingToken_throws() {
    node.setInputMappings(List.of(new DataMapping("data.channel", "C123")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
