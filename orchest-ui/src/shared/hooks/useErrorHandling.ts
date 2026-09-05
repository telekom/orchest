import { AppError, globalErrorHandler } from '@/shared/error/globalErrorHandler';
import { useCallback } from 'react';

export type ErrorSeverity = 'low' | 'medium' | 'high' | 'critical';
export type ErrorType = 'api' | 'validation' | 'network' | 'auth' | 'runtime' | 'unknown';

export interface UseErrorHandlingOptions {
  /** Default context for all errors handled by this hook */
  context: string;
  /** Default severity level */
  defaultSeverity?: ErrorSeverity;
  /** Default error type */
  defaultType?: ErrorType;
}

export interface ErrorContext extends Record<string, unknown> {
  severity?: ErrorSeverity;
}

export interface UseErrorHandlingReturn {
  /** Convert unknown error to Error instance */
  toError: (error: unknown) => Error;
  /** Handle error with globalErrorHandler */
  handleError: (error: unknown, additionalContext?: ErrorContext) => AppError;
  /** Wrap async function with error handling */
  withErrorHandling: <T>(
    asyncFn: () => Promise<T>,
    additionalContext?: ErrorContext
  ) => Promise<T | null>;
  /** Create a try-catch wrapper for sync functions */
  tryCatch: <T>(fn: () => T, fallback: T, additionalContext?: ErrorContext) => T;
}

/**
 * Converts any unknown error value to an Error instance
 */
export const toError = (error: unknown): Error => {
  if (error instanceof Error) return error;
  if (typeof error === 'string') return new Error(error);
  if (error && typeof error === 'object' && 'message' in error) {
    return new Error(String((error as { message: unknown }).message));
  }
  return new Error(String(error));
};

/**
 * Hook for centralized error handling with context
 * 
 * @example
 * ```tsx
 * const { handleError, withErrorHandling } = useErrorHandling({
 *   context: 'diagram-viewer',
 *   defaultSeverity: 'medium'
 * });
 * 
 * // Direct error handling
 * catch (error) {
 *   handleError(error, { action: 'import' });
 * }
 * 
 * // Async wrapper
 * const result = await withErrorHandling(
 *   () => fetchData(),
 *   { severity: 'high' }
 * );
 * ```
 */
export function useErrorHandling(options: UseErrorHandlingOptions): UseErrorHandlingReturn {
  const { context, defaultSeverity = 'medium', defaultType = 'runtime' } = options;

  const handleError = useCallback(
    (error: unknown, additionalContext?: ErrorContext): AppError => {
      const errorInstance = toError(error);
      const severity = additionalContext?.severity ?? defaultSeverity;
      
      return globalErrorHandler.handleError(errorInstance, defaultType, {
        context,
        severity,
        ...additionalContext,
      });
    },
    [context, defaultSeverity, defaultType]
  );

  const withErrorHandling = useCallback(
    async <T>(
      asyncFn: () => Promise<T>,
      additionalContext?: ErrorContext
    ): Promise<T | null> => {
      try {
        return await asyncFn();
      } catch (error) {
        handleError(error, additionalContext);
        return null;
      }
    },
    [handleError]
  );

  const tryCatch = useCallback(
    <T>(fn: () => T, fallback: T, additionalContext?: ErrorContext): T => {
      try {
        return fn();
      } catch (error) {
        handleError(error, additionalContext);
        return fallback;
      }
    },
    [handleError]
  );

  return {
    toError,
    handleError,
    withErrorHandling,
    tryCatch,
  };
}

export default useErrorHandling;
