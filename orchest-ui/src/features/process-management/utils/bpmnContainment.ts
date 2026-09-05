/**
 * Parses a BPMN 2.0 XML string into the containment structure needed to build
 * a hierarchical execution history tree.
 *
 * The execution log (`sequenceExecutions`) is a flat map with no parent/scope
 * information, so the nesting of nodes inside embedded subprocesses has to be
 * recovered from the BPMN definition itself.
 */

export interface BpmnContainment {
  /** Id of the (executable) root process element, if found. */
  processId: string | null;
  /** Human-readable name of the root process element. */
  processName: string;
  /** Maps a node id to the id of its nearest containing element (subprocess or process). */
  parentOf: Map<string, string>;
  /** Ids of subprocess-like container elements (subProcess, transaction, adHocSubProcess). */
  containerIds: Set<string>;
}

/** Local element names that visually contain other flow nodes. */
const CONTAINER_TAGS = new Set([
  "subProcess",
  "transaction",
  "adHocSubProcess",
]);

const emptyContainment = (): BpmnContainment => ({
  processId: null,
  processName: "",
  parentOf: new Map<string, string>(),
  containerIds: new Set<string>(),
});

export function parseBpmnContainment(xml: string | undefined | null): BpmnContainment {
  const result = emptyContainment();
  if (!xml || typeof DOMParser === "undefined") return result;

  let doc: Document;
  try {
    doc = new DOMParser().parseFromString(xml, "application/xml");
  } catch {
    return result;
  }
  if (doc.getElementsByTagName("parsererror").length > 0) return result;

  const processes = Array.from(doc.getElementsByTagName("*")).filter(
    (el) => el.localName === "process"
  );
  const rootProcess =
    processes.find((p) => p.getAttribute("isExecutable") === "true") ??
    processes[0];

  if (!rootProcess) return result;

  result.processId = rootProcess.getAttribute("id");
  result.processName = rootProcess.getAttribute("name") || "";

  const walk = (element: Element, containerId: string | null) => {
    for (const child of Array.from(element.children)) {
      const id = child.getAttribute("id");
      const isContainer = CONTAINER_TAGS.has(child.localName);

      if (id && containerId) {
        result.parentOf.set(id, containerId);
      }

      if (isContainer) {
        if (id) result.containerIds.add(id);
        walk(child, id ?? containerId);
      } else {
        walk(child, containerId);
      }
    }
  };

  walk(rootProcess, result.processId);
  return result;
}
