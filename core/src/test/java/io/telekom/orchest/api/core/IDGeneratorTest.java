package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.utils.IDGenerator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link IDGenerator} verifying unique ID generation and format correctness. */
class IDGeneratorTest {

  @Nested
  class Generate {

    @Test
    void shouldReturnNonNullUUID() {
      String id = IDGenerator.generate();

      assertNotNull(id);
    }

    @Test
    void shouldReturnValidUUIDFormat() {
      String id = IDGenerator.generate();

      assertTrue(
          id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
          "ID should be a valid UUID: " + id);
    }

    @Test
    void shouldReturn36CharacterString() {
      String id = IDGenerator.generate();

      assertEquals(36, id.length());
    }

    @Test
    void shouldProduceUniqueUUIDs() {
      Set<String> ids = new HashSet<>();

      for (int i = 0; i < 1000; i++) {
        ids.add(IDGenerator.generate());
      }

      assertEquals(1000, ids.size(), "Expected 1000 unique UUIDs out of 1000, got: " + ids.size());
    }

    @Test
    void shouldProduceDifferentConsecutiveIds() {
      String id1 = IDGenerator.generate();
      String id2 = IDGenerator.generate();

      assertNotEquals(id1, id2);
    }
  }

  @Nested
  class GenerateNumber {

    @Test
    void shouldReturnNonNullString() {
      String id = IDGenerator.generateNumber();

      assertNotNull(id);
    }

    @Test
    void shouldReturnNonEmptyString() {
      String id = IDGenerator.generateNumber();

      assertFalse(id.isEmpty());
    }

    @Test
    void shouldReturn15DigitString() {
      String id = IDGenerator.generateNumber();

      assertEquals(15, id.length());
    }

    @Test
    void shouldReturnAllDigitsString() {
      String id = IDGenerator.generateNumber();

      assertTrue(id.matches("\\d{15}"), "ID should be exactly 15 digits: " + id);
    }
  }

  @Nested
  class Uniqueness {

    @Test
    void shouldProduceHighlyUniqueIdsOver1000Calls() {
      Set<String> ids = new HashSet<>();

      for (int i = 0; i < 1000; i++) {
        ids.add(IDGenerator.generateNumber());
      }

      assertEquals(1000, ids.size(), "Expected 1000 unique IDs out of 1000, got: " + ids.size());
    }

    @Test
    void shouldProduceDifferentConsecutiveIds() {
      String id1 = IDGenerator.generateNumber();
      String id2 = IDGenerator.generateNumber();

      assertNotEquals(id1, id2);
    }

    @Test
    void shouldProduceUniqueIdsOver100Calls() {
      Set<String> ids = new HashSet<>();

      for (int i = 0; i < 100; i++) {
        ids.add(IDGenerator.generateNumber());
      }

      assertEquals(100, ids.size(), "Expected 100 unique IDs out of 100, got: " + ids.size());
    }
  }

  @Nested
  class Format {

    @Test
    void shouldContainOnlyDigitCharacters() {
      for (int i = 0; i < 100; i++) {
        String id = IDGenerator.generateNumber();
        for (char c : id.toCharArray()) {
          assertTrue(Character.isDigit(c), "Non-digit character found in ID: " + id);
        }
      }
    }

    @RepeatedTest(50)
    void shouldAlwaysReturn15DigitsAcrossRepeatedCalls() {
      String id = IDGenerator.generateNumber();

      assertEquals(15, id.length(), "ID length should be 15 but was " + id.length() + ": " + id);
    }

    @Test
    void shouldNotContainLetters() {
      for (int i = 0; i < 100; i++) {
        String id = IDGenerator.generateNumber();
        assertFalse(id.matches(".*[a-zA-Z].*"), "ID should not contain letters: " + id);
      }
    }

    @Test
    void shouldNotContainSpecialCharacters() {
      for (int i = 0; i < 100; i++) {
        String id = IDGenerator.generateNumber();
        assertFalse(id.matches(".*[^\\d].*"), "ID should not contain special chars: " + id);
      }
    }

    @Test
    void shouldNotStartWithNegativeSign() {
      for (int i = 0; i < 100; i++) {
        String id = IDGenerator.generateNumber();
        assertFalse(id.startsWith("-"), "ID should not start with minus: " + id);
      }
    }
  }

  @Nested
  class SequenceComponent {

    @Test
    void shouldHaveThreeDigitSequenceSuffix() {
      for (int i = 0; i < 20; i++) {
        String id = IDGenerator.generateNumber();
        String seq = id.substring(12, 15);
        assertTrue(seq.matches("\\d{3}"), "Sequence suffix should be 3 digits: " + id);
        int n = Integer.parseInt(seq);
        assertTrue(n >= 0 && n <= 999, "Sequence should be 0-999, got: " + n);
      }
    }
  }

  @Nested
  class ThreadSafety {

    @Test
    void shouldGenerateUniqueIdsFromMultipleThreads() throws InterruptedException {
      Set<String> ids = Collections.synchronizedSet(new HashSet<>());
      int threadCount = 100;
      int idsPerThread = 400;
      Thread[] threads = new Thread[threadCount];

      for (int i = 0; i < threadCount; i++) {
        threads[i] =
            new Thread(
                () -> {
                  for (int j = 0; j < idsPerThread; j++) {
                    ids.add(IDGenerator.generateNumber());
                  }
                });
      }

      for (Thread t : threads) t.start();
      for (Thread t : threads) t.join();

      int totalGenerated = threadCount * idsPerThread;
      assertEquals(
          totalGenerated,
          ids.size(),
          "Expected all unique IDs from concurrent generation, got: "
              + ids.size()
              + " out of "
              + totalGenerated);
    }

    @Test
    void shouldHaveNoDuplicatesOrCollisionsAcross100Threads() throws InterruptedException {
      Set<String> ids = Collections.synchronizedSet(new HashSet<>());
      int threadCount = 100;
      int idsPerThread = 200;
      Thread[] threads = new Thread[threadCount];

      for (int i = 0; i < threadCount; i++) {
        threads[i] =
            new Thread(
                () -> {
                  for (int j = 0; j < idsPerThread; j++) {
                    ids.add(IDGenerator.generateNumber());
                  }
                });
      }

      for (Thread t : threads) {
        t.start();
      }
      for (Thread t : threads) {
        t.join();
      }

      int expected = threadCount * idsPerThread;
      assertEquals(
          expected,
          ids.size(),
          "Duplicate or colliding IDs detected: expected "
              + expected
              + " unique IDs, got "
              + ids.size());
    }

    @Test
    void shouldProduceValidFormatFromMultipleThreads() throws InterruptedException {
      Set<String> ids = Collections.synchronizedSet(new HashSet<>());
      Thread[] threads = new Thread[5];

      for (int i = 0; i < 5; i++) {
        threads[i] =
            new Thread(
                () -> {
                  for (int j = 0; j < 50; j++) {
                    ids.add(IDGenerator.generateNumber());
                  }
                });
      }

      for (Thread t : threads) t.start();
      for (Thread t : threads) t.join();

      for (String id : ids) {
        assertEquals(15, id.length(), "Multi-threaded ID has wrong length: " + id);
        assertTrue(id.matches("\\d{15}"), "Multi-threaded ID is not all digits: " + id);
      }
    }
  }
}
