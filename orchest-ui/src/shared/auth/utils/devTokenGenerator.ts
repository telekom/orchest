/**
 * Development Token Generator
 * Generates mock JWT tokens when VITE_BYPASS_LOGIN is enabled.
 */

import { environment } from '@/shared/constants/environment'
import { globalErrorHandler } from '@/shared/error/globalErrorHandler'
import { logger } from '@/shared/utils/logger'

export function generateDevToken(): string {
  if (!environment.bypassLogin) {
    throw new Error('SECURITY: Dev token generation requires VITE_BYPASS_LOGIN=true')
  }
  if (environment.mode === 'prod') {
    logger.warn(
      'VITE_BYPASS_LOGIN is enabled while VITE_MODE is prod — confirm this is intentional.'
    )
  }

  const now = Math.floor(Date.now() / 1000)
  const payload = {
    sub: 'dev-user-local',
    name: 'Dev User',
    email: 'dev@localhost',
    roles: ['DEV_ONLY'],
    iat: now,
    exp: now + 3600,
  }

  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  const signature = btoa(
    Array.from(
      new TextEncoder().encode(`${header}.${body}.dev-mode-secret`)
    ).map(b => String.fromCharCode(b)).join('')
  ).substring(0, 43)

  return `${header}.${body}.${signature}`
}

export function getDevTokenSafe(): string | null {
  try {
    return generateDevToken()
  } catch (error) {
    globalErrorHandler.handleError(
      error instanceof Error ? error : new Error(String(error)),
      'auth',
      { context: 'dev-token-generation' }
    )
    return null
  }
}
