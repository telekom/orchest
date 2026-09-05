import { OrchLogo } from "@/shared/components/OrchLogo";
import { useAuthRedirect } from "@/shared/auth/hooks/useAuthRedirect";
import { TIMING } from "@/shared/constants";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from './Callback.module.css';

const Callback: React.FC = () => {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  useAuthRedirect();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        const errorParam = urlParams.get('error');

        if (errorParam) {
          const errorMessage = urlParams.get('error_description') || errorParam;
          setError(errorMessage);

          globalErrorHandler.handleError(
            new Error(`OAuth error: ${errorMessage}`),
            'auth',
            { context: 'oauth-callback', severity: 'high' }
          );

          setTimeout(() => navigate('/login', { replace: true }), TIMING.AUTH_REDIRECT_DELAY);
          return;
        }
      } catch (err) {
        globalErrorHandler.handleError(
          err instanceof Error ? err : new Error(String(err)),
          'auth',
          { context: 'oauth-callback-handler', severity: 'critical' }
        );
        setError('Authentication failed. Redirecting to login...');
        setTimeout(() => navigate('/login', { replace: true }), TIMING.AUTH_REDIRECT_DELAY);
      }
    };

    handleCallback();
  }, [navigate]);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        {error ? (
          <>
            <div className={styles.errorContainer}>
              <svg
                className={styles.errorIcon}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <h2 className={styles.errorTitle}>Authentication Error</h2>
              <p className={styles.errorMessage}>{error}</p>
            </div>
            <p className={styles.redirectMessage}>
              Redirecting to login...
            </p>
          </>
        ) : (
          <>
            <OrchLogo size={56} />
            <h2 className={styles.successTitle}>
              Completing authentication...
            </h2>
            <p className={styles.successMessage}>
              Please wait while we redirect you.
            </p>
          </>
        )}
      </div>
    </div>
  );
};

export default Callback;
