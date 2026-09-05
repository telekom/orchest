import { useEffect, useCallback } from 'react';
import { KEYBOARD_SHORTCUTS, matchesShortcut } from '../constants/keyboard';

export interface KeyboardShortcutsConfig {
  onNextTask?: () => void;
  onPrevTask?: () => void;
  onClaimTask?: () => void;
  onCompleteTask?: () => void;
  onFocusSearch?: () => void;
  onShowHelp?: () => void;
  enabled?: boolean;
}

export function useKeyboardShortcuts(config: KeyboardShortcutsConfig): void {
  const {
    onNextTask,
    onPrevTask,
    onClaimTask,
    onCompleteTask,
    onFocusSearch,
    onShowHelp,
    enabled = true,
  } = config;

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if (!enabled) return;

    const target = event.target as HTMLElement;
    const isInputActive = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable;

    if (isInputActive) {
      if (matchesShortcut(event, KEYBOARD_SHORTCUTS.FOCUS_SEARCH)) {
        event.preventDefault();
        onFocusSearch?.();
      }
      return;
    }

    if (matchesShortcut(event, KEYBOARD_SHORTCUTS.NEXT_TASK)) {
      event.preventDefault();
      onNextTask?.();
    } else if (matchesShortcut(event, KEYBOARD_SHORTCUTS.PREV_TASK)) {
      event.preventDefault();
      onPrevTask?.();
    } else if (matchesShortcut(event, KEYBOARD_SHORTCUTS.CLAIM_TASK)) {
      event.preventDefault();
      onClaimTask?.();
    } else if (matchesShortcut(event, KEYBOARD_SHORTCUTS.COMPLETE_TASK)) {
      event.preventDefault();
      onCompleteTask?.();
    } else if (matchesShortcut(event, KEYBOARD_SHORTCUTS.FOCUS_SEARCH)) {
      event.preventDefault();
      onFocusSearch?.();
    } else if (matchesShortcut(event, KEYBOARD_SHORTCUTS.SHOW_HELP)) {
      event.preventDefault();
      onShowHelp?.();
    }
  }, [enabled, onNextTask, onPrevTask, onClaimTask, onCompleteTask, onFocusSearch, onShowHelp]);

  useEffect(() => {
    if (!enabled) return;

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [enabled, handleKeyDown]);
}
