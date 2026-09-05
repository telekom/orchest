import { ErrorBoundary } from '@/shared/error/ErrorBoundary';
import { globalErrorHandler } from '@/shared/error/globalErrorHandler';
import styles from './RouteErrorBoundary.module.css';
import React, { ReactNode, Suspense } from 'react';
import { SpinnerLoader } from '../Loader/Loader';

interface RouteErrorBoundaryProps {
  children: ReactNode;
  routeName?: string;
}

const RouteLoader = () => (
  <div className={styles.loaderContainer}>
    <SpinnerLoader />
  </div>
);

export const RouteErrorBoundary: React.FC<RouteErrorBoundaryProps> = ({
  children,
  routeName,
}) => {
  const handleError = (error: Error) => {
    globalErrorHandler.handleError(error, 'runtime', {
      context: `route-error-${routeName || 'unknown'}`,
      routeName,
      severity: 'high',
      timestamp: new Date().toISOString(),
    });
  };

  return (
    <ErrorBoundary level="page" onError={handleError}>
      <Suspense fallback={<RouteLoader />}>
        {children}
      </Suspense>
    </ErrorBoundary>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export function withRouteErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  routeName?: string
) {
  return function WrappedRoute(props: P) {
    return (
      <RouteErrorBoundary routeName={routeName}>
        <Component {...props} />
      </RouteErrorBoundary>
    );
  };
}

export default RouteErrorBoundary;
