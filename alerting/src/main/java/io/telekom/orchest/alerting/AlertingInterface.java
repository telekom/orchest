package io.telekom.orchest.alerting;

import io.telekom.orchest.alerting.api.dto.AlertDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Service for managing and dispatching alerts to configured alerting services. Acts as a facade to
 * multiple {@link IAlertingService} implementations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertingInterface {

  private final ModuleProperties moduleProperties;
  private final List<? extends IAlertingService> alertingServices;

  /**
   * Sends an alert to all active alerting services. Checks if alerting is enabled globally and if
   * the alert content is valid. Can send alerts synchronously or asynchronously based on the {@link
   * AlertDTO} flag.
   *
   * @param alertDTO The alert data.
   */
  public void sendAlert(AlertDTO alertDTO) {
    if (!moduleProperties.isEnabled()
        || alertDTO == null
        || StringUtils.isEmpty(alertDTO.getMessageBody())) {
      return;
    }
    boolean sendAsync = alertDTO.isSendAsync();
    log.info("send alert with async enabled : {}", sendAsync);
    alertingServices.stream()
        .filter(IAlertingService::isActive)
        .forEach(
            alertingService -> {
              if (sendAsync) {
                alertingService.sendAlertAsync(alertDTO);
              } else {
                alertingService.sendAlert(alertDTO);
              }
            });
  }
}
