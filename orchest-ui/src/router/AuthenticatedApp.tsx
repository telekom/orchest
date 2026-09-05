import { environment } from '@/shared/constants/environment';
import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { isValidRedirectUrl } from '@/shared/utils/urlValidation';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { useEffect } from 'react';

interface AuthenticatedAppProps {
  router: ReturnType<typeof createBrowserRouter>;
}

const captureIntendedUrl = () => {
  const { pathname, search, hash } = window.location;

  if (
    pathname === '/' ||
    ['/login', '/callback', '/no-roles', '/dev'].some(path => pathname.startsWith(path))
  ) {
    return;
  }

  const intendedUrl = `${pathname}${search}${hash}`;

  if (isValidRedirectUrl(intendedUrl)) {
    localStorage.setItem(STORAGE_KEYS.REDIRECT_AFTER_LOGIN, intendedUrl);
  }
};

export const AuthenticatedApp: React.FC<AuthenticatedAppProps> = ({ router }) => {
  useEffect(() => {
    if (!environment.bypassLogin) {
      captureIntendedUrl();
    }
  }, []);

  return <RouterProvider router={router} />;
};
