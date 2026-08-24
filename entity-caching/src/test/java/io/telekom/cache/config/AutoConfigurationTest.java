package io.telekom.cache.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.telekom.orchest.cache.config.CacheAutoConfiguration;
import io.telekom.orchest.cache.config.CacheFactory;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.cache.core.CacheDefinition;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Tests for {@link CacheAutoConfiguration} verifying bean registration and YAML overrides. */
class AutoConfigurationTest {

  record Product(String sku, String name) {}

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class));

  @Test
  void registersCacheBeansFromDefinitions() {
    runner
        .withUserConfiguration(HostConfig.class)
        .run(
            ctx -> {
              assertThat(ctx).hasBean("productCache");
              Cache<?, ?> productCache = (Cache<?, ?>) ctx.getBean("productCache");
              Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> productCache.size() == 2);
            });
  }

  @Test
  void yamlCanDisableCache() {
    runner
        .withUserConfiguration(HostConfig.class)
        .withPropertyValues("orchest.caching.caches.productCache.enabled=false")
        .run(ctx -> assertThat(ctx.containsBean("productCache")).isFalse());
  }

  @Test
  void masterKillSwitchDisablesAutoConfig() {
    runner
        .withUserConfiguration(HostConfig.class)
        .withPropertyValues("orchest.caching.enabled=false")
        .run(ctx -> assertThat(ctx).doesNotHaveBean(CacheFactory.class));
  }

  @Configuration
  static class HostConfig {
    // Definition bean uses a distinct name; the resulting Cache bean is registered
    // under the cache name declared inside the definition ("productCache").
    @Bean
    CacheDefinition<String, Product, Product> productCacheDefinition() {
      return CacheDefinition.<String, Product, Product>builder()
          .name("productCache")
          .loader(() -> List.of(new Product("a", "Alpha"), new Product("b", "Beta")))
          .keyExtractor(Product::sku)
          .refreshInterval(Duration.ofHours(1))
          .loadOnStartup(true)
          .build();
    }
  }
}
