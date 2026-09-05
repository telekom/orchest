import BaseRenderer from "diagram-js/lib/draw/BaseRenderer";
import { getRoundRectPath } from "bpmn-js/lib/draw/BpmnRenderUtil";
import { is, isAny } from "bpmn-js/lib/util/ModelUtil";
import inherits from "inherits";
import {
  append as svgAppend,
  attr as svgAttr,
  create as svgCreate,
} from "tiny-svg";
import { ICON_DEFS } from "./icons/iconPaths";
import {
  getAccentColor,
  resolveIconKey,
  shouldOrchestRender,
} from "./elementStyle";
import { ORCHEST_BPMN_THEME } from "./theme";

const RENDER_PRIORITY = 2000;

type DiagramElement = {
  type?: string;
  width: number;
  height: number;
  x?: number;
  y?: number;
  labelTarget?: unknown;
};

type BpmnRendererLike = {
  drawShape: (parentGfx: SVGElement, element: DiagramElement, attrs?: Record<string, unknown>) => SVGElement;
  getShapePath: (shape: DiagramElement) => string;
  handlers?: Record<
    string,
    (parentGfx: SVGElement, element: DiagramElement, attrs?: Record<string, unknown>) => SVGElement
  >;
};

/** Tasks whose default bpmn-js glyphs we replace with Orchest badges. */
const TYPED_TASKS_WITHOUT_DEFAULT_ICON = [
  "bpmn:ServiceTask",
  "bpmn:UserTask",
  "bpmn:ScriptTask",
  "bpmn:BusinessRuleTask",
  "bpmn:SendTask",
  "bpmn:ReceiveTask",
  "bpmn:ManualTask",
];

/** Gateways drawn as plain diamond, then Orchest center marks. */
const TYPED_GATEWAYS = [
  "bpmn:ExclusiveGateway",
  "bpmn:ParallelGateway",
  "bpmn:InclusiveGateway",
  "bpmn:EventBasedGateway",
];

/**
 * Phase-1 Orchest renderer: restyles default BPMN shapes and overlays
 * futuristic type glyphs without breaking markers/overlays.
 */
export default function OrchestBpmnRenderer(
  eventBus: unknown,
  bpmnRenderer: BpmnRendererLike
) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  BaseRenderer.call(this as any, eventBus, RENDER_PRIORITY);
  this.bpmnRenderer = bpmnRenderer;
}

inherits(OrchestBpmnRenderer, BaseRenderer);

OrchestBpmnRenderer.$inject = ["eventBus", "bpmnRenderer"];

OrchestBpmnRenderer.prototype.canRender = function (element: DiagramElement) {
  return shouldOrchestRender(element);
};

OrchestBpmnRenderer.prototype.drawShape = function (
  parentGfx: SVGElement,
  element: DiagramElement
) {
  const accent = getAccentColor(element);
  const attrs: Record<string, unknown> = {
    fill: ORCHEST_BPMN_THEME.surface,
    stroke: ORCHEST_BPMN_THEME.stroke,
  };

  if (is(element, "bpmn:CallActivity")) {
    attrs.stroke = ORCHEST_BPMN_THEME.colors.callActivity;
    attrs.strokeWidth = ORCHEST_BPMN_THEME.callActivityStrokeWidth;
  } else if (is(element, "bpmn:Event")) {
    attrs.stroke = accent;
  } else if (is(element, "bpmn:Gateway")) {
    attrs.stroke = accent;
    attrs.fill = ORCHEST_BPMN_THEME.surfaceStrong;
  }

  const shape = drawBaseShape(this.bpmnRenderer, parentGfx, element, attrs);
  stylePrimaryShape(shape, element, accent);
  drawTypeBadge(parentGfx, element, accent);
  drawGatewayMark(parentGfx, element, accent);
  return shape;
};

function drawBaseShape(
  bpmnRenderer: BpmnRendererLike,
  parentGfx: SVGElement,
  element: DiagramElement,
  attrs: Record<string, unknown>
): SVGElement {
  const usePlainTask =
    isAny(element, TYPED_TASKS_WITHOUT_DEFAULT_ICON) &&
    typeof bpmnRenderer.handlers?.["bpmn:Task"] === "function";

  if (usePlainTask) {
    return bpmnRenderer.handlers!["bpmn:Task"](parentGfx, element, attrs);
  }

  const usePlainGateway =
    isAny(element, TYPED_GATEWAYS) &&
    typeof bpmnRenderer.handlers?.["bpmn:Gateway"] === "function";

  if (usePlainGateway) {
    return bpmnRenderer.handlers!["bpmn:Gateway"](parentGfx, element, attrs);
  }

  return bpmnRenderer.drawShape(parentGfx, element, attrs);
}

OrchestBpmnRenderer.prototype.getShapePath = function (shape: DiagramElement) {
  if (
    isAny(shape, [
      "bpmn:Task",
      "bpmn:CallActivity",
      "bpmn:SubProcess",
    ])
  ) {
    return getRoundRectPath(shape, ORCHEST_BPMN_THEME.taskRadius);
  }
  return this.bpmnRenderer.getShapePath(shape);
};

function stylePrimaryShape(
  shape: SVGElement,
  element: DiagramElement,
  accent: string
) {
  if (!shape) return;

  const base: Record<string, string | number> = {
    fill: ORCHEST_BPMN_THEME.surface,
    stroke: ORCHEST_BPMN_THEME.stroke,
    "stroke-width": ORCHEST_BPMN_THEME.strokeWidth,
  };

  if (is(element, "bpmn:Task") || is(element, "bpmn:CallActivity")) {
    base.rx = ORCHEST_BPMN_THEME.taskRadius;
    base.ry = ORCHEST_BPMN_THEME.taskRadius;
    base.stroke = is(element, "bpmn:CallActivity")
      ? ORCHEST_BPMN_THEME.colors.callActivity
      : ORCHEST_BPMN_THEME.stroke;
    if (is(element, "bpmn:CallActivity")) {
      base["stroke-width"] = ORCHEST_BPMN_THEME.callActivityStrokeWidth;
    }
  }

  if (is(element, "bpmn:Event")) {
    base.stroke = accent;
    base.fill = ORCHEST_BPMN_THEME.surfaceStrong;
    if (is(element, "bpmn:EndEvent")) {
      base["stroke-width"] = 3.25;
    }
  }

  if (is(element, "bpmn:Gateway")) {
    base.stroke = accent;
    base.fill = ORCHEST_BPMN_THEME.surfaceStrong;
    base["stroke-width"] = ORCHEST_BPMN_THEME.gatewayStrokeWidth;
    shape.classList?.add("orchest-bpmn-gateway");
  }

  svgAttr(shape, base);

  // Soft glass edge via a duplicate inset outline for tasks.
  if (is(element, "bpmn:Task") || is(element, "bpmn:CallActivity")) {
    shape.classList?.add("orchest-bpmn-shape");
  }
}

function drawTypeBadge(
  parentGfx: SVGElement,
  element: DiagramElement,
  accent: string
) {
  // Activity corner badges only — gateways use drawGatewayMark.
  if (!is(element, "bpmn:Activity") && !is(element, "bpmn:Task")) {
    return;
  }
  if (is(element, "bpmn:Gateway")) return;

  const iconKey = resolveIconKey(element);
  if (!iconKey) return;

  const iconDef = ICON_DEFS[iconKey];
  if (!iconDef) return;

  const size = ORCHEST_BPMN_THEME.iconBadgeSize;
  const offset = ORCHEST_BPMN_THEME.iconBadgeOffset;
  const x = offset;
  const y = offset;

  const badge = svgCreate("rect");
  svgAttr(badge, {
    x,
    y,
    width: size,
    height: size,
    rx: ORCHEST_BPMN_THEME.iconBadgeRadius,
    ry: ORCHEST_BPMN_THEME.iconBadgeRadius,
    fill: accent,
    stroke: "none",
    class: "orchest-bpmn-icon-badge",
  });
  svgAppend(parentGfx, badge);

  appendIconGroup(parentGfx, iconDef, {
    x,
    y,
    scale: size / 16,
    stroke: "#FFFFFF",
    strokeWidth: ORCHEST_BPMN_THEME.iconStrokeWidth,
  });
}

function drawGatewayMark(
  parentGfx: SVGElement,
  element: DiagramElement,
  accent: string
) {
  if (!isAny(element, TYPED_GATEWAYS)) return;

  const iconKey = resolveIconKey(element);
  if (!iconKey) return;

  const iconDef = ICON_DEFS[iconKey];
  if (!iconDef) return;

  const markSize = Math.min(element.width, element.height) * ORCHEST_BPMN_THEME.gatewayMarkScale;
  const scale = markSize / 16;
  const x = (element.width - markSize) / 2;
  const y = (element.height - markSize) / 2;

  appendIconGroup(parentGfx, iconDef, {
    x,
    y,
    scale,
    stroke: accent,
    strokeWidth: 2,
  });
}

function appendIconGroup(
  parentGfx: SVGElement,
  iconDef: (typeof ICON_DEFS)[keyof typeof ICON_DEFS],
  opts: { x: number; y: number; scale: number; stroke: string; strokeWidth: number }
) {
  const group = svgCreate("g");
  svgAttr(group, {
    transform: `translate(${opts.x}, ${opts.y}) scale(${opts.scale})`,
    class: "orchest-bpmn-type-icon",
  });

  for (const d of iconDef.strokes) {
    const path = svgCreate("path");
    svgAttr(path, {
      d,
      fill: "none",
      stroke: opts.stroke,
      "stroke-width": opts.strokeWidth,
      "stroke-linecap": "round",
      "stroke-linejoin": "round",
    });
    svgAppend(group, path);
  }

  for (const d of iconDef.fills ?? []) {
    const path = svgCreate("path");
    svgAttr(path, {
      d,
      fill: opts.stroke,
      stroke: opts.stroke,
      "stroke-width": opts.strokeWidth,
      "stroke-linecap": "round",
    });
    svgAppend(group, path);
  }

  svgAppend(parentGfx, group);
}
