import { m } from 'framer-motion';
import { memo, type ReactNode } from 'react';
import { pageTransition } from '@/shared/styles/design-tokens';
import clsx from 'clsx';
import styles from './PageTransition.module.css';

interface PageTransitionProps {
  children: ReactNode;
  className?: string;
}

/** Fade+slide wrapper for route content — requires MotionProvider at app root. */
export const PageTransition = memo(function PageTransition({
  children,
  className,
}: PageTransitionProps) {
  return (
    <m.div
      className={clsx(styles.root, className)}
      initial={pageTransition.initial}
      animate={pageTransition.animate}
      exit={pageTransition.exit}
      transition={pageTransition.transition}
    >
      {children}
    </m.div>
  );
});
