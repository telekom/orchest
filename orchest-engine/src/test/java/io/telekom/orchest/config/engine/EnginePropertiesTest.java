package io.telekom.orchest.config.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.telekom.orchest.api.core.properties.EngineScale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link EngineProperties} default values, setters, and Lombok-generated methods. */
class EnginePropertiesTest {

  @Test
  @DisplayName("default engineScale is LOCAL")
  void defaultEngineScale_isLocal() {
    EngineProperties props = new EngineProperties();
    assertEquals(EngineScale.LOCAL, props.getEngineScale());
  }

  @Test
  @DisplayName("setter and getter round-trip for engineScale")
  void engineScale_roundTrip() {
    EngineProperties props = new EngineProperties();
    props.setEngineScale(EngineScale.LARGE);
    assertEquals(EngineScale.LARGE, props.getEngineScale());
  }

  @Test
  @DisplayName("Lombok @Data generates equals/hashCode for property participation")
  void dataClass_supportsEquals() {
    EngineProperties a = new EngineProperties();
    a.setEngineScale(EngineScale.SMALL);
    EngineProperties b = new EngineProperties();
    b.setEngineScale(EngineScale.SMALL);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}
