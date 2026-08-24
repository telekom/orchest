package io.telekom.orchest.adapter.mongo.convertors.converters;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;

/**
 * Utility class grouping custom converters for handling {@link OffsetDateTime} in MongoDB mapping
 * contexts.
 *
 * <p>Provides converters between {@link Date} and {@link OffsetDateTime}, and {@link String} to
 * {@link OffsetDateTime}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OffsetDateTimeConverter {

  /** Converts a {@link Date} to an {@link OffsetDateTime} in UTC. */
  public static class FromDate implements Converter<Date, OffsetDateTime> {
    /**
     * Converts the source Date to OffsetDateTime.
     *
     * @param source The source Date.
     * @return The corresponding OffsetDateTime in UTC.
     */
    @Override
    public OffsetDateTime convert(Date source) {
      return source.toInstant().atOffset(ZoneOffset.UTC);
    }
  }

  /** Converts an {@link OffsetDateTime} to a {@link Date}. */
  public static class ToDate implements Converter<OffsetDateTime, Date> {
    /**
     * Converts the source OffsetDateTime to Date.
     *
     * @param source The source OffsetDateTime.
     * @return The corresponding Date.
     */
    @Override
    public Date convert(OffsetDateTime source) {
      return Date.from(source.toInstant());
    }
  }

  /** Converts a {@link String} (ISO Date Time format) to an {@link OffsetDateTime}. */
  public static class FromString implements Converter<String, OffsetDateTime> {
    /**
     * Converts the source String to OffsetDateTime.
     *
     * @param source The source String in ISO date time format.
     * @return The parsed OffsetDateTime, or null if source is null.
     */
    @Override
    public OffsetDateTime convert(String source) {
      return source == null ? null : OffsetDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
    }
  }
}
