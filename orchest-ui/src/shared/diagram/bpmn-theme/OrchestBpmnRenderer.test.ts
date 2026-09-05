import { describe, expect, it } from "vitest";
import {
  ICON_DEFS,
  ICON_PATHS,
  PHASE1_ELEMENT_TYPES,
  TYPE_ICON_MAP,
} from "./icons/iconPaths";
import {
  getAccentColor,
  hasModelerTemplateIcon,
  resolveIconKey,
  shouldOrchestRender,
} from "./elementStyle";

const makeElement = (
  type: string,
  extras: Record<string, unknown> = {}
) => ({
  type,
  width: 100,
  height: 80,
  businessObject: {
    $type: type,
    $instanceOf: (t: string) => type === t || type.startsWith(t.replace("bpmn:", "bpmn:")),
    get: (key: string) => (extras[key] as unknown) ?? undefined,
    eventDefinitions: extras.eventDefinitions,
    ...extras,
  },
  ...extras,
});

describe("PHASE1 icon completeness", () => {
  it("has stroke icon defs for every mapped icon key", () => {
    for (const key of Object.values(TYPE_ICON_MAP)) {
      expect(ICON_DEFS[key]?.strokes?.length).toBeGreaterThan(0);
      expect(ICON_PATHS[key]).toBeTruthy();
    }
  });

  it("lists all Phase 1 element types", () => {
    expect(PHASE1_ELEMENT_TYPES).toContain("bpmn:ServiceTask");
    expect(PHASE1_ELEMENT_TYPES).toContain("bpmn:BusinessRuleTask");
    expect(PHASE1_ELEMENT_TYPES).toContain("bpmn:CallActivity");
    expect(PHASE1_ELEMENT_TYPES).toContain("bpmn:ExclusiveGateway");
    expect(PHASE1_ELEMENT_TYPES).toContain("bpmn:StartEvent");
  });
});

describe("shouldOrchestRender", () => {
  it("renders Phase 1 service tasks", () => {
    expect(shouldOrchestRender(makeElement("bpmn:ServiceTask"))).toBe(true);
  });

  it("skips labels", () => {
    expect(
      shouldOrchestRender(makeElement("bpmn:ServiceTask", { labelTarget: {} }))
    ).toBe(false);
  });

  it("skips templated tasks so template icon renderer can own them", () => {
    const el = makeElement("bpmn:ServiceTask", {
      "zeebe:modelerTemplateIcon": "data:image/svg+xml;base64,abc",
    });
    // businessObject.get must return the icon
    el.businessObject.get = (key: string) =>
      key === "zeebe:modelerTemplateIcon" ? "data:image/svg+xml;base64,abc" : undefined;
    expect(hasModelerTemplateIcon(el)).toBe(true);
    expect(shouldOrchestRender(el)).toBe(false);
  });

  it("skips unknown types", () => {
    expect(shouldOrchestRender(makeElement("bpmn:DataObjectReference"))).toBe(false);
  });
});

describe("resolveIconKey / accents", () => {
  it("maps service and user tasks", () => {
    expect(resolveIconKey(makeElement("bpmn:ServiceTask"))).toBe("service");
    expect(resolveIconKey(makeElement("bpmn:UserTask"))).toBe("user");
    expect(getAccentColor(makeElement("bpmn:ServiceTask"))).toBe("#0284C7");
    expect(getAccentColor(makeElement("bpmn:UserTask"))).toBe("#2563EB");
  });
});
