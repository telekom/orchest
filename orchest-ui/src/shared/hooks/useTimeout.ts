import { useEffect, useRef } from 'react';

/**
 * Hook for managing timeouts with automatic cleanup
 */
export function useTimeout(
  callback: () => void,
  delay: number | null,
  deps: unknown[] = []
) {
  const savedCallback = useRef(callback);

  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  useEffect(() => {
    if (delay === null) {
      return;
    }

    const id = setTimeout(() => savedCallback.current(), delay);

    return () => clearTimeout(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps -- deps spread controlled by consumer
  }, [delay, ...deps]);
}
