export type ParseCompensateVariablesResult =
  | { ok: true; variables?: Record<string, unknown> }
  | { ok: false; error: string };

/** Compensation flows are stored as process definition version -1. */
export const COMPENSATION_FLOW_VERSION = -1;

/**
 * Returns definition IDs that have a compensation flow (version -1) deployed.
 */
export function getCompensationProcessDefinitionIds(
  definitions: readonly { definitionId: string; version: number }[] | undefined
): Set<string> {
  const ids = new Set<string>();
  if (!definitions) return ids;
  for (const definition of definitions) {
    if (definition.version === COMPENSATION_FLOW_VERSION) {
      ids.add(definition.definitionId);
    }
  }
  return ids;
}

/**
 * Parses optional compensate variables JSON.
 * Empty / `{}` omits variables from the request.
 */
export function parseCompensateVariables(
  raw: string
): ParseCompensateVariablesResult {
  const trimmed = raw.trim();
  if (!trimmed || trimmed === "{}") {
    return { ok: true };
  }

  try {
    const parsed: unknown = JSON.parse(trimmed);
    if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
      return { ok: false, error: "Variables must be a JSON object" };
    }

    const variables = parsed as Record<string, unknown>;
    if (Object.keys(variables).length === 0) {
      return { ok: true };
    }

    return { ok: true, variables };
  } catch {
    return { ok: false, error: "Invalid JSON for variables" };
  }
}
