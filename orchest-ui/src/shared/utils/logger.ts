/**
 * Logger utility that conditionally logs based on environment
 * In production, logging is disabled by default
 */

type LogLevel = 'log' | 'info' | 'warn' | 'error' | 'debug';

interface LoggerConfig {
  enabledInProduction: boolean;
  minLevel: LogLevel;
}

const LOG_LEVELS: Record<LogLevel, number> = {
  debug: 0,
  log: 1,
  info: 2,
  warn: 3,
  error: 4,
};

class Logger {
  private config: LoggerConfig = {
    enabledInProduction: false,
    minLevel: 'debug',
  };

  private isDevelopment(): boolean {
    return import.meta.env.DEV;
  }

  private shouldLog(level: LogLevel): boolean {
    if (this.isDevelopment()) {
      return true;
    }

    if (!this.config.enabledInProduction) {
      return level === 'error';
    }

    return LOG_LEVELS[level] >= LOG_LEVELS[this.config.minLevel];
  }

  debug(...args: unknown[]): void {
    if (this.shouldLog('debug')) {
      console.debug('[DEBUG]', ...args);
    }
  }

  log(...args: unknown[]): void {
    if (this.shouldLog('log')) {
      console.log('[LOG]', ...args);
    }
  }

  info(...args: unknown[]): void {
    if (this.shouldLog('info')) {
      console.info('[INFO]', ...args);
    }
  }

  warn(...args: unknown[]): void {
    if (this.shouldLog('warn')) {
      console.warn('[WARN]', ...args);
    }
  }

  error(...args: unknown[]): void {
    if (this.shouldLog('error')) {
      console.error('[ERROR]', ...args);
    }
  }

  configure(config: Partial<LoggerConfig>): void {
    this.config = { ...this.config, ...config };
  }
}

export const logger = new Logger();
