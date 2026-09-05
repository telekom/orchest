/**
 * Keyboard shortcuts for task management feature
 *
 * These shortcuts improve accessibility and power user experience
 */

export const KEYBOARD_SHORTCUTS = {
  /** Navigate to next task in queue */
  NEXT_TASK: {
    key: 'j',
    label: 'Next Task',
    description: 'Select next task in queue',
  },

  /** Navigate to previous task in queue */
  PREV_TASK: {
    key: 'k',
    label: 'Previous Task',
    description: 'Select previous task in queue',
  },

  /** Claim selected task */
  CLAIM_TASK: {
    key: 'c',
    modifiers: ['ctrl'],
    label: 'Claim Task',
    description: 'Claim the currently selected task',
  },

  /** Complete selected task */
  COMPLETE_TASK: {
    key: 'Enter',
    modifiers: ['ctrl'],
    label: 'Complete Task',
    description: 'Complete the currently selected task',
  },

  /** Focus on task search */
  FOCUS_SEARCH: {
    key: '/',
    label: 'Focus Search',
    description: 'Focus on task search field',
  },

  /** Show keyboard shortcuts help */
  SHOW_HELP: {
    key: '?',
    label: 'Show Help',
    description: 'Display keyboard shortcuts help',
  },
} as const;

/**
 * Check if a keyboard event matches a shortcut
 */
export function matchesShortcut(
  event: KeyboardEvent,
  shortcut: typeof KEYBOARD_SHORTCUTS[keyof typeof KEYBOARD_SHORTCUTS]
): boolean {
  const { key, modifiers = [] } = shortcut;

  // Check if key matches
  if (event.key.toLowerCase() !== key.toLowerCase()) {
    return false;
  }

  // Check modifiers
  const ctrlRequired = modifiers.includes('ctrl');
  const shiftRequired = modifiers.includes('shift');
  const altRequired = modifiers.includes('alt');
  const metaRequired = modifiers.includes('meta');

  if (ctrlRequired && !event.ctrlKey) return false;
  if (!ctrlRequired && event.ctrlKey) return false;

  if (shiftRequired && !event.shiftKey) return false;
  if (!shiftRequired && event.shiftKey) return false;

  if (altRequired && !event.altKey) return false;
  if (!altRequired && event.altKey) return false;

  if (metaRequired && !event.metaKey) return false;
  if (!metaRequired && event.metaKey) return false;

  return true;
}

/**
 * Format shortcut for display
 */
export function formatShortcut(
  shortcut: typeof KEYBOARD_SHORTCUTS[keyof typeof KEYBOARD_SHORTCUTS]
): string {
  const { key, modifiers = [] } = shortcut;
  const parts: string[] = [];

  if (modifiers.includes('ctrl')) parts.push('Ctrl');
  if (modifiers.includes('shift')) parts.push('Shift');
  if (modifiers.includes('alt')) parts.push('Alt');
  if (modifiers.includes('meta')) parts.push('⌘');

  parts.push(key.toUpperCase());

  return parts.join('+');
}
