import { useCallback, useEffect, useRef } from 'react';

export function useStableCallback<T extends (...args: unknown[]) => unknown>(
  callback: T | undefined
): T {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  return useCallback(
    ((...args: Parameters<T>) => callbackRef.current?.(...args)) as T,
    []
  );
}
