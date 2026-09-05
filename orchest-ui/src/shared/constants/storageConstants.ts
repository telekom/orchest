/**
 * Storage keys for localStorage and sessionStorage
 *
 * All UI state keys are prefixed with 'orchest-ui-' to avoid conflicts
 * with other applications in the same domain.
 *
 * @example
 * ```typescript
 * localStorage.setItem(STORAGE_KEYS.THEME, 'dark');
 * const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
 * ```
 */
export const STORAGE_KEYS = {
  // Authentication tokens
  ACCESS_TOKEN: 'access_token',
  ID_TOKEN: 'id_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_DETAILS: 'user_details',
  TOKEN_EXPIRY: 'token_expiry',

  // UI State
  THEME: 'orchest-ui-theme',
  SIDEBAR_STATE: 'orchest-ui-sidebar',
  LAST_FILTERS: 'orchest-ui-filters',
  USER_PREFERENCES: 'orchest-ui-preferences',
  CACHED_DIAGRAMS: 'orchest-ui-diagrams',

  // Modeler State
  MODELER_PROPERTIES_PANEL: 'orchest-ui-modeler-properties-panel',

  // Authentication Redirect
  REDIRECT_AFTER_LOGIN: 'orchest-ui-redirect-after-login',
} as const;

export type StorageKeyType = typeof STORAGE_KEYS[keyof typeof STORAGE_KEYS];
