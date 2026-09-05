export enum ModelerTypes {
  BPMN = "bpmn",
  DMN = "dmn",
  FORM = "form",
}

export type ModelerType = ModelerTypes.BPMN | ModelerTypes.DMN | ModelerTypes.FORM;

export const MODELER_TITLES: Record<ModelerType, string> = {
  [ModelerTypes.BPMN]: "Process",
  [ModelerTypes.DMN]: "Decision",
  [ModelerTypes.FORM]: "Form",
} as const;

export const MODELER_CONSTANTS = {
  CANVAS: {
    MIN_HEIGHT: 600,
    PADDING: 20,
    BREAKPOINT_LARGE: 1800,
  },

  POLLING: {
    CODE_GENERATION_INTERVAL: 5000,
    CODE_GENERATION_TIMEOUT: 300000, // 5 minutes
  },

  PROPERTIES_PANEL: {
    DEFAULT_VISIBLE: true,
  },

  TRANSITIONS: {
    PANEL_DURATION: 500,
  },

  DMN: {
    DEBOUNCE_TIMEOUT: 150,
  },
} as const;

export const EXPORT_FORMATS = {
  BPMN: [
    { value: "xml", label: "BPMN XML" },
    { value: "svg", label: "SVG Image" },
    { value: "png", label: "PNG Image" },
  ],
  DMN: [
    { value: "xml", label: "DMN XML" },
    { value: "svg", label: "SVG Image" },
  ],
  FORM: [
    { value: "json", label: "JSON Schema" },
  ],
} as const;

export const ACCEPTED_FILE_TYPES = {
  BPMN: ".bpmn,.xml",
  DMN: ".dmn,.xml",
  FORM: ".json",
} as const;

export const DIALOG_MESSAGES = {
  IMPORT: {
    BPMN: {
      TITLE: "Import BPMN Diagram",
      DRAG_DROP_TEXT: "Drag and drop a BPMN file here",
    },
    DMN: {
      TITLE: "Import DMN Diagram",
      DRAG_DROP_TEXT: "Drag and drop a DMN file here",
    },
    FORM: {
      TITLE: "Import Form Schema",
      DRAG_DROP_TEXT: "Drag and drop a form schema JSON file here",
    },
  },
  CONFIRM: {
    NEW_DIAGRAM: "Are you sure you want to create a new diagram? Any unsaved changes will be lost.",
  },
} as const;