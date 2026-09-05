import { environment } from '@/shared/constants/environment';

const NO_LOGIN_MODES = new Set(['development', 'local', 'dev', 'beta', 'teststable', 'uat', 'prod-ref']);

/**
 * True when mock/no-SSO tooling should apply: explicit VITE_BYPASS_LOGIN, or a
 * "dev-style" VITE_MODE. VITE_BYPASS_LOGIN alone is enough to skip real login.
 */
export const isNoLoginMode = (): boolean => {
  if (environment.bypassLogin) return true;
  return NO_LOGIN_MODES.has(environment.mode);
};

export const getCurrentEnvironmentMode = (): string => {
  return environment.mode || 'unknown';
};
