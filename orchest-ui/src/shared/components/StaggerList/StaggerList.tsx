import { m } from 'framer-motion';
import { memo, Children, isValidElement, type ReactNode } from 'react';
import { staggerContainer, staggerItem } from '@/shared/styles/design-tokens';

interface StaggerListProps {
  children: ReactNode;
  className?: string;
}

/** Staggered entrance for grid/list children — requires MotionProvider at app root. */
export const StaggerList = memo(function StaggerList({
  children,
  className,
}: StaggerListProps) {
  return (
    <m.div
      className={className}
      variants={staggerContainer}
      initial="initial"
      animate="animate"
    >
      {Children.map(children, (child, i) =>
        isValidElement(child) ? (
          <m.div key={child.key ?? i} variants={staggerItem}>
            {child}
          </m.div>
        ) : null
      )}
    </m.div>
  );
});
