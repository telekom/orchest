import { LoadingIcon } from '@/design-system/components/ui/loading-icon';
import { Skeleton } from '@/design-system/components/ui/skeleton';
import { OrchLogo } from '@/shared/components/OrchLogo';
import styles from './Loader.module.css';
import React from 'react';
import clsx from 'clsx';

const SIZE_CLASSES = {
  sm: styles.sizeSmall,
  md: styles.sizeMedium,
  lg: styles.sizeLarge,
} as const;

const WIDTH_PERCENTAGES = {
  full: '100%',
  half: '50%',
  quarter: '25%',
} as const;

const generateArray = (length: number) => Array(length).fill(0);

interface BaseLoaderProps {
  className?: string;
}

interface SpinnerLoaderProps extends BaseLoaderProps {
  type: 'spinner';
  size?: 'sm' | 'md' | 'lg';
  text?: string;
  variant?: 'light' | 'dark';
}

interface TableSkeletonProps extends BaseLoaderProps {
  type: 'table';
  rows?: number;
  columns?: number;
  hasHeader?: boolean;
}

interface CardSkeletonProps extends BaseLoaderProps {
  type: 'card';
  lines?: number;
  hasTitle?: boolean;
}

interface TextSkeletonProps extends BaseLoaderProps {
  type: 'text';
  lines?: number;
  width?: 'full' | 'half' | 'quarter';
}

interface FullPageLoaderProps extends BaseLoaderProps {
  type: 'fullpage';
  text?: string;
}

type LoaderProps = 
  | SpinnerLoaderProps 
  | TableSkeletonProps 
  | CardSkeletonProps 
  | TextSkeletonProps
  | FullPageLoaderProps;

const Loader: React.FC<LoaderProps> = (props) => {
  const { type, className } = props;

  if (type === 'spinner') {
    const { size = 'md', text, variant = 'light' } = props as SpinnerLoaderProps;
    const colorClass = variant === 'dark' ? styles.textDark : styles.textLight;

    return (
      <div className={clsx(styles.spinnerContainer, className)}>
        <LoadingIcon className={clsx(SIZE_CLASSES[size], styles.spinnerIcon, colorClass)} />
        {text && (
          <span className={clsx(styles.spinnerText, colorClass)}>
            {text}
          </span>
        )}
      </div>
    );
  }

  if (type === 'table') {
    const { rows = 5, columns = 4, hasHeader = true } = props as TableSkeletonProps;

    return (
      <div className={clsx(styles.tableContainer, className)}>
        {hasHeader && (
          <div className={styles.tableHeaderRow}>
            {generateArray(columns).map((_, i) => (
              <Skeleton key={`header-${i}`} className={styles.tableCell} />
            ))}
          </div>
        )}
        {generateArray(rows).map((_, rowIndex) => (
          <div key={`row-${rowIndex}`} className={styles.tableRow}>
            {generateArray(columns).map((_, colIndex) => (
              <Skeleton
                key={`cell-${rowIndex}-${colIndex}`}
                className={clsx(
                  styles.tableCell,
                  colIndex === 0 && { maxWidth: '200px' },
                  colIndex === columns - 1 && { maxWidth: '100px' }
                )}
              />
            ))}
          </div>
        ))}
      </div>
    );
  }

  if (type === 'card') {
    const { lines = 3, hasTitle = true } = props as CardSkeletonProps;

    return (
      <div className={clsx(styles.cardContainer, className)}>
        {hasTitle && <Skeleton className={styles.cardTitle} />}
        <div className={styles.cardLines}>
          {generateArray(lines).map((_, i) => (
            <Skeleton
              key={`line-${i}`}
              className={clsx(styles.cardLine, i === lines - 1 && styles.cardLineShort)}
            />
          ))}
        </div>
      </div>
    );
  }

  if (type === 'text') {
    const { lines = 1, width = 'full' } = props as TextSkeletonProps;
    const widthClass = WIDTH_PERCENTAGES[width];

    return (
      <div className={clsx(styles.textContainer, className)}>
        {generateArray(lines).map((_, i) => (
          <Skeleton
            key={`text-${i}`}
            className={clsx(
              styles.textLine,
              width === 'half' && styles.textLineHalf,
              width === 'quarter' && styles.textLineQuarter
            )}
            style={width !== 'full' ? { width: widthClass } : undefined}
          />
        ))}
      </div>
    );
  }

  if (type === 'fullpage') {
    const { text = 'Loading...' } = props as FullPageLoaderProps;

    return (
      <div className={clsx(styles.fullPageContainer, className)}>
        <div className={styles.fullPageContent}>
          <OrchLogo size={56} />
          <p className={clsx(styles.fullPageText, styles.textDark)}>
            {text}
          </p>
        </div>
      </div>
    );
  }

  return null;
};

export const TableLoader: React.FC<Omit<TableSkeletonProps, 'type'>> = (props) => (
  <Loader type="table" {...props} />
);

export const SpinnerLoader: React.FC<Omit<SpinnerLoaderProps, 'type'>> = (props) => (
  <Loader type="spinner" {...props} />
);

export const CardLoader: React.FC<Omit<CardSkeletonProps, 'type'>> = (props) => (
  <Loader type="card" {...props} />
);

export const TextLoader: React.FC<Omit<TextSkeletonProps, 'type'>> = (props) => (
  <Loader type="text" {...props} />
);

export const FullPageLoader: React.FC<Omit<FullPageLoaderProps, 'type'>> = (props) => (
  <Loader type="fullpage" {...props} />
);

export default Loader;
