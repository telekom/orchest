import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/design-system/components/ui/card';
import { Button } from '@/design-system/components/ui/button';
import clsx from "clsx";
import { AlertCircle } from 'lucide-react';
import React, { Component, ErrorInfo, ReactNode } from 'react';
import styles from './ErrorBoundary.module.css';
import { globalErrorHandler } from './globalErrorHandler';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  level?: 'page' | 'component' | 'app';
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  errorId: string | null;
}

const MAX_RETRIES = 3;

const INITIAL_STATE: State = {
  hasError: false,
  error: null,
  errorInfo: null,
  errorId: null,
};

interface ErrorDetailsProps {
  error: Error | null;
  errorId: string | null;
  isCompact?: boolean;
}

const ErrorDetails: React.FC<ErrorDetailsProps> = ({ error, errorId, isCompact = false }) => {
  if (!import.meta.env.DEV || !error) return null;

  return (
    <div className={clsx(styles.errorDetails, isCompact && styles.errorDetailsCompact)}>
      <p className={styles.errorDetailsTitle}>
        {isCompact ? "Error:" : "Error Details:"}
      </p>
      <p className={styles.errorDetailsMessage}>{error.message}</p>
      {errorId && (
        <p className={clsx(styles.errorDetailsId, isCompact && styles.errorDetailsIdCompact)}>
          Error ID: {errorId}
        </p>
      )}
    </div>
  );
};


interface ErrorHeaderProps {
  iconSize: 'large' | 'medium' | 'small';
  title: string;
  description: string;
  titleClassName?: string;
  centered?: boolean;
}

const ErrorHeader: React.FC<ErrorHeaderProps> = ({ iconSize, title, description, titleClassName = "text-h2", centered }) => {
  return (
    <CardHeader className={centered ? "text-center" : ""}>
      <div className={centered ? styles.appIconContainer : styles.componentIconContainer}>
        <AlertCircle className={clsx(styles.alertIcon, styles[`icon${iconSize.charAt(0).toUpperCase() + iconSize.slice(1)}` as keyof typeof styles])} />
        {!centered && <CardTitle className={titleClassName}>{title}</CardTitle>}
      </div>
      {centered && <CardTitle className={titleClassName}>{title}</CardTitle>}
      <CardDescription>{description}</CardDescription>
    </CardHeader>
  );
};

export class ErrorBoundary extends Component<Props, State> {
  private retryCount = 0;
  private readonly maxRetries = MAX_RETRIES;

  constructor(props: Props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    const appError = globalErrorHandler.handleError(error, 'runtime', {
      componentStack: errorInfo.componentStack,
      errorBoundaryLevel: this.props.level || 'component',
      retryCount: this.retryCount
    });

    this.setState({
      errorInfo,
      errorId: appError.id
    });

    this.props.onError?.(error, errorInfo);
  }

  handleRetry = () => {
    if (this.retryCount < this.maxRetries) {
      this.retryCount++;
      this.setState(INITIAL_STATE);
    }
  };

  handleGoHome = () => {
    window.location.href = '/';
  };

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return this.renderErrorUI();
    }

    return this.props.children;
  }

  private renderErrorUI() {
    const { level = 'component' } = this.props;
    const { error, errorId } = this.state;
    const canRetry = this.retryCount < this.maxRetries;

    switch (level) {
      case 'app':
        return this.renderAppLevelError(error, errorId);
      case 'page':
        return this.renderPageLevelError(error, errorId, canRetry);
      case 'component':
      default:
        return this.renderComponentLevelError(error, errorId, canRetry);
    }
  }

  private renderAppLevelError(error: Error | null, errorId: string | null) {
    return (
      <div className={styles.appContainer}>
        <Card className={styles.appCard}>
          <ErrorHeader
            iconSize="large"
            title="Application Error"
            description="A critical error has occurred. Please reload the application."
            titleClassName="text-h1"
            centered
          />
          <CardContent className={styles.contentSpace}>
            <ErrorDetails error={error} errorId={errorId} />
            <div className={styles.appActions}>
              <Button
                variant="primary"
                size="sm"
                onClick={this.handleReload}
                className={clsx(styles.refreshButton, styles.appButton)}
              >
                Reload App
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  private renderPageLevelError(error: Error | null, errorId: string | null, canRetry: boolean) {
    return (
      <div className={styles.pageContainer}>
        <Card className={styles.pageCard}>
          <ErrorHeader
            iconSize="medium"
            title="Page Error"
            description="An error occurred while loading this page."
            centered
          />
          <CardContent className={styles.contentSpace}>
            <ErrorDetails error={error} errorId={errorId} />
            <div className={styles.pageActions}>
              {canRetry && (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={this.handleRetry}
                  className={styles.refreshButton}
                >
                  Retry
                </Button>
              )}
              <Button
                variant="ghost"
                size="sm"
                onClick={this.handleGoHome}
              >
                Back to Home
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  private renderComponentLevelError(error: Error | null, errorId: string | null, canRetry: boolean) {
    return (
      <Card className={styles.componentCard}>
        <ErrorHeader
          iconSize="small"
          title="Component Error"
          description="This component could not be displayed due to an error."
          titleClassName="text-h3"
        />
        <CardContent>
          <ErrorDetails error={error} errorId={errorId} isCompact />
          {canRetry && (
            <Button
              variant="ghost"
              size="sm"
              onClick={this.handleRetry}
            >
              Retry
            </Button>
          )}
        </CardContent>
      </Card>
    );
  }
}

// eslint-disable-next-line react-refresh/only-export-components
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  level: Props['level'] = 'component'
) {
  return function WrappedComponent(props: P) {
    return (
      <ErrorBoundary level={level}>
        <Component {...props} />
      </ErrorBoundary>
    );
  };
}

// eslint-disable-next-line react-refresh/only-export-components
export function useErrorHandler() {
  const handleError = React.useCallback((error: Error, context?: Record<string, unknown>) => {
    globalErrorHandler.handleError(error, 'runtime', context);
  }, []);

  const handleAsyncError = React.useCallback(async (asyncFn: () => Promise<unknown>) => {
    try {
      return await asyncFn();
    } catch (error) {
      handleError(error as Error, { type: 'async_operation' });
      throw error;
    }
  }, [handleError]);

  return { handleError, handleAsyncError };
}

export default ErrorBoundary;
