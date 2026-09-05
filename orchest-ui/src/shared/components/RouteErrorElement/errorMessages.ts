import { AlertCircle, LucideIcon, Wifi } from 'lucide-react';

export interface ErrorMessage {
  title: string;
  description: string;
  icon: LucideIcon;
}

const isNetworkError = (error: unknown): boolean => {
  if (error instanceof Error) {
    const msg = error.message.toLowerCase();
    return msg.includes('network') || msg.includes('err_network') || msg.includes('failed to fetch');
  }
  return typeof error === 'object' && error !== null && 'code' in error && error.code === 'ERR_NETWORK';
};

export const getErrorMessage = (error: unknown): ErrorMessage => {
  if (isNetworkError(error)) {
    return {
      title: 'Connection Lost',
      description: 'Unable to connect to the server. Please check your internet connection and try again.',
      icon: Wifi,
    };
  }

  // Check if it's a route error response (from react-router)
  if (typeof error === 'object' && error !== null && 'status' in error) {
    const routeError = error as { status: number; statusText?: string };

    switch (routeError.status) {
      case 404:
        return {
          title: 'Page Not Found',
          description: 'The page you are looking for does not exist.',
          icon: AlertCircle,
        };
      case 401:
        return {
          title: 'Authentication Required',
          description: 'You need to be logged in to access this page.',
          icon: AlertCircle,
        };
      case 403:
        return {
          title: 'Access Denied',
          description: 'You do not have permission to access this page.',
          icon: AlertCircle,
        };
      default:
        return {
          title: `Error ${routeError.status}`,
          description: routeError.statusText || 'An error occurred while loading this page.',
          icon: AlertCircle,
        };
    }
  }

  return {
    title: 'Something Went Wrong',
    description: error instanceof Error ? error.message : 'An unexpected error occurred while loading this page.',
    icon: AlertCircle,
  };
};
