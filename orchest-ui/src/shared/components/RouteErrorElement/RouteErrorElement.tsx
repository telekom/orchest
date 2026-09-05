import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/design-system/components/ui/card';
import { globalErrorHandler } from '@/shared/error/globalErrorHandler';
import { Button } from "@/design-system/components/ui/button";
import { useEffect } from 'react';
import { useNavigate, useRouteError } from 'react-router-dom';
import { getErrorMessage } from './errorMessages';
import styles from './RouteErrorElement.module.css';

export const RouteErrorElement: React.FC = () => {
  const error = useRouteError();
  const navigate = useNavigate();

  useEffect(() => {
    if (error instanceof Error) {
      globalErrorHandler.handleError(error, 'runtime', {
        context: 'route-loader-error',
        severity: 'high',
      });
    }
  }, [error]);

  const handleRetry = () => {
    window.location.reload();
  };

  const handleGoHome = () => {
    navigate('/');
  };

  const { title, description, icon: Icon } = getErrorMessage(error);

  return (
    <div className={styles.container}>
      <Card className={styles.card}>
        <CardHeader className={styles.cardHeader}>
          <div className={styles.iconContainer}>
            <Icon className={styles.icon} />
          </div>
          <CardTitle className={styles.cardTitle}>{title}</CardTitle>
          <CardDescription className={styles.cardDescription}>
            {description}
          </CardDescription>
        </CardHeader>
        <CardContent className={styles.cardContent}>
          {import.meta.env.DEV && error instanceof Error && (
            <div className={styles.errorDetails}>
              <p className={styles.errorDetailsTitle}>Error Details:</p>
              <p className={styles.errorMessage}>{error.message}</p>
              {error.stack && (
                <details className={styles.errorDetailsStack}>
                  <summary className={styles.errorDetailsStackSummary}>Stack trace</summary>
                  <pre className={styles.errorDetailsStackPre}>
                    {error.stack}
                  </pre>
                </details>
              )}
            </div>
          )}
          <div className={styles.actionButtons}>
            <Button
              variant="primary"
              size="sm"
              onClick={handleRetry}
              label="Try Again"
            />
            <Button
              variant="ghost"
              size="sm"
              onClick={handleGoHome}
              label="Go Home"
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default RouteErrorElement;
