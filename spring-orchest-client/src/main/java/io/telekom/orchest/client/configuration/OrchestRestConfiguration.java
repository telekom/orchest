package io.telekom.orchest.client.configuration;

import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.rest.client.EnvironmentURLS;
import io.telekom.orchest.rest.client.OrchesTRestClient;
import io.telekom.orchest.rest.client.TokenAuthInjector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for the OrchesT REST client bean, resolving the base URL and authentication. */
@Configuration
public class OrchestRestConfiguration {

  @Value("${spring.profiles.active:dev}")
  private String activeProfileName;

  /**
   * Creates the OrchesT REST client configured with the active profile URL and optional API key.
   *
   * @param orchestProperties the OrchesT configuration properties
   * @return the configured REST client
   */
  @Bean
  public OrchesTRestClient orchesTRestClient(OrchestProperties orchestProperties) {
    OrchestProperties.Rest rest = orchestProperties.getRest();
    String url = EnvironmentURLS.getUrl(activeProfileName);
    if (rest == null) {
      return OrchesTRestClient.create(url);
    } else {
      String effectiveURL = rest.getUrl() != null ? rest.getUrl() : url;
      return OrchesTRestClient.create(
          effectiveURL, TokenAuthInjector.header("X-API-Key", rest::getApiKey));
    }
  }
}
