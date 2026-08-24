package io.telekom.orchest.orchestrest.extensions.pagevisit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for the page visit tracking feature. */
@Data
@Configuration
@ConfigurationProperties(prefix = "orchest.page-visits")
public class PageVisitProperties {
  private boolean enabled = false;
}
