package io.telekom.orchest.alerting;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ModuleProperties} configuration binding and nested property access. */
class TelemetryModulePropertiesTest {

  @Test
  @DisplayName("nested alerting and configs round-trip")
  void alertingNested() {
    ModuleProperties.AlertingConfig cfg = new ModuleProperties.AlertingConfig();
    cfg.setEnabled(true);
    cfg.setHost("smtp.example.com");
    cfg.setPort(587);
    cfg.setUserName("u");
    cfg.setPassword("p");

    Map<ModuleProperties.AlertType, ModuleProperties.AlertingConfig> configs =
        new EnumMap<>(ModuleProperties.AlertType.class);
    configs.put(ModuleProperties.AlertType.MS_TEAMS, cfg);

    ModuleProperties props = new ModuleProperties();
    props.setEnabled(true);
    props.setConfigs(configs);

    assertTrue(props.isEnabled());
    assertSame(cfg, props.getConfigs().get(ModuleProperties.AlertType.MS_TEAMS));
  }
}
