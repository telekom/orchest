import { TIMING } from '@/shared/constants/ui.config';
import { Button } from '@/design-system/components/ui/button';
import { toast } from '@/design-system/components/ui/sonner';
import { useAuth } from '@/shared/auth/context/AuthContext';
import { useInactivityTimeout } from '@/shared/hooks/useInactivityTimeout';
import { formatTimeRemaining } from '@/shared/utils/timeUtils';
import { Dialog, DialogContent, DialogHeader, DialogFooter, DialogTitle } from '@/design-system/components/ui/dialog';
import { Clock } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import styles from './InactivityGuard.module.css';

/**
 * SECURITY: Inactivity Guard Component
 *
 * Monitors user inactivity and automatically logs out users after a period of inactivity.
 * Shows a warning dialog before logout to allow users to extend their session.
 *
 * Configuration:
 * - Timeout: 30 minutes of inactivity (configurable via environment)
 * - Warning: 2 minutes before timeout
 *
 * @example
 * ```tsx
 * <AuthProvider>
 *   <InactivityGuard />
 *   <App />
 * </AuthProvider>
 * ```
 */

interface InactivityGuardProps {
  timeoutMinutes?: number;
  warningMinutes?: number;
}

// Default configuration (can be overridden via env vars or props)
const DEFAULT_TIMEOUT_MINUTES = 2;
const DEFAULT_WARNING_MINUTES = 2;

export const InactivityGuard: React.FC<InactivityGuardProps> = ({
  timeoutMinutes = DEFAULT_TIMEOUT_MINUTES,
  warningMinutes = DEFAULT_WARNING_MINUTES,
}) => {
  const { isAuthenticated, logout } = useAuth();
  const [showWarningDialog, setShowWarningDialog] = useState(false);

  const handleTimeout = useCallback(() => {
    setShowWarningDialog(false);
    toast.error('Session expired due to inactivity', {
      duration: TIMING.TOAST_ERROR_DURATION,
    });
    logout().catch(() => {
      // Fallback: force redirect if async logout fails
      window.location.href = '/login';
    });
  }, [logout]);

  const handleWarning = useCallback(() => {
    setShowWarningDialog(true);
  }, []);

  const { remainingTime, resetTimer, isWarning } = useInactivityTimeout({
    onTimeout: handleTimeout,
    timeoutMs: timeoutMinutes * 60 * 1000,
    warningMs: warningMinutes * 60 * 1000,
    onWarning: handleWarning,
    enabled: isAuthenticated,
  });

  const handleStayLoggedIn = useCallback(() => {
    setShowWarningDialog(false);
    resetTimer();
    toast.success('Session extended', {
      duration: TIMING.TOAST_DEFAULT_DURATION,
    });
  }, [resetTimer]);

  const handleLogoutNow = useCallback(() => {
    setShowWarningDialog(false);
    logout().catch(() => {
      window.location.href = '/login';
    });
  }, [logout]);

  // Auto-dismiss warning if user becomes active again
  useEffect(() => {
    if (showWarningDialog && !isWarning) {
      setShowWarningDialog(false);
    }
  }, [showWarningDialog, isWarning]);

  if (!isAuthenticated) {
    return null;
  }

  return (
    <Dialog open={showWarningDialog} onOpenChange={setShowWarningDialog}>
      <DialogContent className={styles.dialogContent}>
        <DialogHeader>
          <DialogTitle>Session Expiring Soon</DialogTitle>
        </DialogHeader>
        <div className={styles.headerContainer}>
          <div className={styles.iconContainer}>
            <Clock className={styles.icon} />
          </div>
          <div className={styles.description}>
            Your session will expire in <strong>{formatTimeRemaining(remainingTime)}</strong> due to
            inactivity.
            <br />
            <br />
            Would you like to stay logged in?
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" size="sm" onClick={handleLogoutNow}>
            Logout Now
          </Button>
          <Button variant="primary" size="sm" onClick={handleStayLoggedIn}>
            Stay Logged In
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
