package io.telekom.orchest.client.configuration.msteams;

import feign.Feign;
import feign.okhttp.OkHttpClient;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.notification.CompositeIncidentNotifier;
import io.telekom.orchest.client.notification.IncidentNotifier;
import io.telekom.orchest.client.notification.TeamsIncidentNotifier;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for MS Teams incident alerting via webhook. */
@Configuration
@RequiredArgsConstructor
public class TeamsClientConfiguration {

  private final OrchestProperties orchestProperties;

  /**
   * Creates the Teams webhook Feign client, or a no-op stub when alerting is disabled.
   *
   * @return the Teams webhook client
   */
  @Bean
  public TeamsWebhookClient teamsClient() {
    OrchestProperties.IncidentAlert incidentAlert = orchestProperties.getIncidentAlert();
    if (incidentAlert.isEnabled()) {
      return Feign.builder()
          .client(new OkHttpClient())
          .target(TeamsWebhookClient.class, incidentAlert.getIncidentAlertChannel());
    }
    return payload -> "DISABLED";
  }

  /**
   * Creates the composite incident notifier wrapping the Teams notifier.
   *
   * @param teamsWebhookClient the Teams webhook client
   * @return the incident notifier
   */
  @Bean
  public IncidentNotifier incidentNotifier(TeamsWebhookClient teamsWebhookClient) {
    TeamsIncidentNotifier teamsNotifier =
        new TeamsIncidentNotifier(teamsWebhookClient, orchestProperties);
    return new CompositeIncidentNotifier(List.of(teamsNotifier));
  }
}
