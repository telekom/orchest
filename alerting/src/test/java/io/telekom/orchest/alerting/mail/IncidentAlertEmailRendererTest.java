package io.telekom.orchest.alerting.mail;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.alerting.api.dto.AlertDTO;
import io.telekom.orchest.alerting.service.IncidentAlertEmailRenderer;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests for {@link IncidentAlertEmailRenderer} HTML/plain-text rendering and HTML escaping. */
class IncidentAlertEmailRendererTest {

  @Test
  @DisplayName("HTML template renders placeholders and escapes untrusted content")
  void rendersHtmlSafely() {
    AlertDTO alert =
        AlertDTO.builder()
            .subject("Disk <full> & urgent")
            .messageBody("Volume /data is 95% full\nPlease investigate")
            .processDefinitionId("order-flow")
            .processInstanceId("pi-1")
            .version(3)
            .count(7)
            .createdAt(Instant.parse("2026-08-08T12:00:00Z"))
            .build();

    String html = IncidentAlertEmailRenderer.renderHtml(alert, "prod");

    assertTrue(html.contains("Disk &lt;full&gt; &amp; urgent"));
    assertTrue(html.contains("Volume /data is 95% full<br/>Please investigate"));
    assertTrue(html.contains("order-flow"));
    assertTrue(html.contains("pi-1"));
    assertTrue(html.matches("(?s).*\\b3\\b.*"));
    assertTrue(html.matches("(?s).*\\b7\\b.*"));
    assertTrue(html.contains("prod"));
    assertTrue(html.contains("FIRING"));
    assertTrue(html.contains("#DC2626"));
    assertTrue(html.contains("data:image/png;base64,"));
    assertTrue(html.contains("role=\"presentation\""));
    assertFalse(html.contains("{{logoSrc}}"));
    assertFalse(html.contains("{{subject}}"));
    assertFalse(html.contains("{{messageBody}}"));
    assertFalse(html.contains("{{state}}"));
    assertFalse(html.contains("{{stateBgColor}}"));
  }

  @ParameterizedTest
  @CsvSource({
    "FIRING,#DC2626,FIRING",
    "RESOLVED,#16A34A,RESOLVED",
    "SILENCED,#6B7280,SILENCED",
    "ACKNOWLEDGED,#6B7280,ACKNOWLEDGED"
  })
  @DisplayName("status badge uses state-specific label and background color")
  void rendersStateBadge(AlertState state, String bgColor, String label) {
    AlertDTO alert = AlertDTO.builder().subject("s").messageBody("b").state(state).build();

    String html = IncidentAlertEmailRenderer.renderHtml(alert, "prod");

    assertTrue(html.contains(label));
    assertTrue(html.contains("background-color:" + bgColor));
    assertTrue(html.contains("bgcolor=\"" + bgColor + "\""));
  }

  @Test
  @DisplayName("plain text fallback includes core fields")
  void rendersPlainText() {
    AlertDTO alert =
        AlertDTO.builder().subject("Disk full").messageBody("Almost out of space").count(2).build();

    String text = IncidentAlertEmailRenderer.renderPlainText(alert, "local");

    assertTrue(text.contains("Environment: local"));
    assertTrue(text.contains("Subject: Disk full"));
    assertTrue(text.contains("Almost out of space"));
    assertTrue(text.contains("Incident count: 2"));
    assertTrue(text.contains("Team Tyrell"));
  }

  @Test
  @DisplayName("escapeHtml encodes markup characters")
  void escapesHtml() {
    assertEquals("&lt;b&gt;&amp;&quot;&#39;", IncidentAlertEmailRenderer.escapeHtml("<b>&\"'"));
  }
}
