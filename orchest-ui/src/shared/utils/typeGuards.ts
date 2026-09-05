export function isObject(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

export function isNonEmptyString(value: unknown): value is string {
  return typeof value === 'string' && value.length > 0;
}

export function isArray<T>(value: unknown): value is T[] {
  return Array.isArray(value);
}

export function isAxiosLikeError(
  error: unknown
): error is { response?: { status?: number } } {
  return isObject(error) && 'response' in error;
}

export function isErrorWithId(error: unknown): error is { id: string } {
  return isObject(error) && 'id' in error && typeof error.id === 'string';
}

export interface SequenceExecutionShape {
  nodeId?: string;
  nodeName?: string;
  nodeType?: string;
  sourceNodeId?: string;
  sequenceFlowIds?: string[];
  state?: string;
  stateChanges?: Array<{ timestamp: string; state: string }>;
  metaData?: Record<string, unknown>;
}

export function isSequenceExecution(value: unknown): value is SequenceExecutionShape {
  if (!isObject(value)) return false;

  const hasValidNodeId = !('nodeId' in value) || typeof value.nodeId === 'string';
  const hasValidNodeName = !('nodeName' in value) || typeof value.nodeName === 'string';
  const hasValidNodeType = !('nodeType' in value) || typeof value.nodeType === 'string';
  const hasValidState = !('state' in value) || typeof value.state === 'string';
  const hasValidStateChanges = !('stateChanges' in value) || isArray(value.stateChanges);

  return hasValidNodeId && hasValidNodeName && hasValidNodeType && hasValidState && hasValidStateChanges;
}

export function isSequenceExecutionRecord(
  value: unknown
): value is Record<string, SequenceExecutionShape> {
  if (!isObject(value)) return false;
  
  return Object.values(value).every(
    (v) => v === null || v === undefined || isSequenceExecution(v)
  );
}

export interface StateChangeShape {
  timestamp: string;
  state: string;
}

export function isStateChangeArray(value: unknown): value is StateChangeShape[] {
  if (!isArray(value)) return false;
  
  return value.every(
    (item) =>
      isObject(item) &&
      typeof item.timestamp === 'string' &&
      typeof item.state === 'string'
  );
}

export interface JWTPayload {
  exp?: number;
  iat?: number;
  sub?: string;
}

export function isJWTPayload(value: unknown): value is JWTPayload {
  if (!isObject(value)) return false;
  
  const hasValidExp = !('exp' in value) || typeof value.exp === 'number';
  const hasValidIat = !('iat' in value) || typeof value.iat === 'number';
  const hasValidSub = !('sub' in value) || typeof value.sub === 'string';
  
  return hasValidExp && hasValidIat && hasValidSub;
}

export function isActiveFilterValue(value: unknown): value is string | number | boolean {
  return value !== null && value !== undefined && value !== '';
}

export function getStringProperty(obj: unknown, key: string): string | undefined {
  if (!isObject(obj)) return undefined;
  const value = obj[key];
  return typeof value === 'string' ? value : undefined;
}

export function getNumberProperty(obj: unknown, key: string): number | undefined {
  if (!isObject(obj)) return undefined;
  const value = obj[key];
  return typeof value === 'number' ? value : undefined;
}
