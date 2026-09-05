import { TIMING } from '@/shared/constants/ui.config';
import { useCallback, useState } from 'react';
import { toast } from '@/design-system/components/ui/sonner';

type NotificationType = 'info' | 'success' | 'warning' | 'error';
type NotificationChannel = 'toast' | 'banner' | 'alert';

interface UseNotificationTesterReturn {
  triggerNotification: (type: string, message: string, channel: string) => void;
  alertProps: {
    open: boolean;
    title: string;
    message: string;
    variant: NotificationType;
    onOpenChange: (open: boolean) => void;
  }
}

/**
 * Hook for testing notification system in DevTools
 * Allows triggering different notification types (info, success, warning, error)
 * through different channels (toast, banner, alert)
 */
export const useNotificationTester = (): UseNotificationTesterReturn => {
  const [alertDialog, setAlertDialog] = useState({
    open: false,
    title: '',
    message: '',
    variant: 'info' as NotificationType
  });

  const triggerNotification = useCallback((type: string, message: string, channel: string) => {
    const notificationType = type as NotificationType;
    const notificationChannel = channel as NotificationChannel;

    switch (notificationChannel) {
      case 'toast':
        toast[notificationType](message, {
          position: 'top-right'
        });
        break;

      case 'alert':
        setAlertDialog({
          open: true,
          title: notificationType.charAt(0).toUpperCase() + notificationType.slice(1),
          message,
          variant: notificationType
        });
        break;

      case 'banner':
        toast[notificationType](message, {
          duration: TIMING.NOTIFICATION_TEST_DURATION,
          position: 'top-center',
          className: 'w-full max-w-md',
          unstyled: true,
          classNames: {
            toast: 'w-full bg-white dark:bg-gray-800 shadow-lg rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700',
            title: 'text-body-sm font-medium text-gray-900 dark:text-gray-100 p-4',
            description: 'text-caption text-gray-500 dark:text-gray-400'
          }
        });
        break;

      default:
        toast.info(message);
    }
  }, []);

  return {
    triggerNotification,
    alertProps: {
      open: alertDialog.open,
      title: alertDialog.title,
      message: alertDialog.message,
      variant: alertDialog.variant,
      onOpenChange: (open: boolean) => setAlertDialog(prev => ({ ...prev, open }))
    }
  };
};
