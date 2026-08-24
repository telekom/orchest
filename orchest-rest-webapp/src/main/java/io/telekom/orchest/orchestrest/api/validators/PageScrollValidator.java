package io.telekom.orchest.orchestrest.api.validators;

import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Validates scroll/offset pagination parameters for list endpoints. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageScrollValidator {

  private static final int MAX_SCROLL_WINDOW = 500;

  /**
   * Validates that the scroll window parameters are within acceptable bounds.
   *
   * @param from inclusive start offset (must be non-negative)
   * @param toExclusive exclusive end offset (must be greater than from, window max 500)
   * @throws RestExceptions if validation fails
   */
  public static void validateScroll(int from, int toExclusive) {
    if (from < 0) {
      throw new RestExceptions("from must be >= 0", 400);
    }
    if (toExclusive <= from) {
      throw new RestExceptions("to must be greater than from", 400);
    }
    int windowSize = toExclusive - from;
    if (windowSize > MAX_SCROLL_WINDOW) {
      throw new RestExceptions(
          "scroll window (to - from) must not exceed " + MAX_SCROLL_WINDOW, 400);
    }
  }
}
