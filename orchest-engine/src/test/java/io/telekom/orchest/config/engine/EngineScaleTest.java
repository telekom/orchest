package io.telekom.orchest.config.engine;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.properties.EngineScale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link EngineScale} enum values, partition counts, replication factors, and concurrency
 * calculations.
 */
class EngineScaleTest {

  // --- LOCAL scale tests ---

  @Test
  @DisplayName("LOCAL scale should have replicationCount=1")
  void local_replicationCount() {
    assertEquals(1, EngineScale.LOCAL.getReplicationCount());
  }

  @Test
  @DisplayName("LOCAL scale should have partitionCount=3")
  void local_partitionCount() {
    assertEquals(3, EngineScale.LOCAL.getPartitionCount());
  }

  @Test
  @DisplayName("LOCAL scale should have factor=1.0")
  void local_factor() {
    assertEquals(1.0f, EngineScale.LOCAL.getFactor());
  }

  @Test
  @DisplayName("LOCAL scale derived partition counts")
  void local_derivedPartitionCounts() {
    assertEquals(3, EngineScale.LOCAL.getFactoredPartitionCount());
    assertEquals(3, EngineScale.LOCAL.getDefaultPartitionCount());
  }

  // --- SMALL scale tests ---

  @Test
  @DisplayName("SMALL scale should have replicationCount=3")
  void small_replicationCount() {
    assertEquals(3, EngineScale.SMALL.getReplicationCount());
  }

  @Test
  @DisplayName("SMALL scale should have partitionCount=10")
  void small_partitionCount() {
    assertEquals(10, EngineScale.SMALL.getPartitionCount());
  }

  @Test
  @DisplayName("SMALL scale should have factor=1.0")
  void small_factor() {
    assertEquals(1.0f, EngineScale.SMALL.getFactor());
  }

  @Test
  @DisplayName("SMALL scale derived partition counts")
  void small_derivedPartitionCounts() {
    assertEquals(10, EngineScale.SMALL.getFactoredPartitionCount());
    assertEquals(3, EngineScale.SMALL.getDefaultPartitionCount());
  }

  // --- MEDIUM scale tests ---

  @Test
  @DisplayName("MEDIUM scale should have replicationCount=3")
  void medium_replicationCount() {
    assertEquals(3, EngineScale.MEDIUM.getReplicationCount());
  }

  @Test
  @DisplayName("MEDIUM scale should have partitionCount=25")
  void medium_partitionCount() {
    assertEquals(25, EngineScale.MEDIUM.getPartitionCount());
  }

  @Test
  @DisplayName("MEDIUM scale should have factor=1.5")
  void medium_factor() {
    assertEquals(1.5f, EngineScale.MEDIUM.getFactor());
  }

  @Test
  @DisplayName("MEDIUM scale derived partition counts")
  void medium_derivedPartitionCounts() {
    assertEquals((int) (25 / 1.5f), EngineScale.MEDIUM.getFactoredPartitionCount());
    assertEquals(5, EngineScale.MEDIUM.getDefaultPartitionCount());
  }

  // --- LARGE scale tests ---

  @Test
  @DisplayName("LARGE scale should have replicationCount=3")
  void large_replicationCount() {
    assertEquals(3, EngineScale.LARGE.getReplicationCount());
  }

  @Test
  @DisplayName("LARGE scale should have partitionCount=100")
  void large_partitionCount() {
    assertEquals(100, EngineScale.LARGE.getPartitionCount());
  }

  @Test
  @DisplayName("LARGE scale should have factor=2.0")
  void large_factor() {
    assertEquals(2.0f, EngineScale.LARGE.getFactor());
  }

  @Test
  @DisplayName("LARGE scale derived partition counts")
  void large_derivedPartitionCounts() {
    assertEquals(50, EngineScale.LARGE.getFactoredPartitionCount());
    assertEquals(10, EngineScale.LARGE.getDefaultPartitionCount());
  }

  // --- X_LARGE scale tests ---

  @Test
  @DisplayName("X_LARGE scale should have replicationCount=3")
  void xLarge_replicationCount() {
    assertEquals(3, EngineScale.X_LARGE.getReplicationCount());
  }

  @Test
  @DisplayName("X_LARGE scale should have partitionCount=300")
  void xLarge_partitionCount() {
    assertEquals(300, EngineScale.X_LARGE.getPartitionCount());
  }

  @Test
  @DisplayName("X_LARGE scale should have factor=2.5")
  void xLarge_factor() {
    assertEquals(2.5f, EngineScale.X_LARGE.getFactor());
  }

  @Test
  @DisplayName("X_LARGE scale derived partition counts")
  void xLarge_derivedPartitionCounts() {
    assertEquals(120, EngineScale.X_LARGE.getFactoredPartitionCount());
    assertEquals(10, EngineScale.X_LARGE.getDefaultPartitionCount());
  }

  // --- Cross-cutting enum tests ---

  @Test
  @DisplayName("EngineScale should have exactly 5 enum values")
  void shouldHaveFiveEnumValues() {
    assertEquals(5, EngineScale.values().length);
  }

  @Test
  @DisplayName("valueOf should resolve all scale names correctly")
  void valueOf_shouldResolveAllScaleNames() {
    assertEquals(EngineScale.LOCAL, EngineScale.valueOf("LOCAL"));
    assertEquals(EngineScale.SMALL, EngineScale.valueOf("SMALL"));
    assertEquals(EngineScale.MEDIUM, EngineScale.valueOf("MEDIUM"));
    assertEquals(EngineScale.LARGE, EngineScale.valueOf("LARGE"));
    assertEquals(EngineScale.X_LARGE, EngineScale.valueOf("X_LARGE"));
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("All scales should have positive replication count")
  void allScales_shouldHavePositiveReplicationCount(EngineScale scale) {
    assertTrue(scale.getReplicationCount() > 0, "Replication count must be positive for " + scale);
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("All scales should have positive partitionCount")
  void allScales_shouldHavePositivePartitionCount(EngineScale scale) {
    assertTrue(scale.getPartitionCount() > 0, "partitionCount must be positive for " + scale);
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("All scales should have positive derived partition counts")
  void allScales_shouldHavePositiveDerivedValues(EngineScale scale) {
    assertTrue(
        scale.getFactoredPartitionCount() > 0,
        "factoredPartitionCount must be positive for " + scale);
    assertTrue(
        scale.getDefaultPartitionCount() > 0,
        "defaultPartitionCount must be positive for " + scale);
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("Derived partition formula: factoredPartitionCount = (int)(partitionCount / factor)")
  void allScales_factoredPartitionFormula(EngineScale scale) {
    int expected = (int) (scale.getPartitionCount() / scale.getFactor());
    assertEquals(
        expected,
        scale.getFactoredPartitionCount(),
        "factoredPartitionCount formula mismatch for " + scale);
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName(
      "defaultPartitionCount should match scale-name mapping (MEDIUM=5, LARGE/X_LARGE=10, else=3)")
  void allScales_defaultPartitionCountMapping(EngineScale scale) {
    int expected =
        switch (scale) {
          case MEDIUM -> 5;
          case LARGE, X_LARGE -> 10;
          default -> 3;
        };
    assertEquals(
        expected, scale.getDefaultPartitionCount(), "defaultPartitionCount mismatch for " + scale);
  }

  // --- getConcurrency static method tests ---

  @Test
  @DisplayName("getConcurrency divides partitionCount by brokerCount")
  void getConcurrency_dividesPartitionsByBrokers() {
    assertEquals(5, EngineScale.getConcurrency(15, 3));
    assertEquals(0, EngineScale.getConcurrency(2, 3));
    assertEquals(33, EngineScale.getConcurrency(100, 3));
  }
}
