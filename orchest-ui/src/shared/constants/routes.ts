/**
 * Application route constants
 * Centralized route paths for navigation and routing
 *
 * @example
 * ```typescript
 * import { ROUTES } from '@/shared/constants';
 * navigate(ROUTES.PROCESSES);
 * ```
 */
export const ROUTES = {
  HOME: '/',
  CALLBACK: '/callback',
  AUTH_PREFIX: '/auth/',
  DASHBOARD: '/dashboard',
  DASHBOARD_OLD: '/dashboard-old',
  PROCESSES: '/processes',
  DECISIONS: '/decisions',
  TASKS: '/tasks',
  SETTINGS: '/settings',
  MODELER: '/modeler',
  GENERATOR: '/generator',
  FEEL_PLAYGROUND: '/feel-playground',
  AI_CHAT: '/ai-chat',
  AUDIT_TRAIL: '/audit-trail',
  ACCESS_TOKENS: '/access-tokens',
  USAGE: '/usage',
  APPROVALS: '/approvals',
  ALERTS: '/alerts',
} as const;

export const isAuthRoute = (path: string): boolean => {
  return path === ROUTES.CALLBACK || path.startsWith(ROUTES.AUTH_PREFIX);
};

export const isHomeRoute = (path: string): boolean => {
  return path === ROUTES.HOME;
};
