import React from "react";

/**
 * Theme-compatible BPMN node-type icons for the instance history tree.
 * Shapes follow standard BPMN notation (see reference diagrams).
 * All icons use `currentColor` so they inherit the neutral grey from CSS.
 */

export interface BpmnIconProps {
  className?: string;
}

const SVG_PROPS = {
  viewBox: "0 0 24 24",
  xmlns: "http://www.w3.org/2000/svg",
  "aria-hidden": true,
} as const;

const S = {
  fill: "none",
  stroke: "currentColor",
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
};

const DIAMOND = "M12 2.5 21.5 12 12 21.5 2.5 12Z";
const TASK = { x: 3, y: 5, width: 18, height: 14, rx: 2.5 };

/* ── Events ─────────────────────────────────────────────── */

/** Start event — single thin circle. */
export const StartEventIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <circle cx="12" cy="12" r="9" {...S} strokeWidth={1.5} />
  </svg>
);

/** End event — single thick circle. */
export const EndEventIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <circle cx="12" cy="12" r="8.5" {...S} strokeWidth={3} />
  </svg>
);

/** Intermediate catch event — double thin circle. */
export const IntermediateCatchEventIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.4}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="6" />
    </g>
  </svg>
);

/** Intermediate throw event — double circle with filled centre. */
export const IntermediateThrowEventIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.4}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="6" />
    </g>
    <circle cx="12" cy="12" r="2.5" fill="currentColor" stroke="none" />
  </svg>
);

/** Boundary event — double circle with dashed outer ring. */
export const BoundaryEventIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <circle cx="12" cy="12" r="9" {...S} strokeWidth={1.4} strokeDasharray="3 2" />
    <circle cx="12" cy="12" r="6" {...S} strokeWidth={1.4} />
  </svg>
);

/* ── Tasks ────────────────────────────────────────────── */

/** Generic / undefined task — plain rounded rectangle. */
export const TaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
  </svg>
);

/** User task — person silhouette in top-left. */
export const UserTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <g {...S} strokeWidth={1.3}>
      <circle cx="7.5" cy="8.5" r="1.6" />
      <path d="M5 13.5c0-1.8 1.1-2.5 2.5-2.5s2.5.7 2.5 2.5" />
    </g>
  </svg>
);

/** Service task — two interlocking gears in top-left. */
export const ServiceTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <g fill="currentColor" stroke="none">
      <circle cx="6.8" cy="8.8" r="2.2" />
      <circle cx="9.8" cy="10.8" r="1.8" />
    </g>
    <g {...S} strokeWidth={0.8}>
      <circle cx="6.8" cy="8.8" r="1.2" />
      <circle cx="9.8" cy="10.8" r="0.9" />
    </g>
  </svg>
);

/** Receive task — envelope outline in top-left. */
export const ReceiveTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <g {...S} strokeWidth={1.2}>
      <rect x="5" y="7" width="6" height="4.5" rx="0.5" />
      <path d="M5 7.5 8 10 11 7.5" />
    </g>
  </svg>
);

/** Send task — filled envelope in top-left. */
export const SendTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <rect x="5" y="7" width="6" height="4.5" rx="0.5" fill="currentColor" stroke="none" />
    <path d="M5 7.5 8 10 11 7.5" {...S} strokeWidth={1} fill="none" />
  </svg>
);

/** Business rule task — table/grid in top-left. */
export const BusinessRuleTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <g {...S} strokeWidth={1}>
      <rect x="5" y="7" width="6" height="5" />
      <path d="M5 9h6M8 7v5" />
    </g>
  </svg>
);

/** Script task — scroll with text lines in top-left. */
export const ScriptTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <g {...S} strokeWidth={1.1}>
      <path d="M5.5 7.5c0-1 .8-1.5 1.5-1.5h3c.7 0 1.5.5 1.5 1.5v4.5c0 1-.8 1.5-1.5 1.5h-3c-.7 0-1.5-.5-1.5-1.5z" />
      <path d="M7 7v5.5" />
      <path d="M9.5 9h2M9.5 10.5h1.5M9.5 12h2" />
    </g>
  </svg>
);

/** Manual task — hand in top-left. */
export const ManualTaskIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <path
      d="M6.5 12.5V9.5c0-.8.5-1.2 1-1.2s1 .4 1 1.2v1.5M8.5 9.5V8.5c0-.8.5-1.2 1-1.2s1 .4 1 1.2v4M10.5 8.5V8c0-.8.5-1.2 1-1.2s1 .4 1 1.2v5.5c0 1.2-.8 2-2 2H8c-1.2 0-2-.5-2.5-1.2l-.5-.8"
      {...S}
      strokeWidth={1.2}
    />
  </svg>
);

/* ── Subprocesses ─────────────────────────────────────── */

/** Subprocess marker — small [+] box at bottom-centre. */
const SubProcessMarker = () => (
  <g {...S} strokeWidth={1.2}>
    <rect x="9.5" y="15.5" width="5" height="3.5" rx="0.5" />
    <path d="M12 16.5v1.5M10.5 17.25h3" />
  </g>
);

/** Embedded subprocess — rounded rect with [+] marker. */
export const SubProcessIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect {...TASK} {...S} strokeWidth={1.5} />
    <SubProcessMarker />
  </svg>
);

/** Call activity — thicker border with [+] marker. */
export const CallActivityIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <rect x="3.5" y="5.5" width="17" height="13" rx="2.5" {...S} strokeWidth={2.5} />
    <SubProcessMarker />
  </svg>
);

/* ── Gateways ─────────────────────────────────────────── */

/** Exclusive (XOR) gateway — diamond with X. */
export const ExclusiveGatewayIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.5}>
      <path d={DIAMOND} />
      <path d="m9.5 9.5 5 5M14.5 9.5l-5 5" strokeWidth={2} />
    </g>
  </svg>
);

/** Parallel (AND) gateway — diamond with +. */
export const ParallelGatewayIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.5}>
      <path d={DIAMOND} />
      <path d="M12 8.5v7M8.5 12h7" strokeWidth={2} />
    </g>
  </svg>
);

/** Inclusive (OR) gateway — diamond with circle. */
export const InclusiveGatewayIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.5}>
      <path d={DIAMOND} />
      <circle cx="12" cy="12" r="3.5" strokeWidth={2} />
    </g>
  </svg>
);

/** Event-based gateway — diamond with pentagon inside circle. */
export const EventBasedGatewayIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.5}>
      <path d={DIAMOND} />
      <circle cx="12" cy="12" r="4" />
      <path d="M12 9.2 14.2 12.8H9.8Z" strokeWidth={1.3} />
    </g>
  </svg>
);

/* ── Root process (synthetic history node) ──────────── */

export const ProcessDocumentIcon: React.FC<BpmnIconProps> = ({ className }) => (
  <svg {...SVG_PROPS} className={className}>
    <g {...S} strokeWidth={1.5}>
      <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" />
      <path d="M14 3v5h5" />
    </g>
  </svg>
);

/* ── Icon lookup ────────────────────────────────────── */

type IconComponent = React.FC<BpmnIconProps>;

const NODE_TYPE_ICON_MAP: Record<string, IconComponent> = {
  PROCESS: ProcessDocumentIcon,

  START_EVENT: StartEventIcon,
  END_EVENT: EndEventIcon,
  BOUNDARY_EVENT: BoundaryEventIcon,
  INTERMEDIATE_CATCH_EVENT: IntermediateCatchEventIcon,
  INTERMEDIATE_THROW_EVENT: IntermediateThrowEventIcon,

  TASK: TaskIcon,
  USER_TASK: UserTaskIcon,
  SERVICE_TASK: ServiceTaskIcon,
  RECEIVE_TASK: ReceiveTaskIcon,
  SEND_TASK: SendTaskIcon,
  BUSINESS_RULE_TASK: BusinessRuleTaskIcon,
  SCRIPT_TASK: ScriptTaskIcon,
  MANUAL_TASK: ManualTaskIcon,

  SUB_PROCESS: SubProcessIcon,
  CALL_ACTIVITY: CallActivityIcon,

  EXCLUSIVE_GATEWAY: ExclusiveGatewayIcon,
  PARALLEL_GATEWAY: ParallelGatewayIcon,
  INCLUSIVE_GATEWAY: InclusiveGatewayIcon,
  EVENT_BASED_GATEWAY: EventBasedGatewayIcon,
};

/** Returns the theme-compatible icon component for a given node type. */
export const getBpmnNodeIcon = (nodeType?: string): IconComponent =>
  (nodeType && NODE_TYPE_ICON_MAP[nodeType.toUpperCase()]) || TaskIcon;
