package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.utils.IsoDateTimeParser;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IsoDateTimeParser} covering ISO date-time, duration, cron, and repeating
 * interval parsing.
 */
class IsoDateTimeParserTest {

  @Nested
  class IsoDateTimeParsing {

    @Test
    void shouldParseLocalDateTime() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("2025-06-15T10:30:00");

      assertEquals(2025, result.getYear());
      assertEquals(6, result.getMonthValue());
      assertEquals(15, result.getDayOfMonth());
      assertEquals(10, result.getHour());
      assertEquals(30, result.getMinute());
      assertEquals(0, result.getSecond());
    }

    @Test
    void shouldParseLocalDateTimeWithSeconds() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("2025-12-31T23:59:59");

      assertEquals(23, result.getHour());
      assertEquals(59, result.getMinute());
      assertEquals(59, result.getSecond());
    }

    @Test
    void shouldParseOffsetDateTime() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("2025-06-15T10:30:00+02:00");

      assertNotNull(result);
      // Exact local time depends on system zone, but parsing should succeed
    }

    @Test
    void shouldParseInstantWithZSuffix() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("2025-06-15T10:30:00Z");

      assertNotNull(result);
    }

    @Test
    void shouldParseLocalDateTimeWithMilliseconds() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("2025-06-15T10:30:00.123");

      assertEquals(2025, result.getYear());
      assertEquals(123_000_000, result.getNano());
    }

    @Test
    void shouldThrowForUnsupportedDateFormat() {
      assertThrows(
          IllegalArgumentException.class,
          () -> IsoDateTimeParser.parseToLocalDateTime("not-a-date"));
    }

    @Test
    void shouldThrowForDateOnlyFormat() {
      assertThrows(Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("2025-06-15"));
    }
  }

  @Nested
  class LeadingEqualsAndWhitespace {

    @Test
    void shouldStripLeadingEqualsSign() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("=2025-06-15T10:30:00");

      assertEquals(2025, result.getYear());
      assertEquals(6, result.getMonthValue());
    }

    @Test
    void shouldTrimWhitespace() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("  2025-06-15T10:30:00  ");

      assertEquals(2025, result.getYear());
    }

    @Test
    void shouldTrimWhitespaceAndStripEquals() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("  =2025-06-15T10:30:00  ");

      assertEquals(2025, result.getYear());
    }

    @Test
    void shouldThrowForEqualsSignOnly() {
      assertThrows(Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("="));
    }

    @Test
    void shouldHandleEqualsBeforeDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("=PT10M");

      assertTrue(result.isAfter(before.plusMinutes(9)));
    }
  }

  @Nested
  class DurationParsing {

    @Test
    void shouldReturnFutureTimeForMinutesDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("PT10M");

      assertTrue(result.isAfter(before.plusMinutes(9)));
      assertTrue(result.isBefore(before.plusMinutes(11)));
    }

    @Test
    void shouldReturnFutureTimeForHoursDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("PT2H");

      assertTrue(result.isAfter(before.plusHours(1)));
      assertTrue(result.isBefore(before.plusHours(3)));
    }

    @Test
    void shouldReturnFutureTimeForDaysDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("P1D");

      assertTrue(result.isAfter(before.plusHours(23)));
    }

    @Test
    void shouldReturnFutureTimeForSecondsDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("PT30S");

      assertTrue(result.isAfter(before.plusSeconds(29)));
      assertTrue(result.isBefore(before.plusSeconds(31)));
    }

    @Test
    void shouldReturnFutureTimeForComplexDuration() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("P1DT2H30M");

      assertTrue(result.isAfter(before.plusDays(1).plusHours(2).plusMinutes(29)));
    }

    @Test
    void shouldReturnNowForZeroDuration() {
      LocalDateTime before = LocalDateTime.now().minusSeconds(1);

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("PT0S");

      assertTrue(result.isAfter(before));
    }
  }

  @Nested
  class RepeatingIntervalParsing {

    @Test
    void shouldParseRepeatingIntervalWithDurationOnly() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("R/PT5M");

      assertTrue(result.isAfter(before.plusMinutes(4)));
    }

    @Test
    void shouldParseRepeatingIntervalWithCount() {
      LocalDateTime before = LocalDateTime.now();

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("R3/PT10M");

      assertNotNull(result);
      assertTrue(result.isAfter(before.plusMinutes(9)));
    }

    @Test
    void shouldThrowForInvalidRepeatingIntervalWithTooManyParts() {
      assertThrows(
          IllegalArgumentException.class,
          () -> IsoDateTimeParser.parseToLocalDateTime("R/a/b/c/d"));
    }

    @Test
    void shouldParseRepeatingIntervalWithStartAndDuration() {
      // Use a very recent start so it quickly advances past now
      String start = OffsetDateTime_nowUtcString();
      String expression = "R/" + start + "/PT1H";

      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime(expression);

      assertNotNull(result);
    }

    @Test
    void shouldReturnFutureTimeForUnboundedRepeat() {
      LocalDateTime before = LocalDateTime.now();

      // R with no count means unbounded (Integer.MAX_VALUE)
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("R/PT1M");

      assertTrue(result.isAfter(before));
    }

    private String OffsetDateTime_nowUtcString() {
      return java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString();
    }
  }

  @Nested
  class CronExpressionParsing {

    @Test
    void shouldIdentifyValidCronAsTrue() {
      assertTrue(IsoDateTimeParser.isCronExpression("0 0 12 * * ?"));
    }

    @Test
    void shouldIdentifyIsoDateAsFalse() {
      assertFalse(IsoDateTimeParser.isCronExpression("2025-06-15T10:30:00"));
    }

    @Test
    void shouldIdentifyDurationAsFalse() {
      assertFalse(IsoDateTimeParser.isCronExpression("PT10M"));
    }

    @Test
    void shouldIdentifySixFieldCronAsTrue() {
      assertTrue(IsoDateTimeParser.isCronExpression("0 0 12 * * ?"));
    }

    @Test
    void shouldIdentifySevenFieldCronAsTrue() {
      assertTrue(IsoDateTimeParser.isCronExpression("0 0 12 * * ? 2025"));
    }

    @Test
    void shouldIdentifyFiveFieldExpressionAsFalse() {
      assertFalse(IsoDateTimeParser.isCronExpression("0 0 12 * *"));
    }

    @Test
    void shouldParseCronExpressionReturningFutureTime() {
      LocalDateTime result = IsoDateTimeParser.parseCronExpression("0/1 * * * * ?");

      assertNotNull(result);
      assertTrue(result.isAfter(LocalDateTime.now().minusSeconds(2)));
    }

    @Test
    void shouldThrowForInvalidCronExpression() {
      assertThrows(
          IllegalArgumentException.class,
          () -> IsoDateTimeParser.parseCronExpression("invalid cron expression here now"));
    }

    @Test
    void shouldParseFullCronViaParseToLocalDateTime() {
      LocalDateTime result = IsoDateTimeParser.parseToLocalDateTime("0 0 12 * * ?");

      assertNotNull(result);
    }

    @Test
    void shouldReturnFutureTimeForDailyCron() {
      LocalDateTime result = IsoDateTimeParser.parseCronExpression("0 0 0 * * ?");

      assertNotNull(result);
      // Midnight daily - next execution should be in the future
      assertTrue(result.isAfter(LocalDateTime.now().minusSeconds(1)));
    }
  }

  @Nested
  class EdgeCases {

    @Test
    void shouldThrowForCompletelyUnsupportedFormat() {
      assertThrows(
          IllegalArgumentException.class,
          () -> IsoDateTimeParser.parseToLocalDateTime("not-a-date"));
    }

    @Test
    void shouldThrowForEqualsSignOnly() {
      assertThrows(Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("="));
    }

    @Test
    void shouldThrowForNullInput() {
      assertThrows(NullPointerException.class, () -> IsoDateTimeParser.parseToLocalDateTime(null));
    }

    @Test
    void shouldThrowForRandomNumbers() {
      assertThrows(Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("12345"));
    }

    @Test
    void shouldThrowForSqlDateFormat() {
      assertThrows(
          Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("2025/06/15 10:30:00"));
    }

    @Test
    void shouldHandleWhitespaceOnlyInput() {
      assertThrows(Exception.class, () -> IsoDateTimeParser.parseToLocalDateTime("   "));
    }
  }
}
