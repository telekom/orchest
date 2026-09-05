import { getBusinessObject, is, isAny } from "bpmn-js/lib/util/ModelUtil";
import { isTypedEvent } from "bpmn-js/lib/draw/BpmnRenderUtil";
import { PHASE1_ELEMENT_TYPES, TYPE_ICON_MAP, type OrchestIconKey } from "./icons/iconPaths";
import { ORCHEST_BPMN_THEME, type OrchestAccentKey } from "./theme";

type BpmnLike = {
  type?: string;
  labelTarget?: unknown;
  businessObject?: {
    get?: (key: string) => unknown;
    eventDefinitions?: unknown[];
    $type?: string;
  };
  width?: number;
  height?: number;
};

export const hasModelerTemplateIcon = (element: BpmnLike): boolean => {
  try {
    const icon = getBusinessObject(element)?.get?.("zeebe:modelerTemplateIcon");
    return Boolean(icon);
  } catch {
    return false;
  }
};

export const isPhase1Element = (element: BpmnLike): boolean => {
  if (!element || element.labelTarget) return false;
  return isAny(element, [...PHASE1_ELEMENT_TYPES]);
};

export const shouldOrchestRender = (element: BpmnLike): boolean => {
  if (!isPhase1Element(element)) return false;
  // Let element-templates-icons-renderer own templated elements.
  if (hasModelerTemplateIcon(element)) return false;
  return true;
};

export const getEventKind = (element: BpmnLike): "message" | "timer" | "error" | "none" => {
  const bo = getBusinessObject(element);
  if (!bo) return "none";
  if (isTypedEvent(bo, "bpmn:MessageEventDefinition")) return "message";
  if (isTypedEvent(bo, "bpmn:TimerEventDefinition")) return "timer";
  if (isTypedEvent(bo, "bpmn:ErrorEventDefinition")) return "error";
  return "none";
};

export const resolveIconKey = (element: BpmnLike): OrchestIconKey | null => {
  if (is(element, "bpmn:Event")) {
    const kind = getEventKind(element);
    if (kind === "none") return null;
    return TYPE_ICON_MAP[kind] ?? null;
  }
  return TYPE_ICON_MAP[element.type ?? ""] ?? null;
};

const TYPE_ACCENT_MAP: Record<string, OrchestAccentKey> = {
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
};

export const resolveAccentKey = (element: BpmnLike): OrchestAccentKey => {
  const fromType = TYPE_ACCENT_MAP[element.type ?? ""];
  if (fromType) return fromType;

  if (is(element, "bpmn:Event")) {
    const kind = getEventKind(element);
    if (kind === "message") return "message";
    if (kind === "timer") return "timer";
    if (kind === "error") return "error";
    return "noneEvent";
  }

  return "noneEvent";
};

export const getAccentColor = (element: BpmnLike): string =>
  ORCHEST_BPMN_THEME.colors[resolveAccentKey(element)];
