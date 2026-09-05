import { useEffect, type RefObject } from 'react';
import { useStableCallback } from './useStableCallback';

interface UseIntersectionLoaderOptions {
  /** Element to observe. The callback fires when it intersects the root. */
  targetRef: RefObject<Element | null>;
  /** Optional explicit scroll-root. Defaults to the viewport. */
  rootRef?: RefObject<Element | null>;
  /** Whether the observer should be active. Useful to disable when no more pages exist. */
  enabled: boolean;
  /** Callback fired when the target enters the root viewport. */
  onIntersect: () => void;
  /** rootMargin passed through to IntersectionObserver. Defaults to "0px". */
  rootMargin?: string;
  /** Threshold passed through to IntersectionObserver. Defaults to 0. */
  threshold?: number | number[];
}

/**
 * Reusable hook that observes a single sentinel element with IntersectionObserver
 * and invokes `onIntersect` whenever the element becomes visible inside `rootRef`.
 *
 * Designed for infinite-scroll patterns: place a small sentinel element near the
 * bottom of a scrollable list and load the next page when it intersects.
 */
export function useIntersectionLoader({
  targetRef,
  rootRef,
  enabled,
  onIntersect,
  rootMargin = '0px',
  threshold = 0,
}: UseIntersectionLoaderOptions) {
  const stableOnIntersect = useStableCallback(onIntersect);

  useEffect(() => {
    if (!enabled) return undefined;

    const target = targetRef.current;
    if (!target) return undefined;

    if (typeof IntersectionObserver === 'undefined') {
      // Older browsers — caller should provide a manual fallback (e.g. a button).
      return undefined;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            stableOnIntersect();
            break;
          }
        }
      },
      {
        root: rootRef?.current ?? null,
        rootMargin,
        threshold,
      }
    );

    observer.observe(target);
    return () => observer.disconnect();
    // We intentionally include rootRef?.current so the observer rebinds when
    // the scroll container mounts/changes.
  }, [enabled, targetRef, rootRef, rootMargin, threshold, stableOnIntersect]);
}
