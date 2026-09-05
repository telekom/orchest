/**
 * TokenFailureDetector Service
 *
 * Centralized mechanism to detect repeated token acquisition failures
 * and trigger auth state cleanup when failures exceed threshold.
 *
 * This prevents "zombie session" states where the UI shows the user
 * as authenticated but tokens cannot be acquired (e.g., network timeout,
 * MSAL server unavailable, expired sessions).
 */

import { logger } from '@/shared/utils/logger';

export interface TokenFailureHandler {
  (): void;
}

interface FailureMetrics {
  consecutiveFailures: number;
  lastFailureTime: number | null;
  totalFailures: number;
  totalSuccesses: number;
}

export class TokenFailureDetector {
  private metrics: FailureMetrics = {
    consecutiveFailures: 0,
    lastFailureTime: null,
    totalFailures: 0,
    totalSuccesses: 0,
  };

  private authFailureHandler: TokenFailureHandler | null = null;
  private readonly MAX_CONSECUTIVE_FAILURES: number;
  private readonly FAILURE_RESET_WINDOW_MS: number;

  /**
   * @param maxFailures - Number of consecutive failures before triggering auth failure (default: 3)
   * @param resetWindowMs - Time window after which failure count resets if no failures (default: 5 minutes)
   */
  constructor(
    maxFailures: number = 3,
    resetWindowMs: number = 5 * 60 * 1000
  ) {
    this.MAX_CONSECUTIVE_FAILURES = maxFailures;
    this.FAILURE_RESET_WINDOW_MS = resetWindowMs;
  }

  /**
   * Register a handler to be called when consecutive failures exceed threshold
   */
  public setAuthFailureHandler(handler: TokenFailureHandler): void {
    this.authFailureHandler = handler;
    logger.debug('[TokenFailureDetector] Auth failure handler registered');
  }

  /**
   * Record a token acquisition failure
   * Triggers auth failure handler if consecutive failures exceed threshold
   */
  public recordFailure(context?: string): void {
    this.metrics.consecutiveFailures++;
    this.metrics.totalFailures++;
    this.metrics.lastFailureTime = Date.now();

    const contextMsg = context ? ` (${context})` : '';
    logger.warn(
      `[TokenFailureDetector] Token failure recorded${contextMsg}`,
      `Consecutive failures: ${this.metrics.consecutiveFailures}/${this.MAX_CONSECUTIVE_FAILURES}`
    );

    if (this.metrics.consecutiveFailures >= this.MAX_CONSECUTIVE_FAILURES) {
      logger.error(
        '[TokenFailureDetector] Maximum consecutive failures reached',
        'Triggering auth failure handler to clear auth state',
        { metrics: this.getMetrics() }
      );
      this.triggerAuthFailure();
    }
  }

  /**
   * Record a successful token acquisition
   * Resets consecutive failure count
   */
  public recordSuccess(): void {
    const hadFailures = this.metrics.consecutiveFailures > 0;

    if (hadFailures) {
      logger.info(
        '[TokenFailureDetector] Token acquisition recovered',
        `After ${this.metrics.consecutiveFailures} consecutive failures`
      );
    }

    this.metrics.consecutiveFailures = 0;
    this.metrics.totalSuccesses++;
    this.metrics.lastFailureTime = null;
  }

  /**
   * Check if failures should be reset based on time window
   * Call this periodically or before checking failure status
   */
  public checkAndResetIfStale(): void {
    if (
      this.metrics.lastFailureTime &&
      Date.now() - this.metrics.lastFailureTime > this.FAILURE_RESET_WINDOW_MS
    ) {
      logger.info(
        '[TokenFailureDetector] Resetting failure count after time window',
        `Last failure was ${Math.round((Date.now() - this.metrics.lastFailureTime) / 1000 / 60)} minutes ago`
      );
      this.reset();
    }
  }

  /**
   * Get current failure metrics (for debugging/monitoring)
   */
  public getMetrics(): Readonly<FailureMetrics> {
    return { ...this.metrics };
  }

  /**
   * Reset all failure counters
   */
  public reset(): void {
    this.metrics.consecutiveFailures = 0;
    this.metrics.lastFailureTime = null;
  }

  /**
   * Completely clear all metrics (including totals)
   */
  public clearAll(): void {
    this.metrics = {
      consecutiveFailures: 0,
      lastFailureTime: null,
      totalFailures: 0,
      totalSuccesses: 0,
    };
    logger.debug('[TokenFailureDetector] All metrics cleared');
  }

  /**
   * Internal method to trigger the auth failure handler
   */
  private triggerAuthFailure(): void {
    this.reset();

    if (this.authFailureHandler) {
      try {
        this.authFailureHandler();
      } catch (error) {
        logger.error(
          '[TokenFailureDetector] Error in auth failure handler',
          error instanceof Error ? error.message : String(error)
        );
      }
    } else {
      logger.warn(
        '[TokenFailureDetector] No auth failure handler registered',
        'Cannot clear auth state automatically'
      );
    }
  }
}

/**
 * Singleton instance for application-wide token failure detection
 */
export const tokenFailureDetector = new TokenFailureDetector(3, 5 * 60 * 1000);
