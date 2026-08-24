package io.telekom.orchest.alerting.service;

import io.telekom.orchest.alerting.api.dto.AlertDTO;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Renders the cross-client HTML incident-alert email and a matching plain-text fallback. Uses table
 * layout + inline CSS so Outlook, Gmail, Apple Mail, and similar clients render reliably.
 */
public final class IncidentAlertEmailRenderer {

  private static final String TEMPLATE_PATH = "mail/incident-alert.html";
  private static final String LOGO_PATH = "mail/orchest-logo.png";
  private static final DateTimeFormatter TIMESTAMP_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);
  private static final String DEFAULT_CONTACT =
      "For any issues please contact Team Tyrell tyrell@mg.telekom.de";

  private static final String HTML_TEMPLATE = loadTemplate();
  private static final String LOGO_DATA_URI = loadLogoDataUri();

  private IncidentAlertEmailRenderer() {}

  /**
   * Renders the full HTML email body from the alert template with inline CSS.
   *
   * @param alert the alert data to render
   * @param environment the active environment/profile name (e.g. "prod")
   * @return the rendered HTML string
   */
  public static String renderHtml(AlertDTO alert, String environment) {
    String subject = blankTo(alert.getSubject(), "OrchesT Incident Alert");
    String body = blankTo(alert.getMessageBody(), "An incident alert is firing.");
    String env = blankTo(environment, "unknown");
    AlertState state = alert.getState() == null ? AlertState.FIRING : alert.getState();

    return HTML_TEMPLATE
        .replace("{{logoSrc}}", LOGO_DATA_URI)
        .replace("{{environment}}", escapeHtml(env))
        .replace("{{state}}", escapeHtml(state.name()))
        .replace("{{stateBgColor}}", stateBackgroundColor(state))
        .replace("{{subject}}", escapeHtml(subject))
        .replace("{{messageBody}}", escapeHtml(body).replace("\n", "<br/>"))
        .replace("{{processDefinitionId}}", escapeHtml(nullToDash(alert.getProcessDefinitionId())))
        .replace("{{processInstanceId}}", escapeHtml(nullToDash(alert.getProcessInstanceId())))
        .replace(
            "{{version}}",
            escapeHtml(alert.getVersion() == null ? "—" : String.valueOf(alert.getVersion())))
        .replace(
            "{{count}}",
            escapeHtml(alert.getCount() == null ? "1" : String.valueOf(alert.getCount())))
        .replace("{{createdAt}}", escapeHtml(formatInstant(alert.getCreatedAt())))
        .replace("{{contactFooter}}", escapeHtml(DEFAULT_CONTACT));
  }

  /**
   * Renders a plain-text fallback version of the alert email.
   *
   * @param alert the alert data to render
   * @param environment the active environment/profile name
   * @return the plain-text email content
   */
  public static String renderPlainText(AlertDTO alert, String environment) {
    AlertState state = alert.getState() == null ? AlertState.FIRING : alert.getState();
    StringBuilder text = new StringBuilder();
    text.append("OrchesT Incident Alert").append('\n');
    text.append("State: ").append(state.name()).append('\n');
    text.append("Environment: ").append(blankTo(environment, "unknown")).append('\n');
    text.append("Subject: ")
        .append(blankTo(alert.getSubject(), "OrchesT Incident Alert"))
        .append('\n');
    text.append('\n');
    text.append(blankTo(alert.getMessageBody(), "An incident alert is firing.")).append('\n');
    text.append('\n');
    text.append("Process Definition ID: ")
        .append(nullToDash(alert.getProcessDefinitionId()))
        .append('\n');
    text.append("Process Instance ID: ")
        .append(nullToDash(alert.getProcessInstanceId()))
        .append('\n');
    if (alert.getVersion() != null) {
      text.append("Version: ").append(alert.getVersion()).append('\n');
    }
    text.append("Incident count: ")
        .append(alert.getCount() == null ? 1 : alert.getCount())
        .append('\n');
    text.append("Started at: ").append(formatInstant(alert.getCreatedAt())).append('\n');
    text.append('\n');
    text.append(DEFAULT_CONTACT);
    return text.toString();
  }

  static String stateBackgroundColor(AlertState state) {
    return switch (state) {
      case RESOLVED -> "#16A34A";
      case SILENCED, ACKNOWLEDGED, DISABLED -> "#6B7280";
      case FIRING -> "#DC2626";
    };
  }

  private static String loadTemplate() {
    try {
      ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
      try (InputStream in = resource.getInputStream()) {
        return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load email template: " + TEMPLATE_PATH, e);
    }
  }

  private static String loadLogoDataUri() {
    try {
      ClassPathResource resource = new ClassPathResource(LOGO_PATH);
      try (InputStream in = resource.getInputStream()) {
        byte[] bytes = StreamUtils.copyToByteArray(in);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load email logo: " + LOGO_PATH, e);
    }
  }

  private static String formatInstant(Instant instant) {
    if (instant == null) {
      return "—";
    }
    return TIMESTAMP_FMT.format(instant);
  }

  private static String nullToDash(String value) {
    return StringUtils.hasText(value) ? value : "—";
  }

  private static String blankTo(String value, String fallback) {
    return StringUtils.hasText(value) ? value : fallback;
  }

  /**
   * Escapes HTML special characters to prevent XSS in rendered email content.
   *
   * @param value the raw string
   * @return the HTML-safe escaped string
   */
  public static String escapeHtml(String value) {
    if (value == null) {
      return "";
    }
    StringBuilder out = new StringBuilder(value.length() + 16);
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&' -> out.append("&amp;");
        case '<' -> out.append("&lt;");
        case '>' -> out.append("&gt;");
        case '"' -> out.append("&quot;");
        case '\'' -> out.append("&#39;");
        default -> out.append(c);
      }
    }
    return out.toString();
  }
}
