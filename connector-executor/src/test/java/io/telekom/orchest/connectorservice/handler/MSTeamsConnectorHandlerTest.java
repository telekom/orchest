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

/** Tests Microsoft Teams connector message sending, OAuth token handling, and input validation. */
@ExtendWith(MockitoExtension.class)
class MSTeamsConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private MSTeamsConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new MSTeamsConnectorHandler();
    Field f = MSTeamsConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Teams Task");
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
    assertEquals("io.orchest.connector-microsoft-teams:1", handler.connectorType());
    assertEquals("MS_TEAMS_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void sendMessage_withBearerToken() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.type", "token"),
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("data.type", "chat"),
            new DataMapping("data.method", "sendMessageToChat"),
            new DataMapping("data.chatId", "chat-123"),
            new DataMapping("data.content", "Hello Teams")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"id\":\"msg-1\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://graph.microsoft.com/v1.0/chats/chat-123/messages");
    verify(requestBodySpec).header("Authorization", "Bearer test-token");
    assertNotNull(output.get("teamsResponse"));
  }

  @Test
  void missingChatId_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("data.content", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingContent_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "test-token"),
            new DataMapping("data.chatId", "chat-123")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingToken_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("data.chatId", "chat-123"), new DataMapping("data.content", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
