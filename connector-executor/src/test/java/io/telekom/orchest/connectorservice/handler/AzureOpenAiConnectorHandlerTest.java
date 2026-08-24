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

/** Tests Azure OpenAI connector execution including URL construction, API calls, and validation. */
@ExtendWith(MockitoExtension.class)
class AzureOpenAiConnectorHandlerTest {

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RestClient.RequestBodySpec requestBodySpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  private AzureOpenAiConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() throws Exception {
    handler = new AzureOpenAiConnectorHandler();
    Field f = AzureOpenAiConnectorHandler.class.getDeclaredField("restClient");
    f.setAccessible(true);
    f.set(handler, restClient);
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Azure OpenAI Task");
  }

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
    assertEquals("io.orchest.azure-openai:1", handler.connectorType());
    assertEquals("AZURE_OPENAI_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void chatCompletion_buildsUrlFromParts() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.value", "abc123"),
            new DataMapping("operation", "chatCompletion"),
            new DataMapping("chatCompletion_resourceName", "dt-poc-we"),
            new DataMapping("chatCompletion_deploymentId", "gpt-4o"),
            new DataMapping("chatCompletion_apiVersion", "2025-01-01-preview"),
            new DataMapping("chatCompletion_messageRole", "user"),
            new DataMapping("chatCompletion_messageContent", "Hello")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"id\":\"cmpl-1\"}"));

    Map<String, Object> output = handler.execute(instance, node);

    verify(requestBodyUriSpec)
        .uri(
            "https://dt-poc-we.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2025-01-01-preview");
    verify(requestBodySpec).header("api-key", "abc123");
    assertNotNull(output.get("azureOpenAiConnectorResponse"));
  }

  @Test
  void chatCompletion_usesPreBuiltUrlAndBody() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.value", "abc123"),
            new DataMapping("operation", "chatCompletion"),
            new DataMapping(
                "url",
                "https://my-resource.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-01"),
            new DataMapping("body", "{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"choices\":[]}"));

    handler.execute(instance, node);

    verify(requestBodyUriSpec)
        .uri(
            "https://my-resource.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-01");
    verify(requestBodySpec).body("{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}");
  }

  @Test
  void completion_buildsUrlForCompletionsEndpoint() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.value", "key123"),
            new DataMapping("operation", "completion"),
            new DataMapping("completion_resourceName", "my-resource"),
            new DataMapping("completion_deploymentId", "davinci"),
            new DataMapping("completion_apiVersion", "2024-02-01"),
            new DataMapping("completion_prompt", "Once upon a time")));
    node.setOutputMappings(new ArrayList<>());

    setupPostChain();
    when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("{\"choices\":[]}"));

    handler.execute(instance, node);

    verify(requestBodyUriSpec)
        .uri(
            "https://my-resource.openai.azure.com/openai/deployments/davinci/completions?api-version=2024-02-01");
  }

  @Test
  void missingApiKey_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("operation", "chatCompletion"),
            new DataMapping("chatCompletion_resourceName", "res"),
            new DataMapping("chatCompletion_deploymentId", "gpt-4o"),
            new DataMapping("chatCompletion_messageContent", "Hi")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }

  @Test
  void missingUrlAndResourceName_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("authentication.value", "key"),
            new DataMapping("operation", "chatCompletion"),
            new DataMapping("chatCompletion_messageContent", "Hi")));
    node.setOutputMappings(new ArrayList<>());
    assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
  }
}
