package io.telekom.orchest.orchestrest.extensions.sensitivevariables.config;

import io.telekom.orchest.cache.core.CacheDefinition;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.repository.ProcessSensitiveVariablesRepository;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the in-memory cache for process sensitive variable definitions. */
@Configuration
public class ProcessSensitiveVariableConfig {

  @Bean
  public CacheDefinition<String, ProcessSensitiveVariables, ProcessSensitiveVariables>
      processSensitiveVariablesCacheDef(ProcessSensitiveVariablesRepository repository) {
    return CacheDefinition.<String, ProcessSensitiveVariables, ProcessSensitiveVariables>builder()
        .name("processSensitiveVariablesCache")
        .loader(repository::findAll)
        .keyExtractor(ProcessSensitiveVariables::getProcessDefinitionId)
        .refreshInterval(Duration.ofSeconds(30))
        .loadOnStartup(true)
        .failSafe(true)
        .retryAttempts(2)
        .retryBackoff(Duration.ofSeconds(1))
        .build();
  }
}
