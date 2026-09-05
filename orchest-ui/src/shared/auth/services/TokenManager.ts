import { environment } from '@/shared/constants/environment'
import { STORAGE_KEYS } from '@/shared/constants/storageConstants'
import { TIMING } from '@/shared/constants/ui.config'
import { globalErrorHandler } from '@/shared/error/globalErrorHandler'
import {
    AccountInfo,
    AuthenticationResult,
    InteractionRequiredAuthError,
    PublicClientApplication,
    SilentRequest,
} from '@azure/msal-browser'
import { AuthErrorHandler, AuthErrorType } from '../utils/authErrorHandler'
import { getDevTokenSafe } from '../utils/devTokenGenerator'
import { getTokenExpiryTime, isTokenExpiredOrExpiringSoon } from '../utils/jwtUtils'
import { withTimeout } from '../utils/withTimeout'
import { SecureStorage } from './SecureStorage'
import { tokenFailureDetector } from './TokenFailureDetector'

export class TokenManager {
  private static instance: TokenManager | null = null
  private msalInstance: PublicClientApplication | null = null
  private activeRefresh: Promise<string | null> | null = null

  private constructor() {}

  public static getInstance(): TokenManager {
    if (!this.instance) {
      this.instance = new TokenManager()
    }
    return this.instance
  }

  public setMsalInstance(instance: PublicClientApplication): void {
    this.msalInstance = instance
  }

  public getActiveAccount(): AccountInfo | null {
    return this.msalInstance?.getActiveAccount() ?? null
  }

  public setActiveAccount(account: AccountInfo): void {
    this.msalInstance?.setActiveAccount(account)
  }

  private storeTokensFromResponse(response: AuthenticationResult): void {
    const now = Date.now()
    const expiresAt = getTokenExpiryTime(response.accessToken)
    const expiryMinutes = Math.max(Math.floor((expiresAt - now) / 60000), 1)

    SecureStorage.set(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken, { expiryMinutes })
    SecureStorage.set(STORAGE_KEYS.ID_TOKEN, response.idToken, { expiryMinutes })
  }

  /**
   * Get access token for API requests
   * @throws {Error} When token cannot be acquired (network error, MSAL unavailable, expired session)
   * @returns {Promise<string>} Valid access token
   */
  public async getAccessToken(): Promise<string> {
    if (environment.bypassLogin) {
      const devToken = getDevTokenSafe()
      if (!devToken) {
        throw new Error('Dev token not available in bypass mode')
      }
      return devToken
    }

    const cachedToken = SecureStorage.get<string>(STORAGE_KEYS.ID_TOKEN)
    if (cachedToken && !isTokenExpiredOrExpiringSoon(cachedToken, 1)) {
      tokenFailureDetector.recordSuccess()
      return cachedToken
    }

    // Use MSAL cache first (forceRefresh: false). Forcing refresh on every
    // SecureStorage miss caused cold starts to hang on hidden-iframe renew.
    const token = await this.acquireTokenSilent(undefined, false)
    if (token) {
      const idToken = SecureStorage.get<string>(STORAGE_KEYS.ID_TOKEN)
      if (idToken) {
        tokenFailureDetector.recordSuccess()
        return idToken
      }
      tokenFailureDetector.recordSuccess()
      return token
    }

    tokenFailureDetector.recordFailure('get-access-token-failed')
    throw new Error('Failed to acquire access token. Please log in again.')
  }

  public async acquireTokenSilent(request?: SilentRequest, forceRefresh = false): Promise<string | null> {
    if (environment.bypassLogin) {
      return getDevTokenSafe()
    }

    if (!this.msalInstance) {
      this.handleError('MSAL instance not available', 'token-acquisition')
      tokenFailureDetector.recordFailure('msal-not-available')
      return null
    }

    // If refresh is already in progress, wait for it
    if (this.activeRefresh) {
      return this.activeRefresh
    }

    try {
      const account = this.getAccountForRequest(request)
      if (!account) {
        tokenFailureDetector.recordFailure('no-account')
        return null
      }

      const cachedToken = this.checkCachedToken(forceRefresh)
      if (cachedToken) {
        tokenFailureDetector.recordSuccess()
        return cachedToken
      }

      // Start refresh and store promise
      this.activeRefresh = this.performTokenRefresh(request, account, forceRefresh || !!cachedToken)
      const result = await this.activeRefresh

      if (result) {
        tokenFailureDetector.recordSuccess()
      } else {
        tokenFailureDetector.recordFailure('token-refresh-returned-null')
      }

      return result
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        // Don't count interaction required as failure - it's expected behavior
        return await this.handleInteractionRequired()
      }

      const err = this.toError(error)
      this.handleError(err.message, 'token-acquisition', err)
      tokenFailureDetector.recordFailure('token-acquisition-error')
      return null
    } finally {
      this.activeRefresh = null
    }
  }

  private async performTokenRefresh(
    request: SilentRequest | undefined,
    account: AccountInfo,
    forceRefresh: boolean
  ): Promise<string | null> {
    const tokenRequest = this.buildTokenRequest(request, account, forceRefresh)
    const response = await this.fetchTokenWithRetry(tokenRequest)
    return await this.handleTokenResponse(response, account)
  }

  private getAccountForRequest(request?: SilentRequest): AccountInfo | null {
    const account = request?.account || this.msalInstance?.getActiveAccount()
    if (!account) {
      this.handleError('No active account available', 'token-acquisition')
    }
    return account ?? null
  }

  private checkCachedToken(forceRefresh: boolean): string | null {
    if (forceRefresh) return null
    const cachedToken = SecureStorage.get<string>(STORAGE_KEYS.ACCESS_TOKEN)
    return (cachedToken && !isTokenExpiredOrExpiringSoon(cachedToken, 1)) ? cachedToken : null
  }

  private buildTokenRequest(request: SilentRequest | undefined, account: AccountInfo, forceRefresh: boolean): SilentRequest {
    return request || { scopes: [...environment.auth.scopes], account, forceRefresh }
  }

  private async fetchTokenWithRetry(tokenRequest: SilentRequest): Promise<AuthenticationResult> {
    return withTimeout(
      this.retryWithExponentialBackoff(() => this.msalInstance!.acquireTokenSilent(tokenRequest), 2, 1000),
      TIMING.AUTH_INIT_TIMEOUT,
      'Silent token acquisition timed out'
    )
  }

  private async handleTokenResponse(response: AuthenticationResult, account: AccountInfo): Promise<string> {
    if (isTokenExpiredOrExpiringSoon(response.accessToken, 0)) {
      return await this.refreshExpiredToken(account)
    }

    this.storeTokensFromResponse(response)
    return response.accessToken
  }

  private async refreshExpiredToken(account: AccountInfo): Promise<string> {
    const retryResponse = await this.msalInstance!.acquireTokenSilent({
      scopes: [...environment.auth.scopes],
      account,
      forceRefresh: true,
    })

    if (isTokenExpiredOrExpiringSoon(retryResponse.accessToken, 0)) {
      throw new Error('MSAL returned expired token even after forced refresh')
    }

    this.storeTokensFromResponse(retryResponse)
    return retryResponse.accessToken
  }

  private async handleInteractionRequired(): Promise<string | null> {
    try {
      const response = await this.msalInstance!.acquireTokenPopup({
        scopes: [...environment.auth.scopes],
      })

      this.storeTokensFromResponse(response)
      return response.accessToken
    } catch (popupError) {
      const err = this.toError(popupError)
      this.handleError(err.message, 'token-popup-acquisition', err)
      return null
    }
  }

  private async retryWithExponentialBackoff<T>(
    fn: () => Promise<T>,
    maxRetries: number,
    baseDelay: number
  ): Promise<T> {
    let lastError: Error | unknown

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        return await fn()
      } catch (error) {
        lastError = error

        if (error instanceof InteractionRequiredAuthError) {
          throw error
        }

        if (attempt < maxRetries) {
          const delay = baseDelay * Math.pow(2, attempt)
          await new Promise(resolve => setTimeout(resolve, delay))
        }
      }
    }

    throw lastError
  }

  private handleError(message: string, context: string, error?: Error): void {
    globalErrorHandler.handleError(error || new Error(message), 'auth', { context })
  }

  private toError(error: unknown): Error {
    return error instanceof Error ? error : new Error(String(error))
  }

  public async login(): Promise<boolean> {
    if (!this.msalInstance) {
      this.handleError('MSAL instance not available', 'login')
      tokenFailureDetector.recordFailure('login-msal-not-available')
      return false
    }

    try {
      const response = await this.msalInstance.loginPopup({
        scopes: [...environment.auth.scopes],
      })

      if (response?.account) {
        this.msalInstance.setActiveAccount(response.account)
        this.storeTokensFromResponse(response)
        tokenFailureDetector.clearAll() // Clear all failure history on successful login
        return true
      }
      tokenFailureDetector.recordFailure('login-no-account')
      return false
    } catch (error) {
      AuthErrorHandler.handleError({
        type: AuthErrorType.LOGIN_ERROR,
        message: 'Login failed',
        originalError: this.toError(error),
      })
      tokenFailureDetector.recordFailure('login-error')
      return false
    }
  }

  public async logout(): Promise<void> {
    // SECURITY: Clear all stored tokens and data
    SecureStorage.clearAll(true) // Include mock data

    // Clear MSAL cache - await account removal to ensure sessionStorage is cleaned
    if (this.msalInstance) {
      const accounts = this.msalInstance.getAllAccounts()
      for (const account of accounts) {
        try {
          await this.msalInstance.removeAccount(account)
        } catch {
          // Continue cleanup even if individual account removal fails
        }
      }
    }

    // CRITICAL: Clear MSAL's own sessionStorage entries
    // SecureStorage.clearAll() only clears 'orchest_' prefixed keys,
    // but MSAL stores auth cache under its own keys (e.g. 'msal.<clientId>.*')
    this.clearMsalSessionStorage()

    // Clear any active refresh promises
    this.activeRefresh = null

    // Clear token failure detector state
    tokenFailureDetector.clearAll()

    // Call backend logout endpoint if available
    this.notifyBackendLogout().catch(() => {
      // Ignore errors during logout notification
    })

    if (environment.bypassLogin) return

    if (this.msalInstance) {
      try {
        await this.msalInstance.logoutRedirect({
          postLogoutRedirectUri: environment.auth.logoutRedirectUri,
        })
      } catch (error) {
        // If logoutRedirect fails (e.g. interaction in progress),
        // force redirect to login page as fallback
        this.handleError(
          'logoutRedirect failed, forcing manual redirect',
          'logout',
          error instanceof Error ? error : new Error(String(error))
        )
        window.location.href = environment.auth.logoutRedirectUri || '/login'
      }
    }
  }

  /**
   * Clears all MSAL-related entries from sessionStorage.
   * MSAL stores cache under keys like 'msal.<clientId>.*' which are
   * not covered by SecureStorage.clearAll() (orchest_ prefix only).
   */
  private clearMsalSessionStorage(): void {
    try {
      const keysToRemove = Object.keys(sessionStorage).filter(
        key => key.startsWith('msal.') || key.startsWith('msal-')
      )
      keysToRemove.forEach(key => sessionStorage.removeItem(key))
    } catch {
      // Silently fail - best effort cleanup
    }
  }

  /**
   * Notifies backend of logout for server-side token revocation
   * SECURITY: Ensures tokens are invalidated server-side
   */
  private async notifyBackendLogout(): Promise<void> {
    try {
      // Use fetch directly to avoid httpClient interceptors
      await fetch(`${environment.apiUrl}/auth/logout`, {
        method: 'POST',
        credentials: 'include', // Include cookies if any
        headers: {
          'Content-Type': 'application/json',
        },
      })
    } catch {
      // Silently fail - logout should proceed even if backend call fails
    }
  }

  public getStoredToken(key: string): string | null {
    return SecureStorage.get<string>(key)
  }

  public storeToken(key: string, token: string, expiryMinutes = 60): void {
    SecureStorage.set(key, token, { expiryMinutes })
  }

  public removeToken(key: string): void {
    SecureStorage.remove(key)
  }
}

export const tokenManager = TokenManager.getInstance()
