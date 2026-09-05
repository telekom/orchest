import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import clsx from 'clsx';
import type { InfiniteScrollConfig } from '../../types';
import styles from './InfiniteScrollFooter.module.css';

interface InfiniteScrollFooterProps {
  readonly config: InfiniteScrollConfig;
  /** Disable the manual "Load more" button (e.g. while initial fetch is running). */
  readonly disabled?: boolean;
}

const formatNumber = (value: number): string => value.toLocaleString();

const clampPercent = (value: number): number => {
  if (!Number.isFinite(value)) return 0;
  if (value < 0) return 0;
  if (value > 100) return 100;
  return value;
};

/**
 * Sticky footer for the infinite-scroll variant of {@link DataTable}.
 * Shows the loaded/total counter, a progress bar and contextual status
 * (loading, end-of-list, or a manual "Load more" fallback).
 *
 * The IntersectionObserver sentinel that auto-loads the next page lives
 * *inside* the scroll container in `DataTable` itself.
 */
export function InfiniteScrollFooter({
  config,
  disabled = false,
}: InfiniteScrollFooterProps) {
  const {
    hasNextPage,
    isFetchingNextPage,
    onLoadMore,
    totalElements,
    loadedElements,
    loadedLabel = 'loaded',
    loadMoreLabel = 'Load more',
    endOfListLabel = 'All records loaded.',
  } = config;

  const safeTotal = Math.max(totalElements, loadedElements);
  const percentLoaded = safeTotal > 0 ? clampPercent((loadedElements / safeTotal) * 100) : 0;
  const isComplete = !hasNextPage && loadedElements > 0;
  const isEmpty = loadedElements === 0;

  const renderRightSlot = () => {
    if (isFetchingNextPage) {
      return (
        <span className={styles.status}>
          <SpinnerLoader size="sm" />
          <span>Loading more…</span>
        </span>
      );
    }
    if (isComplete) {
      return (
        <span className={clsx(styles.status, styles.statusDone)}>
          <span className={styles.statusDot} aria-hidden="true" />
          <span>{endOfListLabel}</span>
        </span>
      );
    }
    if (hasNextPage && !isEmpty) {
      return (
        <button
          type="button"
          className={styles.loadMoreButton}
          onClick={onLoadMore}
          disabled={disabled}
        >
          {loadMoreLabel}
        </button>
      );
    }
    return null;
  };

  return (
    <div className={styles.container} role="status" aria-live="polite">
      <div className={styles.row}>
        <span className={styles.counter}>
          <span className={styles.counterLoaded}>{formatNumber(loadedElements)}</span>
          <span className={styles.counterTotal}>
            of {formatNumber(safeTotal)} {loadedLabel}
          </span>
        </span>

        <div className={styles.actions}>{renderRightSlot()}</div>
      </div>

      <div
        className={styles.progressTrack}
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(percentLoaded)}
      >
        <div
          className={clsx(
            styles.progressFill,
            isFetchingNextPage && safeTotal === 0 && styles.progressFillIndeterminate
          )}
          style={{ width: `${percentLoaded}%` }}
        />
      </div>
    </div>
  );
}
