package io.telekom.orchest.orchestrest.configurations;

import de.telekom.solutions.kmsclient.EnableKMS;
import org.springframework.context.annotation.Configuration;

/**
 * General application configuration. @EnableSpringDataWebSupport is servlet-MVC specific and is
 * intentionally omitted in this WebFlux build — Spring Data reactive pagination works without it.
 * Page serialization via DTO is configured in Jackson directly.
 */
@Configuration
@EnableKMS
public class AppConfigurations {}
