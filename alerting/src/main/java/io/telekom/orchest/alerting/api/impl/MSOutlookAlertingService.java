package io.telekom.orchest.alerting.api.impl;

import io.telekom.orchest.alerting.IAlertingService;
import io.telekom.orchest.alerting.ModuleProperties;
import io.telekom.orchest.alerting.api.dto.AlertDTO;
import io.telekom.orchest.alerting.service.IncidentAlertEmailRenderer;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Alerting service implementation for sending email alerts via MS Outlook/SMTP. Enabled only when
 * 'alerting.enabled' property is true.
 *
 * <p>Sends multipart mail with plain-text + HTML. The HTML body embeds the OrchesT logo as a base64
 * data URI (see {@link IncidentAlertEmailRenderer}), so the image is visible in the rendered
 * template and in clients that support data-URI images.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "alerting.enabled", havingValue = "true")
public class MSOutlookAlertingService implements IAlertingService {

  private final JavaMailSender alertingJavaMailSender;
  private final ModuleProperties msOutlookAlertingConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfiles;

  @Override
  public boolean sendAlert(AlertDTO alert) {
    return sendMail(alert);
  }

  @Async
  @Override
  public void sendAlertAsync(AlertDTO alert) {
    sendAlert(alert);
  }

  @Override
  public boolean isActive() {
    return msOutlookAlertingConfig != null
        && msOutlookAlertingConfig.getConfigs().get(ModuleProperties.AlertType.EMAIL).isEnabled();
  }

  private boolean sendMail(AlertDTO alert) {
    try {
      alertingJavaMailSender.send(createMimeMessage(alert));
      return true;
    } catch (Exception e) {
      log.info("Incident alert mailer failed to send alert:", e);
    }
    return false;
  }

  private MimeMessage createMimeMessage(AlertDTO alertDTO) throws MessagingException {
    MimeMessage mimeMessage = alertingJavaMailSender.createMimeMessage();
    MimeMessageHelper helper =
        new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

    applyRecipients(helper, alertDTO.getRecipients());
    helper.setFrom(alertDTO.getFromAddress());

    String subject =
        StringUtils.hasText(alertDTO.getSubject())
            ? activeProfiles.toUpperCase() + " :: " + alertDTO.getSubject()
            : activeProfiles.toUpperCase() + " :: OrchesT Incident Alerts";
    helper.setSubject(subject);

    String plainText = IncidentAlertEmailRenderer.renderPlainText(alertDTO, activeProfiles);
    String htmlContent = IncidentAlertEmailRenderer.renderHtml(alertDTO, activeProfiles);
    System.out.println(htmlContent);
    helper.setText(plainText, htmlContent);

    return mimeMessage;
  }

  private static void applyRecipients(MimeMessageHelper helper, AlertRecipients recipients)
      throws MessagingException {
    helper.setTo(toArray(recipients.getTo()));
    if (!CollectionUtils.isEmpty(recipients.getCc())) {
      helper.setCc(toArray(recipients.getCc()));
    }
    if (!CollectionUtils.isEmpty(recipients.getBcc())) {
      helper.setBcc(toArray(recipients.getBcc()));
    }
  }

  private static String[] toArray(List<String> emails) {
    return emails.toArray(String[]::new);
  }
}
