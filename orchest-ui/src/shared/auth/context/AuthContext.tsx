import { environment } from '@/shared/constants/environment';
import { TIMING } from '@/shared/constants/ui.config';
import { useTokenRefresh } from '@/shared/hooks/useTokenRefresh';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import { AccountInfo, PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import {
    createContext,
    ReactNode,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useReducer,
} from 'react';
import { toast } from 'sonner';
import { createAuthAdapter, IAuthAdapter } from '../adapters';
import { useMockRoleListener } from '../hooks/useMockRoleListener';
import { UserRoles } from '../models/roles';
import { activeRoleService } from '../services/ActiveRoleService';
import { AuthorizationService } from '../services/AuthorizationService';
import { SecureStorage } from '../services/SecureStorage';
import { tokenFailureDetector } from '../services/TokenFailureDetector';
import { AuthErrorHandler, AuthErrorType } from '../utils/authErrorHandler';
import { UserDetails } from '../utils/userAuthUtils';
import { withTimeout } from '../utils/withTimeout';

export interface AuthContextType {
  user: UserDetails | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  rolesLoaded: boolean;
  login: () => Promise<boolean>;
  logout: () => Promise<void>;
  hasRole: (role: UserRoles, exactMatch?: boolean) => boolean;
  hasAnyRole: (roles: UserRoles[]) => boolean;
  hasAllRoles: (roles: UserRoles[]) => boolean;
  isRouteAccessible: (path: string) => boolean;
  refreshAuth: () => Promise<boolean>;
  getEffectiveRoles: () => string[];
  account: AccountInfo | null;
  roleVersion: number;
}

// eslint-disable-next-line react-refresh/only-export-components
export const ACCESS_FREE_PATHS = ['/no-roles', '/login', '/callback', '/dev', '/feel-playground'];

// eslint-disable-next-line react-refresh/only-export-components
export const PATH_PERMISSIONS: Record<string, UserRoles[]> = {
  '/dashboard': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/dashboard-old': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/processes': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/processes/': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/decisions': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/decisions/': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/tasks': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/tasks/': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/modeler': [UserRoles.ADMIN, UserRoles.VIEWER],
  '/generator': [UserRoles.ADMIN, UserRoles.VIEWER],
  '/settings': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/ai-chat': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/utility': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/status': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/audit-trail': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/access-tokens': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/usage': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/approvals': [UserRoles.ADMIN],
  '/approvals/': [UserRoles.ADMIN],
  '/alerts': [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  '/dev': [],
};

const TOKEN_RENEWAL_OFFSET_SECONDS = 300;
const TOKEN_REFRESH_INTERVAL_MS = 60000;

const defaultAuthContext: AuthContextType = {
  user: null,
  isAuthenticated: false,
  isLoading: true,
  rolesLoaded: false,
  login: async () => false,
  logout: async () => {},
  hasRole: () => false,
  hasAnyRole: () => false,
  hasAllRoles: () => false,
  isRouteAccessible: () => false,
  refreshAuth: async () => false,
  getEffectiveRoles: () => [],
  account: null,
  roleVersion: 0,
};

const AuthContext = createContext<AuthContextType>(defaultAuthContext);
// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);

// Auth state with useReducer
interface AuthState {
  user: UserDetails | null;
  isLoading: boolean;
  rolesLoaded: boolean;
  effectiveRoles: string[];
  roleVersion: number;
}

type AuthAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_USER'; payload: UserDetails | null }
  | { type: 'SET_ROLES_LOADED'; payload: boolean }
  | { type: 'UPDATE_ROLES'; payload: string[] }
  | { type: 'INCREMENT_ROLE_VERSION' }
  | { type: 'RESET' };

const initialAuthState: AuthState = {
  user: null,
  isLoading: true,
  rolesLoaded: false,
  effectiveRoles: [],
  roleVersion: 0,
};

function resolveEffectiveRoles(roles: unknown): string[] {
  const effectiveRoles = AuthorizationService.getEffectiveRoles(roles);
  activeRoleService.setRoles(effectiveRoles);
  return effectiveRoles;
}

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'SET_USER':
      return {
        ...state,
        user: action.payload,
        effectiveRoles: action.payload
          ? resolveEffectiveRoles(action.payload.roles || [])
          : resolveEffectiveRoles([]),
      };
    case 'SET_ROLES_LOADED':
      return { ...state, rolesLoaded: action.payload };
    case 'UPDATE_ROLES':
      return { ...state, effectiveRoles: resolveEffectiveRoles(action.payload) };
    case 'INCREMENT_ROLE_VERSION':
      return { ...state, roleVersion: state.roleVersion + 1 };
    case 'RESET':
      activeRoleService.setRoles([]);
      return { ...initialAuthState, isLoading: false };
    default:
      return state;
  }
}

interface InternalAuthProviderProps {
  children: ReactNode;
  adapter: IAuthAdapter;
}

const InternalAuthProvider = ({ children, adapter }: InternalAuthProviderProps) => {
  const [state, dispatch] = useReducer(authReducer, initialAuthState);

  const account = useMemo(() => {
    return state.user ? adapter.getActiveAccount() : null;
  }, [adapter, state.user]);

  // Sync effective roles to ActiveRoleService for request interceptor
  useEffect(() => {
    activeRoleService.setRoles(state.effectiveRoles);
  }, [state.effectiveRoles]);

  // Initialize authentication
  useEffect(() => {
    let mounted = true;

    const initialize = async () => {
      try {
        dispatch({ type: 'SET_LOADING', payload: true });

        await withTimeout(
          (async () => {
            await adapter.initialize();
            if (!mounted) return;

            const result = await adapter.refresh();

            if (mounted && result.success && result.user) {
              dispatch({ type: 'SET_USER', payload: result.user });
              dispatch({ type: 'SET_ROLES_LOADED', payload: true });
            }
          })(),
          TIMING.AUTH_INIT_TIMEOUT,
          'Authentication initialization timed out'
        );
      } catch (error) {
        if (mounted) {
          AuthErrorHandler.handleError({
            type: AuthErrorType.INITIALIZATION_ERROR,
            message: 'Failed to initialize authentication',
            originalError: error instanceof Error ? error : new Error(String(error)),
          });
        }
      } finally {
        if (mounted) {
          dispatch({ type: 'SET_LOADING', payload: false });
        }
      }
    };

    initialize();

    return () => {
      mounted = false;
    };
  }, [adapter]);

  useEffect(() => {
    const handleTokenFailure = () => {
      dispatch({ type: 'RESET' });

      adapter.logout().catch((error) => {
        AuthErrorHandler.handleError({
          type: AuthErrorType.LOGOUT_ERROR,
          message: 'Error during token failure logout',
          originalError: error instanceof Error ? error : new Error(String(error)),
        });
      });

      toast.error(
        'Session expired due to connectivity issues. Please log in again.',
        { duration: TIMING.TOAST_ERROR_DURATION }
      );

      setTimeout(() => {
        window.location.href = '/login';
      }, 500);
    };

    tokenFailureDetector.setAuthFailureHandler(handleTokenFailure);

    return () => {
      tokenFailureDetector.setAuthFailureHandler(() => {});
    };
  }, [adapter]);

  const updateMockRoles = useCallback((roles: string[]) => {
    dispatch({ type: 'UPDATE_ROLES', payload: roles });
  }, []);

  const incrementRoleVersion = useCallback(() => {
    dispatch({ type: 'INCREMENT_ROLE_VERSION' });
  }, []);

  const setRolesLoaded = useCallback((loaded: boolean) => {
    dispatch({ type: 'SET_ROLES_LOADED', payload: loaded });
  }, []);

  useMockRoleListener({
    setMockRoles: updateMockRoles,
    setRoleVersion: incrementRoleVersion,
    setRolesLoaded,
  });

  const handleLogin = useCallback(async () => {
    dispatch({ type: 'SET_LOADING', payload: true });
    dispatch({ type: 'SET_ROLES_LOADED', payload: false });

    try {
      const success = await adapter.login();

      if (success) {
        const result = await adapter.refresh();
        if (result.success && result.user) {
          dispatch({ type: 'SET_USER', payload: result.user });
          dispatch({ type: 'SET_ROLES_LOADED', payload: true });
          return true;
        }
      }
      return false;
    } catch (e) {
      AuthErrorHandler.handleError({
        type: AuthErrorType.LOGIN_ERROR,
        message: 'Login failed',
        originalError: e instanceof Error ? e : new Error(String(e)),
      });
      return false;
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, [adapter]);

  const handleLogout = useCallback(async () => {
    dispatch({ type: 'RESET' });
    SecureStorage.clearAll(true);

    try {
      await adapter.logout();
    } catch (error) {
      AuthErrorHandler.handleError({
        type: AuthErrorType.LOGOUT_ERROR,
        message: 'Logout failed, forcing redirect to login',
        originalError: error instanceof Error ? error : new Error(String(error)),
      });
      window.location.href = '/login';
    }
  }, [adapter]);

  const handleRefreshAuth = useCallback(async (): Promise<boolean> => {
    try {
      const result = await adapter.refresh();
      if (result.success && result.user) {
        dispatch({ type: 'SET_USER', payload: result.user });
        dispatch({ type: 'SET_ROLES_LOADED', payload: true });
        return true;
      }
      return false;
    } catch {
      return false;
    }
  }, [adapter]);

  const isAuthenticated = useMemo(
    () => !!state.user && state.rolesLoaded,
    [state.user, state.rolesLoaded]
  );

  const hasRole = useCallback(
    (role: UserRoles, exactMatch = false): boolean => {
      try {
        return AuthorizationService.hasRole(state.effectiveRoles, role, exactMatch);
      } catch (error) {
        AuthErrorHandler.handleError({
          type: AuthErrorType.PERMISSION_ERROR,
          message: 'Error during permission check',
          originalError: error instanceof Error ? error : new Error(String(error)),
        });
        return false;
      }
    },
    [state.effectiveRoles]
  );

  const hasAnyRole = useCallback(
    (roles: UserRoles[]): boolean => {
      return AuthorizationService.hasAnyRole(state.effectiveRoles, roles);
    },
    [state.effectiveRoles]
  );

  const hasAllRoles = useCallback(
    (roles: UserRoles[]): boolean => {
      return AuthorizationService.hasAllRoles(state.effectiveRoles, roles);
    },
    [state.effectiveRoles]
  );

  const isRouteAccessible = useCallback(
    (path: string): boolean => {
      return AuthorizationService.isRouteAccessible(
        path,
        state.effectiveRoles,
        PATH_PERMISSIONS,
        ACCESS_FREE_PATHS
      );
    },
    [state.effectiveRoles]
  );

  const getEffectiveRoles = useCallback(() => state.effectiveRoles, [state.effectiveRoles]);

  useTokenRefresh({
    isAuthenticated,
    isLoading: state.isLoading,
    isNoLoginMode: isNoLoginMode(),
    refreshInterval: TOKEN_REFRESH_INTERVAL_MS,
    renewalOffsetSeconds: TOKEN_RENEWAL_OFFSET_SECONDS,
  });

  const contextValue = useMemo(
    () => ({
      user: state.user,
      isAuthenticated,
      isLoading: state.isLoading,
      rolesLoaded: state.rolesLoaded,
      login: handleLogin,
      logout: handleLogout,
      refreshAuth: handleRefreshAuth,
      account,
      roleVersion: state.roleVersion,
      hasRole,
      hasAnyRole,
      hasAllRoles,
      isRouteAccessible,
      getEffectiveRoles,
    }),
    [
      state.user,
      state.isLoading,
      state.rolesLoaded,
      state.roleVersion,
      isAuthenticated,
      handleLogin,
      handleLogout,
      handleRefreshAuth,
      account,
      hasRole,
      hasAnyRole,
      hasAllRoles,
      isRouteAccessible,
      getEffectiveRoles,
    ]
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const { msalInstance, adapter } = useMemo(() => {
    const instance = new PublicClientApplication({
      auth: {
        clientId: environment.auth.clientId,
        authority: environment.auth.authority,
        redirectUri: environment.auth.redirectUri,
      },
      cache: {
        cacheLocation: 'localStorage',
        storeAuthStateInCookie: false,
      },
      system: {
        tokenRenewalOffsetSeconds: TOKEN_RENEWAL_OFFSET_SECONDS,
        iframeHashTimeout: TIMING.AUTH_IFRAME_HASH_TIMEOUT,
      },
    });

    const authAdapter = createAuthAdapter(instance);

    return { msalInstance: instance, adapter: authAdapter };
  }, []);

  return (
    <MsalProvider instance={msalInstance}>
      <InternalAuthProvider adapter={adapter}>{children}</InternalAuthProvider>
    </MsalProvider>
  );
};
