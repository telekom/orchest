type ModuleType = Record<string, unknown>;

declare module '*.module.css' {
  const classes: Record<string, string>;
  export default classes;
}

declare module 'inherits' {
  function inherits(ctor: unknown, superCtor: unknown): void;
  export default inherits;
}

declare module 'tiny-svg' {
  export function append(parent: SVGElement, child: SVGElement): SVGElement;
  export function attr(node: SVGElement, attrs: Record<string, string | number>): SVGElement;
  export function create(name: string): SVGElement;
  export function remove(node: SVGElement): void;
}

declare module '@bpmn-io/element-templates-icons-renderer' {
  const TemplateIconRendererModule: ModuleType;
  export default TemplateIconRendererModule;
}

declare module 'bpmn-js-properties-panel' {
  export const BpmnPropertiesPanelModule: ModuleType;
  export const BpmnPropertiesProviderModule: ModuleType;
  export const ZeebePropertiesProviderModule: ModuleType;
}

declare module 'bpmn-js-element-templates' {
  export const CloudElementTemplatesPropertiesProviderModule: ModuleType;
  export const CloudElementTemplatesCoreModule: ModuleType;
  export const CloudElementTemplatesValidator: unknown;
  export const ElementTemplatesPropertiesProviderModule: ModuleType;
}

declare module 'dmn-js-properties-panel' {
  export const DmnPropertiesPanelModule: ModuleType;
  export const DmnPropertiesProviderModule: ModuleType;
}

declare module 'dmn-js/lib/Modeler' {
  interface DmnView {
    type: string;
    element?: { id: string };
  }
  
  const DmnJS: new (options: Record<string, unknown>) => {
    importXML: (xml: string) => Promise<{ warnings: unknown[] }>;
    saveXML: (options: { format: boolean }) => Promise<{ xml?: string }>;
    saveSVG: () => Promise<{ svg: string }>;
    get: (moduleName: string) => unknown;
    destroy: () => void;
    on: (event: string, callback: (...args: unknown[]) => void) => void;
    attachTo: (container: HTMLElement) => void;
    getViews: () => DmnView[];
    open: (view: DmnView, callback?: () => void) => void;
    getActiveViewer: () => { get: (moduleName: string) => unknown } | undefined;
  };
  export default DmnJS;
}

declare module 'dmn-js/dist/dmn-navigated-viewer.production.min' {
  export default class DmnJS {
    constructor(options: Record<string, unknown>);

    importXML(xml: string): Promise<{ warnings: unknown[] }>;
    get(moduleName: string): unknown;
    destroy(): void;
    getViews(): Array<{ type: string; [key: string]: unknown }>;
    getActiveView(): { type: string; [key: string]: unknown } | null;
    open(view: { type: string; [key: string]: unknown }): Promise<void>;
    on(event: string, callback: (...args: unknown[]) => void): void;
    off(event: string, callback: (...args: unknown[]) => void): void;
    saveXML(options: { format: boolean }): Promise<{ xml: string }>;
    getActiveViewer(): unknown;
  }
}

declare module '@/features/workflow-modeling/BpmnConnectorConfiguration' {
  const ConnectorsExtensionModule: ModuleType;
  export default ConnectorsExtensionModule;
}

declare module 'camunda-bpmn-js-behaviors/lib/camunda-cloud' {
  const CamundaBehaviorsModule: ModuleType;
  export default CamundaBehaviorsModule;
}

declare module 'bpmnlint/lib/linter' {
  interface LinterOptions {
    resolver: {
      resolveRule: (pkg: string, ruleName: string) => unknown;
      resolveConfig: (pkg: string, configName: string) => unknown;
    };
    config?: Record<string, unknown>;
  }

  interface LintReport {
    id: string;
    message: string;
    category: string;
    node?: { id: string };
  }

  type LintResults = Record<string, LintReport[]>;

  class Linter {
    constructor(options: LinterOptions);
    lint(moddleRoot: unknown, config?: Record<string, unknown>): Promise<LintResults>;
  }

  export default Linter;
}

declare module 'bpmnlint/rules/*' {
  const rule: unknown;
  export default rule;
}

declare module '@flowskin-bpmn/flowskin-bpmn' {
  interface FlowSkinOptions {
    container: string | HTMLElement;
    theme?: 'dark' | 'light';
    hoverCard?: boolean;
  }

  interface EdgeAnimateOptions {
    type?: 'parallel' | 'sequential' | 'loading';
    color?: string;
    speed?: number;
  }

  interface EdgeHighlightOptions {
    color?: string;
    width?: number;
    glow?: boolean;
  }

  type NodeState = 'running' | 'completed' | 'incident' | 'hold';

  interface FlowSkinBPMN {
    loadXml(xml: string): Promise<void>;
    setTheme(theme: 'dark' | 'light'): void;
    getTheme(): 'dark' | 'light';
    getXml(): string;
    highlightEdge(edgeId: string, options?: EdgeHighlightOptions): (() => void) | null;
    highlightEdges(edgeIds: string[], options?: EdgeHighlightOptions): () => void;
    animateEdges(edgeIds: string[], options?: EdgeAnimateOptions): () => void;
    stopAllEdgeAnimations(): void;
    setNodeStates(states: Record<string, NodeState>): void;
    clearNodeStates(elementIds?: string[]): void;
    getViewer(): unknown;
    destroy(): void;
  }

  interface FlattenBpmnChild {
    processId: string;
    name: string;
    xml: string;
    executedEdgeIds: string[];
    children?: FlattenBpmnChild[];
  }

  interface FlattenBpmnOptions {
    parent: {
      xml: string;
      executedEdgeIds: string[];
    };
    children?: FlattenBpmnChild[];
  }

  export function createFlowSkinBPMN(options: FlowSkinOptions): FlowSkinBPMN;
  export function flattenBpmn(options: FlattenBpmnOptions): Promise<string>;
  export function setTheme(t: 'dark' | 'light'): void;
  export function isDark(): boolean;
  export function highlightEdge(viewer: unknown, edgeId: string, options?: EdgeHighlightOptions): (() => void) | null;
  export function highlightEdges(viewer: unknown, edgeIds: string[], options?: EdgeHighlightOptions): () => void;
  export function animateEdges(viewer: unknown, edgeIds: string[], options?: EdgeAnimateOptions): () => void;
  export function stopAllEdgeAnimations(): void;
  export function setupHoverCard(viewer: unknown): void;
  export function setNodeStates(viewer: unknown, states: Record<string, NodeState>): void;
  export function clearNodeStates(viewer: unknown, elementIds?: string[]): void;
}

declare module '@flowskin-bpmn/flowskin-bpmn/styles' {
  const styles: string;
  export default styles;
}

declare module '@flowskin-bpmn/flowskin-bpmn/modeler' {
  interface FlowSkinModelerOptions {
    container: string | HTMLElement;
    theme?: 'dark' | 'light';
    hoverCard?: boolean;
    keyboard?: boolean;
  }

  interface FlowSkinModeler {
    loadXml(xml: string): Promise<void>;
    createNewDiagram(): Promise<void>;
    saveXml(): Promise<string>;
    saveSvg(): Promise<string>;
    undo(): void;
    redo(): void;
    canUndo(): boolean;
    canRedo(): boolean;
    setTheme(t: 'dark' | 'light'): Promise<void>;
    getTheme(): 'dark' | 'light';
    getXml(): string;
    highlightEdge(edgeId: string, options?: { color?: string; width?: number; glow?: boolean }): (() => void) | null;
    highlightEdges(edgeIds: string[], options?: { color?: string; width?: number; glow?: boolean }): () => void;
    animateEdges(edgeIds: string[], options?: { type?: 'parallel' | 'sequential' | 'loading'; color?: string; speed?: number }): () => void;
    stopAllEdgeAnimations(): void;
    setNodeStates(states: Record<string, 'running' | 'completed' | 'incident' | 'hold'>): void;
    clearNodeStates(elementIds?: string[]): void;
    getModeling(): unknown;
    getElementRegistry(): unknown;
    getModeler(): unknown;
    on(event: string, callback: (...args: unknown[]) => void): void;
    off(event: string, callback: (...args: unknown[]) => void): void;
    destroy(): void;
  }

  export function createFlowSkinModeler(options: FlowSkinModelerOptions): FlowSkinModeler;
}
