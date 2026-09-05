/**
 * Extracts definition-time Zeebe/BPMN details from a bpmn-js businessObject.
 */

export type ElementDetailField = { label: string; value: string };
export type ElementDetailSection = { title: string; fields: ElementDetailField[] };

/** Minimal shape of a bpmn-js / moddle business object we read from. */
export type BpmnBusinessObject = {
  $type?: string;
  retryCounter?: string | number;
  modelerTemplate?: string;
  modelerTemplateVersion?: string | number;
  modelerTemplateIcon?: string;
  extensionElements?: { values?: unknown[] };
  loopCharacteristics?: BpmnBusinessObject & {
    isSequential?: boolean;
    collection?: string;
    elementVariable?: string;
    completionCondition?: { body?: string };
    loopCardinality?: { body?: string } | string;
    extensionElements?: { values?: unknown[] };
  };
  eventDefinitions?: BpmnBusinessObject[];
  messageRef?: { name?: string; id?: string };
  errorRef?: { name?: string; errorCode?: string; id?: string };
  escalationRef?: { name?: string; escalationCode?: string; id?: string };
  signalRef?: { name?: string; id?: string };
  [key: string]: unknown;
};

const SKIP_KEYS = new Set([
  "$type",
  "$instanceOf",
  "$descriptor",
  "$model",
  "$parent",
  "$refs",
  "di",
  "extensionElements",
  "incoming",
  "outgoing",
  "flowElements",
  "laneSets",
  "artifacts",
  "eventDefinitions",
  "loopCharacteristics",
  "documentation",
  "auditing",
  "monitoring",
  "properties",
  "categoryValueRef",
  "dataInputAssociations",
  "dataOutputAssociations",
  "property",
  "resourceRole",
  "ioSpecification",
  "boundaryEventRefs",
  "default",
  "sourceRef",
  "targetRef",
  "conditionExpression",
]);

const KNOWN_EXTENSION_TYPES = new Set([
  "zeebe:TaskDefinition",
  "zeebe:CalledElement",
  "zeebe:CalledDecision",
  "zeebe:IoMapping",
  "zeebe:TaskHeaders",
  "zeebe:FormDefinition",
  "zeebe:AssignmentDefinition",
  "zeebe:PriorityDefinition",
  "zeebe:TaskSchedule",
  "zeebe:Script",
  "zeebe:Subscription",
  "zeebe:Properties",
  "zeebe:LinkedResources",
  "zeebe:ExecutionListeners",
  "zeebe:TaskListeners",
  "zeebe:AdHoc",
  "zeebe:UserTask",
  "zeebe:LoopCharacteristics",
]);

const humanizeKey = (key: string): string =>
  key
    .replace(/^zeebe:/, "")
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .replace(/^./, (c) => c.toUpperCase());

const formatValue = (value: unknown): string | null => {
  if (value === null || value === undefined) return null;
  if (typeof value === "string") {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }
  if (typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }
  return null;
};

const getType = (obj: unknown): string | undefined => {
  if (!obj || typeof obj !== "object") return undefined;
  const t = (obj as { $type?: string }).$type;
  return typeof t === "string" ? t : undefined;
};

const asRecord = (obj: unknown): Record<string, unknown> | null => {
  if (!obj || typeof obj !== "object") return null;
  return obj as Record<string, unknown>;
};

/** Normalizes moddle collections / arrays; anything else → []. */
const toArray = (value: unknown): unknown[] => {
  if (Array.isArray(value)) return value;
  if (value && typeof value === "object" && typeof (value as { length?: unknown }).length === "number") {
    try {
      return Array.from(value as ArrayLike<unknown>);
    } catch {
      return [];
    }
  }
  return [];
};

const hasMeaningfulFields = (fields: ElementDetailField[]): boolean =>
  fields.some((f) => f.label.trim().length > 0 || f.value.trim().length > 0);


const collectAttrs = (
  obj: unknown,
  extraSkip: string[] = []
): ElementDetailField[] => {
  const record = asRecord(obj);
  if (!record) return [];

  const skip = new Set([...SKIP_KEYS, ...extraSkip]);
  const fields: ElementDetailField[] = [];

  for (const [key, raw] of Object.entries(record)) {
    if (key.startsWith("$") || skip.has(key)) continue;
    if (raw !== null && typeof raw === "object") continue;

    const value = formatValue(raw);
    if (value === null) continue;
    fields.push({ label: humanizeKey(key), value });
  }

  return fields;
};

const pushSection = (
  sections: ElementDetailSection[],
  title: string,
  fields: ElementDetailField[]
) => {
  if (!hasMeaningfulFields(fields)) return;
  sections.push({ title, fields: fields.filter((f) => f.label.trim() || f.value.trim()) });
};

const extractTaskDefinition = (ext: unknown, sections: ElementDetailSection[]) => {
  pushSection(sections, "Task definition", collectAttrs(ext));
};

const extractCalledElement = (ext: unknown, sections: ElementDetailSection[]) => {
  pushSection(sections, "Called element", collectAttrs(ext));
};

const extractCalledDecision = (ext: unknown, sections: ElementDetailSection[]) => {
  pushSection(sections, "Called decision", collectAttrs(ext));
};

const mapIoParameters = (items: unknown[]): ElementDetailField[] => {
  const fields: ElementDetailField[] = [];
  for (const item of items) {
    const row = asRecord(item);
    if (!row) continue;
    const target = formatValue(row.target);
    const source = formatValue(row.source);
    if (!target && !source) continue;
    fields.push({
      label: target ?? "(target)",
      value: source ?? "",
    });
  }
  return fields;
};

const extractIoMapping = (ext: unknown, sections: ElementDetailSection[]) => {
  const record = asRecord(ext);
  if (!record) return;

  const inputs = toArray(record.inputParameters);
  const outputs = toArray(record.outputParameters);

  // Empty lists → no Input/Output mapping sections or tables
  if (inputs.length > 0) {
    pushSection(sections, "Input mappings", mapIoParameters(inputs));
  }
  if (outputs.length > 0) {
    pushSection(sections, "Output mappings", mapIoParameters(outputs));
  }
};

const extractTaskHeaders = (ext: unknown, sections: ElementDetailSection[]) => {
  const record = asRecord(ext);
  if (!record) return;
  const values = toArray(record.values);
  if (values.length === 0) return;

  const fields: ElementDetailField[] = [];
  for (const item of values) {
    const row = asRecord(item);
    if (!row) continue;
    const key = formatValue(row.key);
    const value = formatValue(row.value);
    if (!key && !value) continue;
    fields.push({ label: key ?? "(key)", value: value ?? "" });
  }
  pushSection(sections, "Headers", fields);
};

const extractProperties = (ext: unknown, sections: ElementDetailSection[]) => {
  const record = asRecord(ext);
  if (!record) return;
  const props = toArray(record.properties);
  if (props.length === 0) return;

  const fields: ElementDetailField[] = [];
  for (const item of props) {
    const row = asRecord(item);
    if (!row) continue;
    const name = formatValue(row.name);
    const value = formatValue(row.value);
    if (!name && !value) continue;
    fields.push({ label: name ?? "(name)", value: value ?? "" });
  }
  pushSection(sections, "Properties", fields);
};

const extractLinkedResources = (ext: unknown, sections: ElementDetailSection[]) => {
  const record = asRecord(ext);
  if (!record) return;
  const values = toArray(record.values);
  if (values.length === 0) return;

  const fields: ElementDetailField[] = [];
  values.forEach((item, index) => {
    const attrs = collectAttrs(item);
    if (attrs.length === 0) return;
    attrs.forEach((f) => {
      fields.push({
        label: values.length > 1 ? `${f.label} (${index + 1})` : f.label,
        value: f.value,
      });
    });
  });
  pushSection(sections, "Linked resources", fields);
};

const extractListeners = (
  ext: unknown,
  title: string,
  sections: ElementDetailSection[]
) => {
  const record = asRecord(ext);
  if (!record) return;
  const listeners = toArray(record.listeners);
  if (listeners.length === 0) return;

  const fields: ElementDetailField[] = [];
  listeners.forEach((item, index) => {
    const attrs = collectAttrs(item, ["headers"]);
    attrs.forEach((f) => {
      fields.push({
        label: listeners.length > 1 ? `${f.label} (${index + 1})` : f.label,
        value: f.value,
      });
    });
    const headers = asRecord(item)?.headers;
    if (headers) {
      const headerRecord = asRecord(headers);
      const values = toArray(headerRecord?.values);
      values.forEach((h) => {
        const row = asRecord(h);
        if (!row) return;
        const key = formatValue(row.key);
        const value = formatValue(row.value);
        if (!key && !value) return;
        fields.push({
          label: listeners.length > 1 ? `Header ${key ?? ""} (${index + 1})`.trim() : `Header ${key ?? ""}`.trim(),
          value: value ?? "",
        });
      });
    }
  });
  pushSection(sections, title, fields);
};

const extractGenericExtension = (ext: unknown, sections: ElementDetailSection[]) => {
  const type = getType(ext);
  const title = type ? humanizeKey(type) : "Extension";
  pushSection(sections, title, collectAttrs(ext, ["values", "listeners", "properties", "inputParameters", "outputParameters"]));
};

const extractExtensionElements = (
  values: unknown[] | undefined,
  sections: ElementDetailSection[]
) => {
  if (!values?.length) return;

  for (const ext of values) {
    const type = getType(ext);
    switch (type) {
      case "zeebe:TaskDefinition":
        extractTaskDefinition(ext, sections);
        break;
      case "zeebe:CalledElement":
        extractCalledElement(ext, sections);
        break;
      case "zeebe:CalledDecision":
        extractCalledDecision(ext, sections);
        break;
      case "zeebe:IoMapping":
        extractIoMapping(ext, sections);
        break;
      case "zeebe:TaskHeaders":
        extractTaskHeaders(ext, sections);
        break;
      case "zeebe:FormDefinition":
        pushSection(sections, "Form", collectAttrs(ext));
        break;
      case "zeebe:AssignmentDefinition":
        pushSection(sections, "Assignment", collectAttrs(ext));
        break;
      case "zeebe:PriorityDefinition":
        pushSection(sections, "Priority", collectAttrs(ext));
        break;
      case "zeebe:TaskSchedule":
        pushSection(sections, "Task schedule", collectAttrs(ext));
        break;
      case "zeebe:Script":
        pushSection(sections, "Script", collectAttrs(ext));
        break;
      case "zeebe:Subscription":
        // Shown on Event as correlationId; skip duplicate Subscription section
        break;
      case "zeebe:Properties":
        extractProperties(ext, sections);
        break;
      case "zeebe:LinkedResources":
        extractLinkedResources(ext, sections);
        break;
      case "zeebe:ExecutionListeners":
        extractListeners(ext, "Execution listeners", sections);
        break;
      case "zeebe:TaskListeners":
        extractListeners(ext, "Task listeners", sections);
        break;
      case "zeebe:AdHoc":
        pushSection(sections, "Ad-hoc", collectAttrs(ext));
        break;
      case "zeebe:UserTask":
        // Marker extension — no attrs; skip unless it gains fields later
        pushSection(sections, "User task", collectAttrs(ext));
        break;
      case "zeebe:LoopCharacteristics":
        // Handled with multi-instance on the element
        break;
      default:
        if (!type || !KNOWN_EXTENSION_TYPES.has(type)) {
          extractGenericExtension(ext, sections);
        }
        break;
    }
  }
};

const extractMultiInstance = (
  bo: BpmnBusinessObject,
  sections: ElementDetailSection[]
) => {
  const loop = bo.loopCharacteristics;
  if (!loop) return;

  const fields: ElementDetailField[] = [];

  if (typeof loop.isSequential === "boolean") {
    fields.push({ label: "Sequential", value: String(loop.isSequential) });
  }

  const completion = formatValue(loop.completionCondition?.body);
  if (completion) {
    fields.push({ label: "Completion condition", value: completion });
  }

  // Standard BPMN multi-instance attrs sometimes present
  fields.push(...collectAttrs(loop, ["completionCondition", "loopCardinality"]));

  const loopCardinality = formatValue(
    asRecord(loop.loopCardinality as unknown)?.body ?? loop.loopCardinality
  );
  if (loopCardinality) {
    fields.push({ label: "Loop cardinality", value: loopCardinality });
  }

  const zeebeLoop = loop.extensionElements?.values?.find(
    (v) => getType(v) === "zeebe:LoopCharacteristics"
  );
  if (zeebeLoop) {
    fields.push(...collectAttrs(zeebeLoop));
  }

  pushSection(sections, "Multi-instance", fields);
};

const extractEventDetails = (
  bo: BpmnBusinessObject,
  sections: ElementDetailSection[]
) => {
  const fields: ElementDetailField[] = [];

  const messageName =
    formatValue(bo.messageRef?.name) ?? formatValue(bo.messageRef?.id);
  if (messageName) fields.push({ label: "Message", value: messageName });

  const errorName = formatValue(bo.errorRef?.name) ?? formatValue(bo.errorRef?.id);
  const errorCode = formatValue(bo.errorRef?.errorCode);
  if (errorName) fields.push({ label: "Error", value: errorName });
  if (errorCode) fields.push({ label: "Error code", value: errorCode });

  const escalationName =
    formatValue(bo.escalationRef?.name) ?? formatValue(bo.escalationRef?.id);
  const escalationCode = formatValue(bo.escalationRef?.escalationCode);
  if (escalationName) fields.push({ label: "Escalation", value: escalationName });
  if (escalationCode) fields.push({ label: "Escalation code", value: escalationCode });

  const signalName =
    formatValue(bo.signalRef?.name) ?? formatValue(bo.signalRef?.id);
  if (signalName) fields.push({ label: "Signal", value: signalName });

  const defs = toArray(bo.eventDefinitions);
  let hasMessageEvent = Boolean(messageName);

  for (const def of defs) {
    const type = getType(def);
    if (type === "bpmn:TimerEventDefinition") {
      const timeDuration = formatValue(
        asRecord(def.timeDuration)?.body ?? def.timeDuration
      );
      const timeDate = formatValue(asRecord(def.timeDate)?.body ?? def.timeDate);
      const timeCycle = formatValue(asRecord(def.timeCycle)?.body ?? def.timeCycle);
      if (timeDuration) fields.push({ label: "Timer duration", value: timeDuration });
      if (timeDate) fields.push({ label: "Timer date", value: timeDate });
      if (timeCycle) fields.push({ label: "Timer cycle", value: timeCycle });
    } else if (type === "bpmn:MessageEventDefinition") {
      hasMessageEvent = true;
      const ref = asRecord(def.messageRef);
      const name = formatValue(ref?.name) ?? formatValue(ref?.id);
      if (name) fields.push({ label: "Message", value: name });
    } else if (type === "bpmn:ErrorEventDefinition") {
      const ref = asRecord(def.errorRef);
      const name = formatValue(ref?.name) ?? formatValue(ref?.id);
      const code = formatValue(ref?.errorCode);
      if (name) fields.push({ label: "Error", value: name });
      if (code) fields.push({ label: "Error code", value: code });
    } else if (type === "bpmn:SignalEventDefinition") {
      const ref = asRecord(def.signalRef);
      const name = formatValue(ref?.name) ?? formatValue(ref?.id);
      if (name) fields.push({ label: "Signal", value: name });
    } else if (type === "bpmn:EscalationEventDefinition") {
      const ref = asRecord(def.escalationRef);
      const name = formatValue(ref?.name) ?? formatValue(ref?.id);
      const code = formatValue(ref?.escalationCode);
      if (name) fields.push({ label: "Escalation", value: name });
      if (code) fields.push({ label: "Escalation code", value: code });
    } else if (type === "bpmn:ConditionalEventDefinition") {
      const body = formatValue(asRecord(def.condition)?.body ?? def.condition);
      if (body) fields.push({ label: "Condition", value: body });
      fields.push(...collectAttrs(def, ["condition"]));
    } else if (type === "bpmn:LinkEventDefinition") {
      const name = formatValue(def.name);
      if (name) fields.push({ label: "Link name", value: name });
    } else if (type) {
      fields.push(...collectAttrs(def));
    }
  }

  // Message catch/start/boundary: correlation lives on zeebe:subscription
  if (hasMessageEvent || bo.$type === "bpmn:ReceiveTask" || bo.$type === "bpmn:IntermediateCatchEvent") {
    const correlationId = findSubscriptionCorrelationKey(bo);
    if (correlationId) {
      fields.push({ label: "correlationId", value: correlationId });
    }
  }

  // Deduplicate by label+value
  const seen = new Set<string>();
  const unique = fields.filter((f) => {
    const key = `${f.label}:${f.value}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });

  pushSection(sections, "Event", unique);
};

const findSubscriptionCorrelationKey = (bo: BpmnBusinessObject): string | null => {
  const readCorrelation = (ext: unknown): string | null => {
    const record = asRecord(ext);
    if (!record) return null;
    const attrs = asRecord(record.$attrs);
    return (
      formatValue(record.correlationKey) ??
      formatValue(record.correlationId) ??
      formatValue(attrs?.correlationKey) ??
      formatValue(attrs?.correlationId)
    );
  };

  const readFromValues = (values: unknown[] | undefined): string | null => {
    for (const ext of toArray(values)) {
      const type = getType(ext) ?? "";
      const isSubscription =
        type.endsWith(":Subscription") ||
        type.toLowerCase().includes("subscription");
      if (!isSubscription) continue;
      const key = readCorrelation(ext);
      if (key) return key;
    }
    return null;
  };

  // Most common: <intermediateCatchEvent><extensionElements><zeebe:subscription/>
  const fromEvent = readFromValues(bo.extensionElements?.values);
  if (fromEvent) return fromEvent;

  // Some exporters nest subscription under messageEventDefinition / message
  for (const def of toArray(bo.eventDefinitions)) {
    const defRecord = asRecord(def);
    const fromDef = readFromValues(
      asRecord(defRecord?.extensionElements)?.values as unknown[] | undefined
    );
    if (fromDef) return fromDef;

    const messageRef = asRecord(defRecord?.messageRef);
    const fromMessage = readFromValues(
      asRecord(messageRef?.extensionElements)?.values as unknown[] | undefined
    );
    if (fromMessage) return fromMessage;
  }

  // Last resort: any extension value that carries correlationKey / correlationId
  const scanAll = (values: unknown[] | undefined): string | null => {
    for (const ext of toArray(values)) {
      const key = readCorrelation(ext);
      if (key) return key;
      const nested = asRecord(asRecord(ext)?.extensionElements)?.values;
      const fromNested = scanAll(nested as unknown[] | undefined);
      if (fromNested) return fromNested;
    }
    return null;
  };

  const fromAnyEventExt = scanAll(bo.extensionElements?.values);
  if (fromAnyEventExt) return fromAnyEventExt;

  for (const def of toArray(bo.eventDefinitions)) {
    const defRecord = asRecord(def);
    const fromAnyDefExt = scanAll(
      asRecord(defRecord?.extensionElements)?.values as unknown[] | undefined
    );
    if (fromAnyDefExt) return fromAnyDefExt;
  }

  return null;
};

const extractSequenceFlowDetails = (
  bo: BpmnBusinessObject,
  sections: ElementDetailSection[]
) => {
  if (bo.$type !== "bpmn:SequenceFlow" && bo.$type !== "bpmn:MessageFlow") {
    return;
  }

  const fields: ElementDetailField[] = [];

  const source = asRecord(bo.sourceRef);
  const target = asRecord(bo.targetRef);
  const sourceLabel =
    formatValue(source?.name) ?? formatValue(source?.id);
  const targetLabel =
    formatValue(target?.name) ?? formatValue(target?.id);

  if (sourceLabel) fields.push({ label: "From", value: sourceLabel });
  if (targetLabel) fields.push({ label: "To", value: targetLabel });

  const sourceDefault = source?.default;
  const isDefault =
    sourceDefault === bo ||
    formatValue(asRecord(sourceDefault)?.id) === formatValue(bo.id);
  if (isDefault) {
    fields.push({ label: "Default", value: "true" });
  }

  pushSection(sections, "Flow", fields);

  const condition = asRecord(bo.conditionExpression);
  const conditionBody =
    formatValue(condition?.body) ??
    formatValue(bo.conditionExpression);
  if (conditionBody) {
    pushSection(sections, "Condition", [
      { label: "Expression", value: conditionBody },
    ]);
  }
};

const extractElementLevelExtras = (
  bo: BpmnBusinessObject,
  sections: ElementDetailSection[]
) => {
  const fields: ElementDetailField[] = [];
  const retry = formatValue(bo.retryCounter);
  if (retry) fields.push({ label: "Retry counter", value: retry });
  const template = formatValue(bo.modelerTemplate);
  if (template) fields.push({ label: "Template", value: template });
  const templateVersion = formatValue(bo.modelerTemplateVersion);
  if (templateVersion) fields.push({ label: "Template version", value: templateVersion });
  pushSection(sections, "Element attributes", fields);
};

/**
 * Walks a bpmn-js businessObject and returns structured detail sections.
 * Returns [] when there are no definition extras beyond BPMN $type alone.
 * Never throws — unexpected shapes yield [].
 */
export const extractBpmnElementDetails = (
  businessObject: BpmnBusinessObject | null | undefined
): ElementDetailSection[] => {
  try {
    if (!businessObject || typeof businessObject !== "object") {
      return [];
    }

    const extras: ElementDetailSection[] = [];

    extractElementLevelExtras(businessObject, extras);
    extractSequenceFlowDetails(businessObject, extras);
    extractExtensionElements(businessObject.extensionElements?.values, extras);
    extractMultiInstance(businessObject, extras);
    extractEventDetails(businessObject, extras);

    if (extras.length === 0) {
      return [];
    }

    const sections: ElementDetailSection[] = [];
    const bpmnType = formatValue(businessObject.$type);
    if (bpmnType) {
      sections.push({
        title: "Element",
        fields: [{ label: "BPMN type", value: bpmnType }],
      });
    }
    sections.push(...extras);
    return sections.filter((s) => hasMeaningfulFields(s.fields));
  } catch {
    return [];
  }
};
