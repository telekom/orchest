package io.telekom.orchest.telemetry.metrics;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.util.Objects;

/**
 * Compound, immutable identity for a metric stream.
 *
 * <p>Each {@code MetricKey} corresponds to exactly one Micrometer {@link
 * io.micrometer.core.instrument.Counter} and exactly one MongoDB document (per collection). The
 * string returned by {@link #stableId()} is used as the MongoDB {@code _id} for the lifetime-totals
 * document.
 */
public record MetricKey(MetricType type, String definitionId, Integer version) {

  public static final String METER_NAME_PREFIX = "orchest.lifetime.";
  public static final String TAG_DEFINITION_ID = "definitionId";
  public static final String TAG_VERSION = "version";

  private static final String SEP = "::";
  private static final String NULL_TOKEN = "_";

  public MetricKey {
    Objects.requireNonNull(type, "metric type must not be null");
  }

  /** Convenience factory for engine-wide counters not scoped to any definition. */
  public static MetricKey of(MetricType type) {
    return new MetricKey(type, null, null);
  }

  /** Convenience factory for per-definition counters that don't care about version. */
  public static MetricKey of(MetricType type, String definitionId) {
    return new MetricKey(type, definitionId, null);
  }

  /** Convenience factory for fully-qualified per-version counters. */
  public static MetricKey of(MetricType type, String definitionId, Integer version) {
    return new MetricKey(type, definitionId, version);
  }

  /**
   * Stable, deterministic identifier suitable for use as a MongoDB {@code _id}. Format: {@code
   * <type>::<definitionId|_>::<version|_>}.
   */
  public String stableId() {
    return type.name()
        + SEP
        + (definitionId == null ? NULL_TOKEN : definitionId)
        + SEP
        + (version == null ? NULL_TOKEN : version);
  }

  /**
   * Micrometer meter name for this key. Identical for all instances of the same {@link MetricType}.
   */
  public String meterName() {
    return METER_NAME_PREFIX + type.meterSuffix();
  }

  /** Micrometer tags representing the {@code definitionId} / {@code version} dimensions. */
  public Tags meterTags() {
    return Tags.of(
        Tag.of(TAG_DEFINITION_ID, definitionId == null ? NULL_TOKEN : definitionId),
        Tag.of(TAG_VERSION, version == null ? NULL_TOKEN : version.toString()));
  }
}
