package io.telekom.orchest.ai.config;

import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Azure OpenAI client, chat model, and chat client beans. Activated only
 * when {@code orchest.ai.enabled=true}.
 */
@EnableConfigurationProperties(OrchestAiProperties.class)
@Configuration
@ConditionalOnProperty(
    prefix = "orchest.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class AzureOpenAiConfig {

  /**
   * Creates the Azure OpenAI HTTP client configured with endpoint, API key, and service version.
   *
   * @param properties the OrchesT AI configuration properties
   * @return configured {@link OpenAIClient} instance
   */
  @Bean
  public OpenAIClient openAIClient(OrchestAiProperties properties) {
    return OpenAIOkHttpClient.builder()
        .baseUrl(properties.getEndPoint())
        .apiKey(properties.getApiKey())
        .azureServiceVersion(AzureOpenAIServiceVersion.fromString(properties.getApiVersion()))
        .build();
  }

  /**
   * Creates the Spring AI chat model wrapping the Azure OpenAI client with deployment-specific
   * options.
   *
   * @param openAIClient the underlying OpenAI HTTP client
   * @param properties the OrchesT AI configuration properties
   * @return configured {@link OpenAiChatModel} instance
   */
  @Bean
  public OpenAiChatModel openAiChatModel(
      OpenAIClient openAIClient, OrchestAiProperties properties) {
    OpenAiChatOptions options =
        OpenAiChatOptions.builder()
            .model(properties.getModel())
            .apiKey(properties.getApiKey())
            .baseUrl(properties.getEndPoint())
            .deploymentName(properties.getDeploymentName())
            .temperature(0.7)
            .build();

    return OpenAiChatModel.builder().openAiClient(openAIClient).options(options).build();
  }

  /**
   * Creates the Spring AI chat client used for conversational interactions.
   *
   * @param openAiChatModel the chat model to back the client
   * @return configured {@link ChatClient} instance
   */
  @Bean
  public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
    return ChatClient.builder(openAiChatModel).build();
  }
}
