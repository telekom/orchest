import { Button } from "@/design-system/components/ui/button";
import { useAuth } from "@/shared/auth/context/AuthContext";
import { useAuthRedirect } from "@/shared/auth/hooks/useAuthRedirect";
import { FullPageLoader } from "@/shared/components";
import { ROUTES } from "@/shared/constants";
import { environment } from "@/shared/constants/environment";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import clsx from "clsx";
import styles from "./Login.module.css";

const Login: React.FC = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { login: authLogin, isAuthenticated, isLoading: authInitializing } = useAuth();
  const bypassAutoSignInStarted = useRef(false);

  useAuthRedirect();

  const handleLogin = useCallback(async () => {
    try {
      setIsSubmitting(true);
      setError(null);

      const success = await authLogin();

      if (!success) {
        setError("Login failed. Please try again.");
      }
    } catch (err) {
      globalErrorHandler.handleError(
        err instanceof Error ? err : new Error(String(err)),
        'auth',
        { context: 'login-page', severity: 'high' }
      );
      setError("An error occurred during login. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  }, [authLogin]);

  // Single-switch bypass: no login marketing UI — auto-establish session then redirect
  useEffect(() => {
    if (!environment.bypassLogin) return;
    if (authInitializing) return;
    if (isAuthenticated) return;
    if (bypassAutoSignInStarted.current) return;
    bypassAutoSignInStarted.current = true;
    void authLogin().then((success) => {
      if (!success) {
        setError("Unable to start your session. Please try again.");
      }
    });
  }, [authInitializing, isAuthenticated, authLogin]);

  if (environment.bypassLogin) {
    if (isAuthenticated) {
      return <FullPageLoader text="Redirecting..." />;
    }
    if (authInitializing || isSubmitting) {
      return <FullPageLoader text="Signing you in..." />;
    }
    if (error) {
      return (
        <div className={styles.container}>
          <div className={styles.card}>
            <div className={styles.header}>
              <div className={styles.logoContainer}>
                <svg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg">
                  <rect width="60" height="60" fill="#e20074" />
                  <path
                    d="M22.67,35.28h-6.67v-6.6h6.67v6.6Zm-6.67-21.78v11.22h2v-.33c0-5.28,3-8.58,8.67-8.58h.33v23.76c0,3.3-1.33,4.62-4.67,4.62h-1v2.31h17.33v-2.31h-1c-3.33,0-4.67-1.32-4.67-4.62V15.81h.33c5.67,0,8.67,3.3,8.67,8.58v.33h2V13.5H16Zm21.33,21.78h6.67v-6.6h-6.67v6.6Z"
                    fill="#fff"
                  />
                </svg>
              </div>
              <h1 className={styles.title}>OrchesT</h1>
              <p className={styles.subtitle}>We couldn&apos;t start your session</p>
            </div>
            <div className={styles.content}>
              <div className={styles.error}>{error}</div>
              <Button
                variant="primary"
                size="sm"
                label="Try again"
                buttonIcon="refresh"
                onClick={() => void handleLogin()}
                disabled={isSubmitting}
                className={clsx(styles.loginButton)}
              />
            </div>
          </div>
        </div>
      );
    }
    return <FullPageLoader text="Signing you in..." />;
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <div className={styles.logoContainer}>
            <svg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg">
              <rect width="60" height="60" fill="#e20074" />
              <path
                d="M22.67,35.28h-6.67v-6.6h6.67v6.6Zm-6.67-21.78v11.22h2v-.33c0-5.28,3-8.58,8.67-8.58h.33v23.76c0,3.3-1.33,4.62-4.67,4.62h-1v2.31h17.33v-2.31h-1c-3.33,0-4.67-1.32-4.67-4.62V15.81h.33c5.67,0,8.67,3.3,8.67,8.58v.33h2V13.5H16Zm21.33,21.78h6.67v-6.6h-6.67v6.6Z"
                fill="#fff"
              />
            </svg>
          </div>
          <h1 className={styles.title}>OrchesT</h1>
          <p className={styles.subtitle}>Sign in to access your dashboard</p>
        </div>

        <div className={styles.content}>
          <Button
            variant="primary"
            size="sm"
            label="Sign in with Microsoft"
            buttonIcon="login"
            onClick={handleLogin}
            disabled={isSubmitting}
            className={clsx(styles.loginButton)}
          />

          {error && <div className={styles.error}>{error}</div>}

          <Link to={ROUTES.FEEL_PLAYGROUND} className={styles.publicLink}>
            Open FEEL Playground
          </Link>

          <div className={styles.footer}>
            <p>By signing in, you agree to our Terms of Service and Privacy Policy</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
