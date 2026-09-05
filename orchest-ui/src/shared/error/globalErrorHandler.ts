import { AxiosError } from 'axios';
import { toast } from '@/design-system/components/ui/sonner';
import { ERROR_HANDLER_CONFIG } from '@/shared/constants/apiConfig';
import { HTTP_STATUS, TIMING } from '@/shared/constants';
import { logger } from '@/shared/utils/logger';

export interface AppError {
  id: string;
  type: 'api' | 'validation' | 'network' | 'auth' | 'runtime' | 'unknown';
  code?: string;
  message: string;
  details?: string;
  stack?: string;
  timestamp: string;
  url?: string;
  method?: string;
  status?: number;
  severity: 'low' | 'medium' | 'high' | 'critical';
  context?: Record<string, unknown>;
}

interface ErrorHandlerConfig {
  enableToasts: boolean;
  suppressPatterns: RegExp[];
}

const DEFAULT_SUPPRESSED_PATTERNS: RegExp[] = [
  /no\s+definitions/i,
  /no\s+bpmn\s+definitions/i,
  /definitions\s+not\s+loaded/i,
];

class GlobalErrorHandler {
  private config: ErrorHandlerConfig = {
    enableToasts: true,
    suppressPatterns: DEFAULT_SUPPRESSED_PATTERNS,
  };

  private toastCounts = new Map<string, number>();
  private errorHistory: AppError[] = [];
  private listeners: Set<(error: AppError) => void> = new Set();

  constructor() {
    this.setupGlobalHandlers();
  }

  configure(config: Partial<ErrorHandlerConfig>): void {
    this.config = { ...this.config, ...config };
  }

  handleApiError(error: AxiosError, context?: Record<string, unknown>): AppError {
    const appError = this.createAppError(error, 'api', context);
    return this.processError(appError);
  }

  handleError(error: Error, type: AppError['type'] = 'runtime', context?: Record<string, unknown>): AppError {
    const appError = this.createAppError(error, type, context);
    return this.processError(appError);
  }

  private createAppError(error: Error | AxiosError, type: AppError['type'], context?: Record<string, unknown>): AppError {
    const appError: AppError = {
      id: this.generateId('error'),
      type,
      message: this.sanitizeErrorMessage(error.message || 'Unknown error occurred'),
      timestamp: new Date().toISOString(),
      severity: 'medium',
      context,
    };

    if (this.isAxiosError(error)) {
      const { response, config: reqConfig, code } = error;
      Object.assign(appError, {
        status: response?.status,
        code,
        url: reqConfig?.url,
        method: reqConfig?.method?.toUpperCase(),
        details: this.extractErrorDetails(error),
        ...this.categorizeApiError(error),
      });
    } else {
      // SECURITY: Only include stack trace in development mode
      appError.stack = import.meta.env.DEV ? error.stack : undefined;
      appError.details = error.name;
    }

    return appError;
  }

  private processError(error: AppError): AppError {
    this.errorHistory.push(error);
    if (this.errorHistory.length > 100) {
      this.errorHistory.shift();
    }

    this.listeners.forEach(listener => listener(error));

    this.logError(error);

    if (this.config.enableToasts) {
      this.showErrorToast(error);
    }

    if (error.type === 'auth' && error.status === HTTP_STATUS.UNAUTHORIZED) {
      this.handleAuthRedirect();
    }

    return error;
  }

  private categorizeApiError(error: AxiosError): Partial<AppError> {
    const status = error.response?.status;

    if (!status) {
      return {
        type: 'network',
        severity: 'high',
        message: 'Network connection failed',
      };
    }

    const statusCategories: Record<number, Partial<AppError>> = {
      [HTTP_STATUS.UNAUTHORIZED]: { type: 'auth', severity: 'high', message: 'Authentication required' },
      [HTTP_STATUS.FORBIDDEN]: { type: 'auth', severity: 'medium', message: 'Access denied' },
      [HTTP_STATUS.NOT_FOUND]: { type: 'api', severity: 'medium', message: 'Resource not found' },
      [HTTP_STATUS.VALIDATION_FAILED]: { type: 'validation', severity: 'low', message: 'Validation failed' },
      [HTTP_STATUS.RATE_LIMITED]: { type: 'api', severity: 'medium', message: 'Rate limit exceeded' },
    };

    if (statusCategories[status]) {
      return statusCategories[status];
    }

    if (status >= 500) {
      return { type: 'api', severity: 'critical', message: 'Server error occurred' };
    }

    return { type: 'api', severity: 'medium' };
  }

  /**
   * SECURITY: Extract error details while sanitizing sensitive information
   * Removes stack traces, file paths, and implementation details from error messages
   */
  private extractErrorDetails(error: AxiosError): string {
    const { data } = error.response || {};
    if (!data) return this.sanitizeErrorMessage(error.message);

    const errorString = typeof data === 'object'
      ? JSON.stringify(data, null, 2)
      : String(data);

    return this.sanitizeErrorMessage(errorString);
  }

  /**
   * SECURITY: Sanitize error message to prevent information disclosure
   * Removes:
   * - File paths (e.g., /src/components/Foo.tsx, C:\Users\...)
   * - Stack traces
   * - Database queries
   * - Environment variables
   * - Internal IP addresses
   * - Version numbers
   * - Implementation details
   */
  private sanitizeErrorMessage(message: string): string {
    if (!message) return 'An error occurred';

    // Remove file paths (Unix and Windows)
    let sanitized = message.replace(/(?:\/[\w.-]+)+\/?/g, '[PATH]');
    sanitized = sanitized.replace(/[A-Z]:\\(?:[\w.-]+\\)*/g, '[PATH]');

    // Remove stack traces
    sanitized = sanitized.replace(/at\s+[\w.<>]+\s+\([^)]+\)/g, '');
    sanitized = sanitized.replace(/^\s*at\s+.+$/gm, '');

    // Remove line numbers and column references
    sanitized = sanitized.replace(/:\d+:\d+/g, '');

    // Remove SQL queries
    sanitized = sanitized.replace(/SELECT\s+.+FROM\s+.+/gi, '[SQL_QUERY]');
    sanitized = sanitized.replace(/INSERT\s+INTO\s+.+/gi, '[SQL_QUERY]');
    sanitized = sanitized.replace(/UPDATE\s+.+SET\s+.+/gi, '[SQL_QUERY]');
    sanitized = sanitized.replace(/DELETE\s+FROM\s+.+/gi, '[SQL_QUERY]');

    // Remove environment variable references
    sanitized = sanitized.replace(/\${?\w+}?/g, '[ENV_VAR]');

    // Remove IP addresses (IPv4 and IPv6)
    sanitized = sanitized.replace(/\b(?:\d{1,3}\.){3}\d{1,3}\b/g, '[IP]');
    sanitized = sanitized.replace(/\b(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\b/g, '[IP]');

    // Remove version numbers
    sanitized = sanitized.replace(/v?\d+\.\d+\.\d+/g, '[VERSION]');

    // Remove sensitive keywords that might reveal implementation
    const sensitivePatterns = [
      /database/gi,
      /connection\s+string/gi,
      /password/gi,
      /secret/gi,
      /token/gi,
      /api[_\s]?key/gi,
      /authorization/gi,
    ];

    sensitivePatterns.forEach(pattern => {
      sanitized = sanitized.replace(pattern, '[REDACTED]');
    });

    // Remove multiple consecutive whitespace/newlines
    sanitized = sanitized.replace(/\n{3,}/g, '\n\n');
    sanitized = sanitized.replace(/\s{3,}/g, ' ');

    // Trim and return
    return sanitized.trim() || 'An error occurred';
  }

  private shouldSuppressError(error: AppError): boolean {
    if (error.type !== 'runtime') return false;
    return this.config.suppressPatterns.some(pattern => pattern.test(error.message));
  }

  private logError(error: AppError): void {
    if (this.shouldSuppressError(error)) return;

    const logData = { ...error, stack: import.meta.env.DEV ? error.stack : undefined };
    const severityLoggers: Record<string, () => void> = {
      critical: () => logger.error('🚨 Critical Error:', logData),
      high: () => logger.error('❌ High Severity Error:', logData),
      medium: () => logger.warn('⚠️ Medium Severity Error:', logData),
      low: () => logger.info('ℹ️ Low Severity Error:', logData),
    };

    severityLoggers[error.severity]?.();
  }

  private showErrorToast(error: AppError): void {
    if (this.shouldSuppressError(error)) return;
    if (!this.shouldShowToast(error)) return;

    const message = this.getUserFriendlyMessage(error);

    const toastOptions: Record<string, () => void> = {
      critical: () => toast.error(message, { duration: TIMING.TOAST_ERROR_DURATION }),
      high: () => toast.error(message, { duration: TIMING.TOAST_ERROR_DURATION }),
      medium: () => toast.warning(message, { duration: TIMING.TOAST_WARNING_DURATION }),
    };

    toastOptions[error.severity]?.();
  }

  private getUserFriendlyMessage(error: AppError): string {
    const messages: Record<string, string> = {
      'Network connection failed': 'Please check your internet connection.',
      'Authentication required': 'Please log in.',
      'Access denied': 'You do not have permission for this action.',
      'Resource not found': 'The requested resource was not found.',
      'Validation failed': 'Please check your input.',
      'Rate limit exceeded': 'Too many requests. Please wait a moment.',
      'Server error occurred': 'A server error occurred. We are working on a solution.',
    };

    return messages[error.message] || error.message || 'An unexpected error occurred.';
  }

  private shouldShowToast(error: AppError): boolean {
    const key = `${error.type}:${error.code || error.message}`;
    const count = this.toastCounts.get(key) || 0;

    if (count >= ERROR_HANDLER_CONFIG.MAX_TOAST_REPETITIONS) {
      return false;
    }

    this.toastCounts.set(key, count + 1);

    setTimeout(() => {
      this.toastCounts.delete(key);
    }, ERROR_HANDLER_CONFIG.ERROR_COUNT_RESET_WINDOW);

    return true;
  }

  private handleAuthRedirect(): void {
    setTimeout(() => {
      window.location.href = '/login';
    }, TIMING.NAVIGATION_REDIRECT_DELAY);
  }

  private setupGlobalHandlers(): void {
    window.addEventListener('unhandledrejection', (event) => {
      this.handleError(new Error(event.reason), 'runtime', {
        type: 'unhandled_promise_rejection',
      });
      event.preventDefault();
    });

    window.addEventListener('error', (event) => {
      this.handleError(event.error || new Error(event.message), 'runtime', {
        type: 'uncaught_error',
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
      });
    });
  }

  private isAxiosError(error: unknown): error is AxiosError {
    return !!(error as AxiosError)?.isAxiosError;
  }

  private generateId(prefix: string): string {
    if (crypto.randomUUID) {
      return `${prefix}_${crypto.randomUUID()}`;
    }

    const array = new Uint8Array(16);
    crypto.getRandomValues(array);
    const hex = Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
    return `${prefix}_${Date.now()}_${hex.substring(0, 12)}`;
  }

  getErrorHistory(): AppError[] {
    return [...this.errorHistory];
  }

  getErrorStats() {
    const totalErrors = this.errorHistory.length;
    const errorsByType = this.errorHistory.reduce((acc, error) => {
      acc[error.type] = (acc[error.type] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    return { totalErrors, errorsByType };
  }

  clearErrorHistory(): void {
    this.errorHistory = [];
  }

  addListener(listener: (error: AppError) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }
}

export const globalErrorHandler = new GlobalErrorHandler();
export default globalErrorHandler;
