package io.telekom.orchest.orchestrest.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation. Defines API metadata, contact
 * information, and server URLs.
 */
@Configuration
public class OpenAPIConfig {

  @Value("${server.port:8080}")
  private String port;

  /**
   * Creates the OpenAPI definition for the application.
   *
   * @return The OpenAPI object.
   */
  @Bean
  public OpenAPI orderSplitterOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("OrchesT")
                .description("REST interface for interacting with Orchest Engine")
                .version("1.0.0")
                .contact(
                    new Contact().name("DOT Team Tyrell").email("teamtyrell@telekom-digital.com"))
                .license(new License().name("Proprietary license"))
                .extensions(Map.of("x-api-category", "other")))
        .servers(
            List.of(
                new Server().url("http://localhost:" + port + "/orchest/"),
                new Server().url("https://api-orchest.your-domain.example.com/orchest/"),
                new Server().url("https://api-orchest.your-domain.example.com/orchest/")));
  }
}
