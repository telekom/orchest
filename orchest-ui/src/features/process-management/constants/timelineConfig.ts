/**
 * Timeline configuration constants
 * Used by TaskTimeline component for animation and loading states
 */
export const TIMELINE_CONFIG = {
  /** Animation delay per item in milliseconds */
  ANIMATION_DELAY_PER_ITEM: 20,

  /** Number of skeleton items to show while loading */
  LOADING_ITEMS_COUNT: 3,
} as const;

export type TimelineConfigType = typeof TIMELINE_CONFIG;
