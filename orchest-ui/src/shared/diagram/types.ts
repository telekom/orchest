// Helper interfaces for common diagram services
export interface OverlaysService {
  add: (elementId: string, overlay: { position: { top?: number; right?: number; bottom?: number; left?: number }; html: string; type?: string; id?: string }) => string;
  remove: (overlayId: string) => void;
  get?: (overlayId: string) => unknown;
  clear?: () => void;
}

export interface ElementRegistryService {
  get: (elementId: string) => { type: string; businessObject?: { name?: string }; [key: string]: unknown } | undefined;
  getAll?: () => Array<{ id: string; type: string; [key: string]: unknown }>;
}

export interface EventBusService {
  on: (event: string, callback: (...args: unknown[]) => void) => void;
  off: (event: string, callback: (...args: unknown[]) => void) => void;
  fire?: (event: string, ...args: unknown[]) => void;
}

// Type definitions for BPMN/DMN viewer instances (external libraries without full TS support)
export interface DiagramViewer {
  importXML(xml: string): Promise<{ warnings: unknown[] } | unknown>;
  get(moduleName: "overlays"): OverlaysService;
  get(moduleName: "elementRegistry"): ElementRegistryService;
  get(moduleName: "eventBus"): EventBusService;
  get(moduleName: string): unknown;
  destroy(): void;
  // DMN specific methods
  getViews?: () => Array<{ type: string; [key: string]: unknown }>;
  getActiveView?: () => { type: string; [key: string]: unknown } | null;
  open?: (view: { type: string; [key: string]: unknown }, callback?: () => void) => Promise<unknown> | void;
  on?: (event: string, callback: (...args: unknown[]) => void) => void;
  off?: (event: string, callback: (...args: unknown[]) => void) => void;
  getActiveViewer?: () => unknown;
  saveXML?: (options: { format: boolean }) => Promise<{ xml?: string }>;
  saveSVG?: () => Promise<{ svg: string }>;
}
