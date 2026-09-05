import { toast as sonnerToast } from '@/design-system/components/ui/sonner';
import { useIsDarkMode } from '@/shared/stores/uiStore';

/**
 * Get current theme status from DOM.
 * Note: This reads directly from DOM as it's used in non-React contexts
 * where hooks cannot be used. UIStore updates the DOM, so this is
 * the source of truth for theme state outside React components.
 */
const getThemeStatus = (): boolean => {
  return document.documentElement.classList.contains('dark');
};

/**
 * React hook for toast notifications with theme-aware styling.
 * Use this in React components where hooks are available.
 * Automatically syncs with UIStore.
 */
export const useToast = () => {
  const isDarkMode = useIsDarkMode();

  return {
    info: (message: string) => {
      sonnerToast(message, {
        className: isDarkMode ? 'info dark-mode-toast' : 'info',
      });
    },
    
    success: (message: string) => {
      sonnerToast.success(message, {
        className: isDarkMode ? 'success dark-mode-toast' : 'success',
      });
    },
    
    warning: (message: string) => {
      sonnerToast.warning(message, {
        className: isDarkMode ? 'warning dark-mode-toast' : 'warning',
      });
    },
    
    error: (message: string) => {
      sonnerToast.error(message, {
        className: isDarkMode ? 'error dark-mode-toast' : 'error',
      });
    },
    
    default: sonnerToast,
  };
};

/**
 * Toast notification utilities for non-React contexts.
 * Use this in service files, utility functions, or anywhere hooks cannot be used.
 * Automatically detects theme from DOM (updated by UIStore).
 */
export const toast = {
  info: (message: string) => {
    sonnerToast(message, {
      className: getThemeStatus() ? 'info dark-mode-toast' : 'info',
    });
  },
  
  success: (message: string) => {
    sonnerToast.success(message, {
      className: getThemeStatus() ? 'success dark-mode-toast' : 'success',
    });
  },
  
  warning: (message: string) => {
    sonnerToast.warning(message, {
      className: getThemeStatus() ? 'warning dark-mode-toast' : 'warning',
    });
  },
  


  error: (message: string) => {
    sonnerToast.error(message, {
      className: getThemeStatus() ? 'error dark-mode-toast' : 'error',
    });
  },
  
  default: sonnerToast,
};

export default toast;