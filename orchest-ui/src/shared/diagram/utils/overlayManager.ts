/**
 * Overlay management utilities for BPMN/DMN diagrams
 * Provides safe overlay operations and batch management
 */

export interface OverlayConfig {
  position: { top?: number; bottom?: number; left?: number; right?: number };
  html: string | HTMLElement;
  type?: string;
  /** When false, overlay stays constant screen size (diagram-js counter-scale). */
  scale?: boolean | { min?: number; max?: number };
}

export interface OverlayManager {
  add(elementId: string, config: OverlayConfig): string;
  remove(id: string): void;
}

/**
 * Safely removes an overlay, suppressing errors if overlay doesn't exist
 */
export const removeOverlaySafe = (overlays: unknown, id: string): void => {
  try {
    (overlays as OverlayManager).remove(id);
  } catch {
    // Overlay may have already been removed
  }
};

/**
 * Clears all overlays in a Set and empties the Set
 */
export const clearOverlaySet = (overlays: unknown, overlaySet: Set<string>): void => {
  overlaySet.forEach(id => removeOverlaySafe(overlays, id));
  overlaySet.clear();
};

/**
 * Adds an overlay and tracks its ID in a Set
 */
export const addTrackedOverlay = (
  overlays: unknown,
  overlaySet: Set<string>,
  elementId: string,
  config: OverlayConfig
): void => {
  const overlayId = (overlays as OverlayManager).add(elementId, config);
  overlaySet.add(overlayId);
};
