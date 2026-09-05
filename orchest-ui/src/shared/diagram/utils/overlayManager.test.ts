import { describe, expect, it, vi } from 'vitest';
import { addTrackedOverlay } from './overlayManager';

describe('addTrackedOverlay', () => {
  it('forwards scale: false on the overlay config', () => {
    const add = vi.fn().mockReturnValue('overlay-1');
    const overlays = { add, remove: vi.fn() };
    const set = new Set<string>();
    const html = document.createElement('div');

    addTrackedOverlay(overlays, set, 'Task_1', {
      position: { bottom: 0, left: 0 },
      html,
      type: 'info-card',
      scale: false,
    });

    expect(add).toHaveBeenCalledWith('Task_1', {
      position: { bottom: 0, left: 0 },
      html,
      type: 'info-card',
      scale: false,
    });
    expect(set.has('overlay-1')).toBe(true);
  });
});
