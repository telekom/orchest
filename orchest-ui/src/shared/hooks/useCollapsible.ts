import { useCallback, useState } from 'react';

export interface UseCollapsibleOptions {
  initialOpen?: boolean;
}

/**
 * Hook for managing collapsible/expandable sections
 */
export function useCollapsible(options: UseCollapsibleOptions = {}) {
  const { initialOpen = true } = options;

  const [isOpen, setIsOpen] = useState(initialOpen);

  const toggle = useCallback(() => {
    setIsOpen((prev) => !prev);
  }, []);

  const open = useCallback(() => {
    setIsOpen(true);
  }, []);

  const close = useCallback(() => {
    setIsOpen(false);
  }, []);

  return {
    isOpen,
    setIsOpen,
    toggle,
    open,
    close,
  };
}
