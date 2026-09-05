/** Motion & glass design tokens for JS/TS consumers (Framer Motion, etc.) */

export const motion = {
  duration: {
    instant: 0.1,
    fast: 0.15,
    base: 0.25,
    slow: 0.4,
    page: 0.35,
  },
  ease: {
    default: [0.22, 1, 0.36, 1] as const,
    out: [0, 0, 0.2, 1] as const,
    in: [0.4, 0, 1, 1] as const,
    spring: { type: 'spring' as const, stiffness: 400, damping: 30 },
  },
} as const;

export const glass = {
  blur: { sm: 8, md: 16, lg: 24 },
  radius: { sm: '0.5rem', md: '0.75rem', lg: '1rem', xl: '1.25rem' },
} as const;

export const pageTransition = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -6 },
  transition: { duration: motion.duration.page, ease: motion.ease.default },
} as const;

export const staggerContainer = {
  initial: {},
  animate: {
    transition: { staggerChildren: 0.06, delayChildren: 0.08 },
  },
} as const;

export const staggerItem = {
  initial: { opacity: 0, y: 16 },
  animate: {
    opacity: 1,
    y: 0,
    transition: { duration: motion.duration.base, ease: motion.ease.default },
  },
} as const;
