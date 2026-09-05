import { useAuth } from '@/shared/auth/context/AuthContext';
import { getRedirectUrl } from '@/shared/auth/utils/redirectUtils';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface UseAuthRedirectOptions {
  enabled?: boolean;
  redirectTo?: string;
}

export function useAuthRedirect(options: UseAuthRedirectOptions = {}) {
  const { enabled = true, redirectTo } = options;
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!enabled || isLoading) {
      return;
    }

    if (isAuthenticated) {
      const destination = redirectTo || getRedirectUrl();
      navigate(destination, { replace: true });
    }
  }, [isAuthenticated, isLoading, enabled, redirectTo, navigate]);
}
