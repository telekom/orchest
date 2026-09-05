import { LazyMotion, domAnimation } from 'framer-motion';
import type { ReactNode } from 'react';

interface MotionProviderProps {
  children: ReactNode;
}

/** Lazy-loads Framer Motion features once at app root (~60% bundle savings vs full). */
export function MotionProvider({ children }: MotionProviderProps) {
  return <LazyMotion features={domAnimation}>{children}</LazyMotion>;
}
