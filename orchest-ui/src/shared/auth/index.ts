export { ACCESS_FREE_PATHS, AuthProvider, PATH_PERMISSIONS, useAuth } from './context/AuthContext';
export type { AuthContextType } from './context/AuthContext';

export { useRoles } from './hooks/useRoles';
export { useAuthRedirect } from './hooks/useAuthRedirect';

export { RouteGuard } from './guards/RouteGuard/RouteGuard';

export { AuthorizationService } from './services/AuthorizationService';
export { tokenManager } from './services/TokenManager';

export { MOCK_ROLES_STORAGE_KEY, ROLE_PRIORITY_ORDER, UserRoles, roleHierarchy } from './models/roles';

export { AuthErrorHandler, AuthErrorType } from './utils/authErrorHandler';
export type { AuthError } from './utils/authErrorHandler';
export { UserAuthUtils } from './utils/userAuthUtils';
export type { UserDetails } from './utils/userAuthUtils';

export { default as Login } from './pages/Login/Login';
export { default as NoRoles } from './pages/NoRoles/NoRoles';

export { getCurrentEnvironmentMode, isNoLoginMode } from '../utils/environmentUtils';

export * from './constants';
