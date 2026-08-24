package io.telekom.orchest.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link ModuleProperties} getter/setter round-trip. */
class LoggingModulePropertiesTest {

  @Test
  @DisplayName("format property round-trip")
  void format() {
    ModuleProperties p = new ModuleProperties();
    p.setFormat("json");
    assertEquals("json", p.getFormat());
  }
}
