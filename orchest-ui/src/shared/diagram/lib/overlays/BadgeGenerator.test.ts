import { describe, expect, it } from "vitest";
import {
  generateModificationBadge,
  generateStatusBadge,
  getStatusLabel,
  getStatusRingMarkerClass,
} from "./BadgeGenerator";

describe("getStatusRingMarkerClass", () => {
  it("maps running-family states to status-ring-running", () => {
    expect(getStatusRingMarkerClass("RUNNING")).toBe("status-ring-running");
    expect(getStatusRingMarkerClass("active")).toBe("status-ring-running");
    expect(getStatusRingMarkerClass("STARTED")).toBe("status-ring-running");
    expect(getStatusRingMarkerClass("TRIGGERED")).toBe("status-ring-running");
    expect(getStatusRingMarkerClass("PENDING")).toBe("status-ring-running");
  });

  it("maps hold, completed, and incident states", () => {
    expect(getStatusRingMarkerClass("HOLD")).toBe("status-ring-hold");
    expect(getStatusRingMarkerClass("COMPLETED")).toBe("status-ring-completed");
    expect(getStatusRingMarkerClass("INCIDENT")).toBe("status-ring-incident");
    expect(getStatusRingMarkerClass("FAILED")).toBe("status-ring-incident");
  });

  it("returns null for unknown states", () => {
    expect(getStatusRingMarkerClass("UNKNOWN")).toBeNull();
    expect(getStatusRingMarkerClass("")).toBeNull();
  });
});

describe("getStatusLabel", () => {
  it("returns human labels for known states", () => {
    expect(getStatusLabel("RUNNING")).toBe("Running");
    expect(getStatusLabel("HOLD")).toBe("On hold");
    expect(getStatusLabel("COMPLETED")).toBe("Completed");
    expect(getStatusLabel("INCIDENT")).toBe("Incident");
    expect(getStatusLabel("FAILED")).toBe("Failed");
  });

  it("returns null for unknown states", () => {
    expect(getStatusLabel("NOPE")).toBeNull();
  });
});

describe("generateStatusBadge", () => {
  it("returns an icon-only chip with title and data-status, without a count", () => {
    const html = generateStatusBadge(undefined, "RUNNING");
    expect(html).toContain('title="Running"');
    expect(html).toContain('data-status="RUNNING"');
    expect(html).toContain("activity-status-icon");
    expect(html).toContain("activity-status-blink-dot");
    expect(html).toContain("#16A34A");
    expect(html).not.toContain(">1<");
    expect(html).toContain("data-testid=\"statistics-overlay\"");
  });

  it("uses pause for hold, tick for completed, and ban for cancelled", () => {
    const hold = generateStatusBadge(undefined, "HOLD");
    const completed = generateStatusBadge(undefined, "COMPLETED");
    const cancelled = generateStatusBadge(undefined, "CANCELLED");
    const pending = generateStatusBadge(undefined, "PENDING");

    expect(hold).toContain("#EAB308");
    expect(hold).toContain("activity-status-blink-icon");
    expect(completed).toContain("#6B7280");
    expect(cancelled).toContain("#374151");
    expect(pending).toContain("#16A34A");
    expect(pending).toContain("activity-status-blink-dot");
    expect(hold).toMatch(/lucide-pause|class="lucide lucide-pause"/);
    expect(completed).toMatch(/lucide-check|class="lucide lucide-check"/);
    expect(cancelled).toMatch(/lucide-ban|class="lucide lucide-ban"/);
  });

  it("returns null for unknown states", () => {
    expect(generateStatusBadge(undefined, "WEIRD")).toBeNull();
  });
});

describe("generateModificationBadge", () => {
  it("keeps source and target badges unchanged", () => {
    expect(generateModificationBadge("source")).toContain("-1");
    expect(generateModificationBadge("target")).toContain("+1");
  });
});
