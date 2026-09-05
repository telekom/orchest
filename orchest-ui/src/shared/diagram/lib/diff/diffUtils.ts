interface Canvas {
  viewbox: (viewbox?: unknown) => unknown;
  zoom: (zoom: number | string, center?: string) => void;
  addMarker: (elementId: string, className: string) => void;
}

interface ElementRegistry {
  get: (elementId: string) => { type: string } | undefined;
}

const EXCLUDED_TYPES = ["bpmn:Process", "bpmn:Participant", "bpmn:Lane", "bpmn:Collaboration"];

const shouldMarkElement = (registry: ElementRegistry, elementId: string): boolean => {
  const element = registry.get(elementId);
  return !!element && !EXCLUDED_TYPES.includes(element.type);
};

export const applyDiffMarkers = (
  differences: {
    _added?: Record<string, unknown>;
    _removed?: Record<string, unknown>;
    _changed?: Record<string, unknown>;
    _layoutChanged?: Record<string, unknown>;
  },
  oldCanvas: Canvas,
  newCanvas: Canvas,
  oldRegistry: ElementRegistry,
  newRegistry: ElementRegistry
) => {
  const stats = { added: 0, removed: 0, changed: 0, layoutChanged: 0 };

  const mark = (ids: string[], canvas: Canvas, registry: ElementRegistry, className: string) =>
    ids.forEach((id) => {
      if (shouldMarkElement(registry, id)) {
        try {
          canvas.addMarker(id, className);
        } catch {}
      }
    });

  const countMarked = (ids: string[], registry: ElementRegistry) =>
    ids.filter((id) => shouldMarkElement(registry, id)).length;

  if (differences._removed) {
    const ids = Object.keys(differences._removed);
    mark(ids, oldCanvas, oldRegistry, "diff-removed");
    stats.removed = countMarked(ids, oldRegistry);
  }

  if (differences._added) {
    const ids = Object.keys(differences._added);
    mark(ids, newCanvas, newRegistry, "diff-added");
    stats.added = countMarked(ids, newRegistry);
  }

  if (differences._changed) {
    const ids = Object.keys(differences._changed);
    mark(ids, oldCanvas, oldRegistry, "diff-changed");
    mark(ids, newCanvas, newRegistry, "diff-changed");
    stats.changed = countMarked(ids, oldRegistry);
  }

  if (differences._layoutChanged) {
    const ids = Object.keys(differences._layoutChanged);
    mark(ids, oldCanvas, oldRegistry, "diff-layout-changed");
    mark(ids, newCanvas, newRegistry, "diff-layout-changed");
    stats.layoutChanged = countMarked(ids, oldRegistry);
  }

  return stats;
};

export const syncViewboxes = (oldViewer: { get: (key: string) => unknown }, newViewer: { get: (key: string) => unknown }) => {
  const oldCanvas = oldViewer.get("canvas") as Canvas;
  const newCanvas = newViewer.get("canvas") as Canvas;
  const oldEventBus = oldViewer.get("eventBus") as { on: (event: string, callback: () => void) => void };
  const newEventBus = newViewer.get("eventBus") as { on: (event: string, callback: () => void) => void };

  let syncing = false;

  oldEventBus.on("canvas.viewbox.changed", () => {
    if (!syncing) {
      syncing = true;
      newCanvas.viewbox(oldCanvas.viewbox());
      syncing = false;
    }
  });

  newEventBus.on("canvas.viewbox.changed", () => {
    if (!syncing) {
      syncing = true;
      oldCanvas.viewbox(newCanvas.viewbox());
      syncing = false;
    }
  });
};

export const fitViewports = (oldCanvas: Canvas, newCanvas: Canvas) => {
  requestAnimationFrame(() => {
    try {
      oldCanvas.zoom("fit-viewport", "auto");
      newCanvas.zoom("fit-viewport", "auto");
    } catch {
      oldCanvas.zoom(0.8);
      newCanvas.zoom(0.8);
    }
  });
};
