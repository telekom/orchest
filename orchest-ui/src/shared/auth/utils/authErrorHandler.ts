import { AppError, globalErrorHandler } from '@/shared/error/globalErrorHandler'

export enum AuthErrorType {
  INITIALIZATION_ERROR = 'INITIALIZATION_ERROR',
  LOGIN_ERROR = 'LOGIN_ERROR',
  LOGOUT_ERROR = 'LOGOUT_ERROR',
  TOKEN_REFRESH = 'TOKEN_REFRESH',
  TOKEN_VALIDATION = 'TOKEN_VALIDATION',
  UNAUTHORIZED = 'UNAUTHORIZED',
  PERMISSION_ERROR = 'PERMISSION_ERROR',
  ROLE_ERROR = 'ROLE_ERROR',
}

export interface AuthError {
  type: AuthErrorType
  message: string
  originalError: Error
  context?: Record<string, unknown>
}

export class AuthErrorHandler {
  static handleError(error: AuthError): AppError {
    return globalErrorHandler.handleError(error.originalError, 'auth', {
      authErrorType: error.type,
      ...error.context,
    })
  }

  static handleErrorSilent(error: AuthError): void {
    const originalConfig = globalErrorHandler.getConfig()
    globalErrorHandler.configure({ enableToasts: false })

    this.handleError(error)

    globalErrorHandler.configure(originalConfig)
  }
}
