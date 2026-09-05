/**
 * Keyboard shortcut constants
 *
 * Centralized keyboard shortcuts for the application
 *
 * @example
 * ```typescript
 * // In help dialog
 * <kbd>{KEYBOARD_SHORTCUTS.SEARCH}</kbd> to open search
 * ```
 */
export const KEYBOARD_SHORTCUTS = {
  /** Open search dialog */
  SEARCH: 'Ctrl+K',

  /** Save current document */
  SAVE: 'Ctrl+S',

  /** Create new item */
  NEW: 'Ctrl+N',

  /** Refresh current view */
  REFRESH: 'F5',

  /** Toggle sidebar visibility */
  TOGGLE_SIDEBAR: 'Ctrl+B',

  /** Show help dialog */
  HELP: '?',
} as const;

export type KeyboardShortcutType = typeof KEYBOARD_SHORTCUTS[keyof typeof KEYBOARD_SHORTCUTS];
