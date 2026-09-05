/**
 * Consistent Orchest glyph set — 16×16 viewBox, 1.5px stroke language
 * (Lucide-like: round caps/joins, geometric, single visual weight).
 */
export type IconDef = {
  /** Outline paths rendered with stroke (no fill). */
  strokes: string[];
  /** Optional solid fills (e.g. small dots) rendered with fill. */
  fills?: string[];
};

export const ICON_DEFS = {
  service: {
    strokes: [
      // service chip / API node
      "M4.4 4.4h7.2v7.2H4.4z",
      "M5.8 2.8v1.6M8 2.8v1.6M10.2 2.8v1.6",
      "M5.8 11.6v1.6M8 11.6v1.6M10.2 11.6v1.6",
      "M2.8 5.8h1.6M2.8 8h1.6M2.8 10.2h1.6",
      "M11.6 5.8h1.6M11.6 8h1.6M11.6 10.2h1.6",
      "M6.2 6.2h3.6v3.6H6.2z",
    ],
  },
  user: {
    strokes: [
      "M8 3.2a2.3 2.3 0 1 1 0 4.6 2.3 2.3 0 0 1 0-4.6z",
      "M3.6 13.2c0-2.3 1.95-3.6 4.4-3.6s4.4 1.3 4.4 3.6",
    ],
  },
  script: {
    strokes: [
      // code brackets { }
      "M6.2 3.8c-1.2.2-1.8 1-1.8 2.2v1.2c0 .7-.3 1.1-.9 1.3.6.2.9.6.9 1.3v1.2c0 1.2.6 2 1.8 2.2",
      "M9.8 3.8c1.2.2 1.8 1 1.8 2.2v1.2c0 .7.3 1.1.9 1.3-.6.2-.9.6-.9 1.3v1.2c0 1.2-.6 2-1.8 2.2",
    ],
  },
  businessRule: {
    strokes: [
      // decision table
      "M3.4 3.6h9.2v8.8H3.4z",
      "M3.4 6.2h9.2M3.4 8.8h9.2M7.4 6.2v6.2",
    ],
  },
  send: {
    strokes: [
      // paper plane
      "M2.8 8l10.4-4.4L8.6 8l4.6 4.4L2.8 8z",
      "M2.8 8h5.8",
    ],
  },
  receive: {
    strokes: [
      // inbox
      "M3 5.2h10v6.6H3z",
      "M3 5.2l5 3.6 5-3.6",
      "M3 11.8h10",
    ],
  },
  manual: {
    strokes: [
      // hand / pointer simplified
      "M6.4 3.4v4.2",
      "M6.4 7.6H4.8c-.9 0-1.6.7-1.6 1.6v.4c0 1.8 1.5 3.2 3.3 3.2h3.2c1.5 0 2.7-1.2 2.7-2.7V8.2c0-.8-.6-1.4-1.4-1.4h-.6",
      "M8 4.2v3.4M9.6 5v2.6",
    ],
  },
  callActivity: {
    strokes: [
      // nested boxes
      "M3.2 3.4h6.4v6.4H3.2z",
      "M6.4 6.4h6.4v6.4H6.4z",
    ],
  },
  message: {
    strokes: [
      "M2.8 4.4h10.4v7.2H2.8z",
      "M2.8 4.4l5.2 3.8 5.2-3.8",
    ],
  },
  timer: {
    strokes: [
      "M8 2.8a5.2 5.2 0 1 1 0 10.4A5.2 5.2 0 0 1 8 2.8z",
      "M8 5.2v3.2l2.2 1.3",
    ],
  },
  error: {
    strokes: [
      "M8 2.8 13.4 13H2.6L8 2.8z",
      "M8 6.6v2.8",
    ],
    fills: ["M8 11.2h.01"],
  },
  exclusive: {
    strokes: [
      "M4.2 4.2l7.6 7.6",
      "M11.8 4.2l-7.6 7.6",
    ],
  },
  parallel: {
    strokes: [
      "M8 3.2v9.6",
      "M3.2 8h9.6",
    ],
  },
  inclusive: {
    strokes: [
      "M8 3.4a4.6 4.6 0 1 1 0 9.2 4.6 4.6 0 0 1 0-9.2z",
    ],
  },
  eventGateway: {
    strokes: [
      "M8 2.6l1.7 3.4 3.8.6-2.75 2.7.65 3.8L8 11.4l-3.4 1.7.65-3.8-2.75-2.7 3.8-.6L8 2.6z",
      "M8 5.6a2.4 2.4 0 1 1 0 4.8 2.4 2.4 0 0 1 0-4.8z",
    ],
  },
} as const satisfies Record<string, IconDef>;

/** @deprecated Prefer ICON_DEFS — kept for map completeness checks. */
export const ICON_PATHS = Object.fromEntries(
  Object.entries(ICON_DEFS).map(([key, def]) => [key, def.strokes.join(" ")])
) as Record<keyof typeof ICON_DEFS, string>;

export type OrchestIconKey = keyof typeof ICON_DEFS;

/** Maps BPMN `$type` (and event kinds) to icon keys. */
export const TYPE_ICON_MAP: Record<string, OrchestIconKey> = {
  "bpmn:ServiceTask": "service",
  "bpmn:UserTask": "user",
  "bpmn:ScriptTask": "script",
  "bpmn:BusinessRuleTask": "businessRule",
  "bpmn:SendTask": "send",
  "bpmn:ReceiveTask": "receive",
  "bpmn:ManualTask": "manual",
  "bpmn:CallActivity": "callActivity",
  "bpmn:ExclusiveGateway": "exclusive",
  "bpmn:ParallelGateway": "parallel",
  "bpmn:InclusiveGateway": "inclusive",
  "bpmn:EventBasedGateway": "eventGateway",
  message: "message",
  timer: "timer",
  error: "error",
};

export const PHASE1_ELEMENT_TYPES = [
  "bpmn:ServiceTask",
  "bpmn:UserTask",
  "bpmn:ScriptTask",
  "bpmn:BusinessRuleTask",
  "bpmn:SendTask",
  "bpmn:ReceiveTask",
  "bpmn:ManualTask",
  "bpmn:CallActivity",
  "bpmn:StartEvent",
  "bpmn:EndEvent",
  "bpmn:IntermediateThrowEvent",
  "bpmn:IntermediateCatchEvent",
  "bpmn:BoundaryEvent",
  "bpmn:ExclusiveGateway",
  "bpmn:ParallelGateway",
  "bpmn:InclusiveGateway",
  "bpmn:EventBasedGateway",
] as const;
