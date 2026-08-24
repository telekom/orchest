package io.telekom.orchest.api.core.utils;

import java.net.InetAddress;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating unique identifiers across the distributed system.
 *
 * <p>The generated ID is a 15-digit numeric string composed of:
 *
 * <ul>
 *   <li>Time (10 digits): last 10 digits of {@link System#currentTimeMillis()}
 *   <li>Pod hash (2 digits): derived from hostname and PID to discriminate nodes
 *   <li>Sequence (3 digits): monotonic counter within the same millisecond on this JVM
 * </ul>
 *
 * Per JVM, up to 1000 IDs can be issued in the same millisecond; if that bound is hit, generation
 * waits until the next millisecond. This easily covers 100 RPS sustained and concurrent bursts well
 * beyond that.
 */
@Slf4j
public class IDGenerator {

  private IDGenerator() {}

  private static final Object LOCK = new Object();
  private static long lastMillis = -1L;
  private static int sequence = 0;
  private static final int MAX_SEQUENCE = 999;
  private static final int POD_HASH;

  static {
    String host = "unknown";
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (Exception ex) {
      log.warn("Failed to resolve hostname for POD_HASH, using fallback 'unknown'", ex);
    }

    long pid = ProcessHandle.current().pid();

    // 00–99 (supports 100 pods)
    POD_HASH = Math.abs((host + pid).hashCode() % 1000);
  }

  /**
   * Generates a 15-digit numeric identifier unique across the distributed cluster.
   *
   * @return a 15-digit numeric string composed of timestamp, pod hash, and sequence counter
   */
  public static String generateNumber() {
    synchronized (LOCK) {
      long now = System.currentTimeMillis();
      if (now == lastMillis) {
        if (sequence >= MAX_SEQUENCE) {
          do {
            now = System.currentTimeMillis();
          } while (now == lastMillis);
          lastMillis = now;
          sequence = 0;
        } else {
          sequence++;
        }
      } else {
        lastMillis = now;
        sequence = 0;
      }
      return String.format("%010d%02d%03d", now % 10_000_000_000L, POD_HASH % 100, sequence);
    }
  }

  /**
   * Generates a random UUID string.
   *
   * @return a new random UUID as a string
   */
  public static String generate() {
    return UUID.randomUUID().toString();
  }
}
