package io.telekom.orchest.configuration.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link EngineScale} verifying concurrency derivation formulas and enum values. */
@DisplayName("EngineScale")
class EngineScaleTest {

  @Nested
  @DisplayName("Enum values")
  class EnumValues {

    @Test
    @DisplayName("should have exactly 5 enum values")
    void shouldHaveFiveValues() {
      assertThat(EngineScale.values()).hasSize(5);
    }

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName("all enum values should be accessible")
    void allValuesShouldBeAccessible(EngineScale scale) {
      assertThat(scale).isNotNull();
    }

    @Test
    @DisplayName("should contain LOCAL, SMALL, MEDIUM, LARGE, X_LARGE")
    void shouldContainExpectedValues() {
      assertThat(EngineScale.values())
          .containsExactly(
              EngineScale.LOCAL,
              EngineScale.SMALL,
              EngineScale.MEDIUM,
              EngineScale.LARGE,
              EngineScale.X_LARGE);
    }
  }

  @Nested
  @DisplayName("LOCAL scale")
  class LocalScale {

    private final EngineScale scale = EngineScale.LOCAL;

    @Test
    @DisplayName("should have replicationCount = 1")
    void replicationCount() {
      assertThat(scale.getReplicationCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should have processInvocationConcurrency = 3")
    void processInvocationConcurrency() {
      assertThat(scale.getProcessInvocationConcurrency()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have factor = 1.0")
    void factor() {
      assertThat(scale.getFactor()).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("should have processDeploymentConcurrency = 3 (3 / 1.0)")
    void processDeploymentConcurrency() {
      // (int)(3 / 1.0f) = 3
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have serverWorkerEventConcurrency = 3 (3 * 1.0)")
    void serverWorkerEventConcurrency() {
      // (int)(3 * 1.0f) = 3
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have pendingTaskConcurrency = 3 (3 * 1.0)")
    void pendingTaskConcurrency() {
      // (int)(3 * 1.0f) = 3
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have messageEventConcurrency = 3 (3 * 1.0)")
    void messageEventConcurrency() {
      // (int)(3 * 1.0f) = 3
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("SMALL scale")
  class SmallScale {

    private final EngineScale scale = EngineScale.SMALL;

    @Test
    @DisplayName("should have replicationCount = 3")
    void replicationCount() {
      assertThat(scale.getReplicationCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have processInvocationConcurrency = 20")
    void processInvocationConcurrency() {
      assertThat(scale.getProcessInvocationConcurrency()).isEqualTo(20);
    }

    @Test
    @DisplayName("should have factor = 1.5")
    void factor() {
      assertThat(scale.getFactor()).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("should have processDeploymentConcurrency = 13 (20 / 1.5)")
    void processDeploymentConcurrency() {
      // (int)(20 / 1.5f) = (int)(13.333) = 13
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(13);
    }

    @Test
    @DisplayName("should have serverWorkerEventConcurrency = 30 (20 * 1.5)")
    void serverWorkerEventConcurrency() {
      // (int)(20 * 1.5f) = 30
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(30);
    }

    @Test
    @DisplayName("should have pendingTaskConcurrency = 30 (20 * 1.5)")
    void pendingTaskConcurrency() {
      // (int)(20 * 1.5f) = 30
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(30);
    }

    @Test
    @DisplayName("should have messageEventConcurrency = 30 (20 * 1.5)")
    void messageEventConcurrency() {
      // (int)(20 * 1.5f) = 30
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(30);
    }
  }

  @Nested
  @DisplayName("MEDIUM scale")
  class MediumScale {

    private final EngineScale scale = EngineScale.MEDIUM;

    @Test
    @DisplayName("should have replicationCount = 3")
    void replicationCount() {
      assertThat(scale.getReplicationCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have processInvocationConcurrency = 50")
    void processInvocationConcurrency() {
      assertThat(scale.getProcessInvocationConcurrency()).isEqualTo(50);
    }

    @Test
    @DisplayName("should have factor = 2.0")
    void factor() {
      assertThat(scale.getFactor()).isEqualTo(2.0f);
    }

    @Test
    @DisplayName("should have processDeploymentConcurrency = 25 (50 / 2.0)")
    void processDeploymentConcurrency() {
      // (int)(50 / 2.0f) = 25
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(25);
    }

    @Test
    @DisplayName("should have serverWorkerEventConcurrency = 100 (50 * 2.0)")
    void serverWorkerEventConcurrency() {
      // (int)(50 * 2.0f) = 100
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(100);
    }

    @Test
    @DisplayName("should have pendingTaskConcurrency = 100 (50 * 2.0)")
    void pendingTaskConcurrency() {
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(100);
    }

    @Test
    @DisplayName("should have messageEventConcurrency = 100 (50 * 2.0)")
    void messageEventConcurrency() {
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("LARGE scale")
  class LargeScale {

    private final EngineScale scale = EngineScale.LARGE;

    @Test
    @DisplayName("should have replicationCount = 3")
    void replicationCount() {
      assertThat(scale.getReplicationCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have processInvocationConcurrency = 100")
    void processInvocationConcurrency() {
      assertThat(scale.getProcessInvocationConcurrency()).isEqualTo(100);
    }

    @Test
    @DisplayName("should have factor = 2.5")
    void factor() {
      assertThat(scale.getFactor()).isEqualTo(2.5f);
    }

    @Test
    @DisplayName("should have processDeploymentConcurrency = 40 (100 / 2.5)")
    void processDeploymentConcurrency() {
      // (int)(100 / 2.5f) = 40
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(40);
    }

    @Test
    @DisplayName("should have serverWorkerEventConcurrency = 250 (100 * 2.5)")
    void serverWorkerEventConcurrency() {
      // (int)(100 * 2.5f) = 250
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(250);
    }

    @Test
    @DisplayName("should have pendingTaskConcurrency = 250 (100 * 2.5)")
    void pendingTaskConcurrency() {
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(250);
    }

    @Test
    @DisplayName("should have messageEventConcurrency = 250 (100 * 2.5)")
    void messageEventConcurrency() {
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(250);
    }
  }

  @Nested
  @DisplayName("X_LARGE scale")
  class XLargeScale {

    private final EngineScale scale = EngineScale.X_LARGE;

    @Test
    @DisplayName("should have replicationCount = 3")
    void replicationCount() {
      assertThat(scale.getReplicationCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should have processInvocationConcurrency = 300")
    void processInvocationConcurrency() {
      assertThat(scale.getProcessInvocationConcurrency()).isEqualTo(300);
    }

    @Test
    @DisplayName("should have factor = 2.5")
    void factor() {
      assertThat(scale.getFactor()).isEqualTo(2.5f);
    }

    @Test
    @DisplayName("should have processDeploymentConcurrency = 120 (300 / 2.5)")
    void processDeploymentConcurrency() {
      // (int)(300 / 2.5f) = 120
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(120);
    }

    @Test
    @DisplayName("should have serverWorkerEventConcurrency = 750 (300 * 2.5)")
    void serverWorkerEventConcurrency() {
      // (int)(300 * 2.5f) = 750
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(750);
    }

    @Test
    @DisplayName("should have pendingTaskConcurrency = 750 (300 * 2.5)")
    void pendingTaskConcurrency() {
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(750);
    }

    @Test
    @DisplayName("should have messageEventConcurrency = 750 (300 * 2.5)")
    void messageEventConcurrency() {
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(750);
    }
  }

  @Nested
  @DisplayName("Derived concurrency formula verification")
  class DerivedConcurrencyFormulas {

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName("processDeploymentConcurrency should equal (int)(invocationConcurrency / factor)")
    void processDeploymentFormula(EngineScale scale) {
      int expected = (int) (scale.getProcessInvocationConcurrency() / scale.getFactor());
      assertThat(scale.getProcessDeploymentConcurrency()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName("serverWorkerEventConcurrency should equal (int)(invocationConcurrency * factor)")
    void serverWorkerEventFormula(EngineScale scale) {
      int expected = (int) (scale.getProcessInvocationConcurrency() * scale.getFactor());
      assertThat(scale.getServerWorkerEventConcurrency()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName("pendingTaskConcurrency should equal (int)(invocationConcurrency * factor)")
    void pendingTaskFormula(EngineScale scale) {
      int expected = (int) (scale.getProcessInvocationConcurrency() * scale.getFactor());
      assertThat(scale.getPendingTaskConcurrency()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName("messageEventConcurrency should equal (int)(invocationConcurrency * factor)")
    void messageEventFormula(EngineScale scale) {
      int expected = (int) (scale.getProcessInvocationConcurrency() * scale.getFactor());
      assertThat(scale.getMessageEventConcurrency()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(EngineScale.class)
    @DisplayName(
        "serverWorkerEventConcurrency, pendingTaskConcurrency, and messageEventConcurrency should all be equal")
    void multipliedConcurrenciesShouldBeEqual(EngineScale scale) {
      assertThat(scale.getServerWorkerEventConcurrency())
          .isEqualTo(scale.getPendingTaskConcurrency())
          .isEqualTo(scale.getMessageEventConcurrency());
    }
  }

  @Nested
  @DisplayName("Replication counts")
  class ReplicationCounts {

    @Test
    @DisplayName("LOCAL should have replicationCount = 1 (single instance)")
    void localReplication() {
      assertThat(EngineScale.LOCAL.getReplicationCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(
        value = EngineScale.class,
        names = {"SMALL", "MEDIUM", "LARGE", "X_LARGE"})
    @DisplayName("non-LOCAL scales should have replicationCount = 3")
    void nonLocalReplication(EngineScale scale) {
      assertThat(scale.getReplicationCount()).isEqualTo(3);
    }
  }
}
