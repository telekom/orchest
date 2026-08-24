package io.telekom.orchest.orchestrest.configurations;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.PathResourceResolver;
import org.webjars.WebJarVersionLocator;

/** Configures Swagger UI static resource serving and the /swagger-ui.html redirect for WebFlux. */
@Configuration
public class SwaggerUiConfiguration implements WebFluxConfigurer {

  @Value("${spring.webflux.base-path:}")
  private String basePath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String swaggerUiVersion = new WebJarVersionLocator().version("swagger-ui");
    String resourceLocation =
        "classpath:/META-INF/resources/webjars/swagger-ui/" + swaggerUiVersion + "/";

    registry
        .addResourceHandler("/swagger-ui/**")
        .addResourceLocations(resourceLocation)
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  public RouterFunction<ServerResponse> swaggerUiRedirect() {
    String swaggerHtmlPath = basePath + "/swagger-ui.html";
    String redirectTarget =
        basePath + "/swagger-ui/index.html?configUrl=" + basePath + "/v3/api-docs/swagger-config";

    return RouterFunctions.route(
        RequestPredicates.GET(swaggerHtmlPath),
        request -> ServerResponse.temporaryRedirect(URI.create(redirectTarget)).build());
  }
}
