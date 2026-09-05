import clsx from 'clsx';
import { forwardRef, type HTMLAttributes, type ReactNode } from 'react';
import styles from './GlassSurface.module.css';

type GlassVariant = 'default' | 'subtle' | 'strong';
type GlassElement = 'div' | 'section' | 'article' | 'aside' | 'header' | 'footer' | 'nav' | 'main';

export interface GlassSurfaceProps extends HTMLAttributes<HTMLElement> {
  variant?: GlassVariant;
  interactive?: boolean;
  highlight?: boolean;
  as?: GlassElement;
  children?: ReactNode;
}

const variantClass: Record<GlassVariant, string> = {
  default: 'glass',
  subtle: 'glass-subtle',
  strong: 'glass-strong',
};

export const GlassSurface = forwardRef<HTMLElement, GlassSurfaceProps>(
  function GlassSurface(
    {
      variant = 'default',
      interactive = false,
      highlight = false,
      as: Tag = 'div',
      className,
      children,
      ...props
    },
    ref
  ) {
    return (
      <Tag
        ref={ref as never}
        className={clsx(
          styles.surface,
          variantClass[variant],
          interactive && 'glass-interactive',
          highlight && 'glass-highlight',
          className
        )}
        {...props}
      >
        {children}
      </Tag>
    );
  }
);
