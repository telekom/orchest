package io.telekom.orchest.api.core.utils;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.CronExpression;

/**
 * Utility class for parsing ISO-8601 date/time expressions and Quartz cron expressions. Supports
 * various formats including intervals, repeating intervals, and standard date/time strings.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IsoDateTimeParser {

  /**
   * Parses: - ISO-8601 date/time - ISO-8601 interval - ISO-8601 repeating interval (R / Rn) -
   * Quartz cron expression
   *
   * @param expression ISO-8601 or cron
   * @return next execution time as LocalDateTime (system default zone)
   */
  public static LocalDateTime parseToLocalDateTime(String expression) {

    expression = expression.trim();

    if (expression.startsWith("=")) {
      expression = expression.substring(1);
    }

    // 1. ISO-8601 Interval
    if (expression.startsWith("P")) {
      return parseIsoInterval(expression);
    }

    // 1. ISO-8601 Repeating Interval
    if (expression.startsWith("R")) {
      return parseIsoRepeatingInterval(expression);
    }

    // 2. Cron Expression
    if (isCronExpression(expression)) {
      return parseCronExpression(expression);
    }

    // 3. Plain ISO-8601 Date/Time
    return parseIsoDateTime(expression);
  }

  // ----------------- ISO DATE/TIME -----------------

  private static LocalDateTime parseIsoDateTime(String value) {
    try {
      return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    } catch (DateTimeParseException ignored) {
    }

    try {
      return OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
          .atZoneSameInstant(ZoneId.systemDefault())
          .toLocalDateTime();
    } catch (DateTimeParseException ignored) {
    }

    try {
      return Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Unsupported ISO-8601 date/time: " + value, e);
    }
  }

  // ----------------- ISO REPEATING INTERVAL -----------------

  private static LocalDateTime parseIsoInterval(String value) {
    return LocalDateTime.now().plus(Duration.parse(value));
  }

  private static LocalDateTime parseIsoRepeatingInterval(String value) {

    String[] parts = value.split("/");

    int index = 0;

    // R or Rn
    String repeatPart = parts[index++];
    int repeatCount =
        repeatPart.length() > 1 ? Integer.parseInt(repeatPart.substring(1)) : Integer.MAX_VALUE;

    Instant start;
    Duration duration;

    if (parts.length == 2) {
      // R/PT10M
      start = Instant.now();
      duration = Duration.parse(parts[1]);
    } else if (parts.length == 3) {
      // R/START/PT10M or Rn/START/PT10M
      start = parseStartInstant(parts[index++]);
      duration = Duration.parse(parts[index]);
    } else {
      throw new IllegalArgumentException("Invalid ISO-8601 repeating interval: " + value);
    }

    Instant now = Instant.now();
    Instant next = start;

    int iterations = 0;
    while (next.isBefore(now) && iterations < repeatCount) {
      next = next.plus(duration);
      iterations++;
    }

    return next.atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  private static Instant parseStartInstant(String value) {
    try {
      return OffsetDateTime.parse(value).toInstant();
    } catch (DateTimeParseException ignored) {
    }

    try {
      return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid interval start time: " + value, e);
    }
  }

  // ----------------- CRON -----------------

  /**
   * Checks if a string is a Quartz cron expression. Cron expressions typically have 6 or more
   * space-separated fields.
   *
   * @param value The string to check.
   * @return true if the string appears to be a cron expression, false otherwise.
   */
  public static boolean isCronExpression(String value) {
    return value.split("\\s+").length >= 6;
  }

  /**
   * Parses a Quartz cron expression and returns the next valid execution time.
   *
   * @param cron The cron expression to parse.
   * @return The next valid execution time as LocalDateTime.
   * @throws IllegalArgumentException if the cron expression is invalid.
   */
  public static LocalDateTime parseCronExpression(String cron) {
    try {
      CronExpression cronExpression = new CronExpression(cron);
      Date next = cronExpression.getNextValidTimeAfter(new Date());

      return LocalDateTime.ofInstant(next.toInstant(), ZoneId.systemDefault());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid cron expression: " + cron, e);
    }
  }
}
