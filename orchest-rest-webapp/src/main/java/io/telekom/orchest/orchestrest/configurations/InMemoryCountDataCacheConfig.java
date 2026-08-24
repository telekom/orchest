package io.telekom.orchest.orchestrest.configurations;

import io.telekom.orchest.cache.core.CacheDefinition;
import io.telekom.orchest.cache.core.CacheHooks;
import io.telekom.orchest.orchestrest.api.dto.StatsDTO;
import io.telekom.orchest.orchestrest.service.StatisticsAPIService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the in-memory statistics cache that periodically refreshes instance counts. */
@Slf4j
@Configuration
public class InMemoryCountDataCacheConfig {

  @Bean
  public StatsDTO statsDTO() {
    return new StatsDTO();
  }

  @Bean
  public CacheDefinition<String, StatsDTO, StatsDTO> statsCache(
      StatisticsAPIService statisticsAPIService, StatsDTO statsDTO) {
    return CacheDefinition.<String, StatsDTO, StatsDTO>builder()
        .name("statsDTOCache")
        .loader(() -> List.of(statisticsAPIService.reloadStats()))
        .keyExtractor(stats -> stats.getLastUpdatedAt().toString())
        .refreshInterval(Duration.ofSeconds(60))
        .loadOnStartup(false)
        .failSafe(true)
        .retryAttempts(2)
        .retryBackoff(Duration.ofSeconds(30))
        .hooks(
            new CacheHooks<>() {
              @Override
              public void afterRefresh(
                  String cacheName, Map<String, StatsDTO> snapshot, long durationMillis) {
                snapshot.values().forEach(statsDTO::copy);
                log.debug("Stats cache has been refreshed");
              }
            })
        .build();
  }
}
