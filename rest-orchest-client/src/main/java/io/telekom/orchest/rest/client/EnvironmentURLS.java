package io.telekom.orchest.rest.client;

import lombok.Getter;

/** Enum of OrchesT API base URLs for each deployment environment. */
@Getter
public enum EnvironmentURLS {
  LOCAL("http://localhost:6200/orchest"),
  DEV("https://api-orchest.your-domain.example.com/orchest"),
  TESTSTABLE("https://api-orchest.your-domain.example.com/orchest"),
  UAT("https://api-orchest.your-domain.example.com/orchest"),
  PROD_REF("https://api-orchest.your-domain.example.com/orchest"),
  PROD("https://api-orchest.your-domain.example.com/orchest");

  private final String url;

  EnvironmentURLS(String url) {
    this.url = url;
  }

  /**
   * Resolves the base URL for the given environment name.
   *
   * @param environment environment identifier (e.g. "local", "dev", "prod")
   * @return the corresponding base URL; defaults to DEV if unrecognized
   */
  public static String getUrl(String environment) {
    return switch (environment) {
      case "local" -> LOCAL.getUrl();
      case "teststable" -> TESTSTABLE.getUrl();
      case "uat" -> UAT.getUrl();
      case "prod-ref" -> PROD_REF.getUrl();
      case "prod" -> PROD.getUrl();
      default -> DEV.getUrl();
    };
  }
}
