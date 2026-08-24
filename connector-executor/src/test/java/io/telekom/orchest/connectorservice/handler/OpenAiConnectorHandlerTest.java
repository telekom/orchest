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

/** Tests OpenAI connector chat completion and moderation API calls and input validation. */
@ExtendWith(MockitoExtension.class)
class OpenAiConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private OpenAiConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new OpenAiConnectorHandler();
    Field f = OpenAiConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "OpenAI Task");
  }

  @SuppressWarnings("unchecked")
  private void setupPostChain() {
    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
    when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.openai:1", handler.connectorType());
    assertEquals("OPENAI_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void chat_callsChatCompletionsEndpoint() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "sk-test"),
            new DataMapping("operation", "chat"),
            new DataMapping("model", "gpt-4o"),
            new DataMapping("prompt", "Hello")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"id\":\"cmpl-1\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://api.openai.com/v1/chat/completions");
    assertNotNull(output.get("openAiConnectorResponse"));
  }

  @Test
  void moderation_callsModerationsEndpoint() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.token", "sk-test"),
            new DataMapping("operation", "moderation"),
            new DataMapping("internal_textToAnalyze", "check this")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"results\":[]}"));

    handler.execute(instance, node);

    verify(requestBodyUriSpec).uri("https://api.openai.com/v1/moderations");
  }

  @Test
  void missingApiKey_throws() {
    node.setInputMappings(List.of(new DataMapping("prompt", "Hello")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingPrompt_throws() {
    node.setInputMappings(List.of(new DataMapping("authentication.token", "sk-test")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
