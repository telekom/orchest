import { globalErrorHandler } from '../error/globalErrorHandler';
import { toError } from '../hooks/useErrorHandling';
import standardModalStyles from '@/shared/components/StandardModal/StandardModal.module.css';

export interface TransformedVariable {
  name: string;
  value: string;
  type: string;
  scope: string;
}

export interface ValidationResult {
  isValid: boolean;
  errorMessage?: string;
}

export interface ConversionResult {
  success: boolean;
  value?: unknown;
  errorMessage?: string;
}

type VariableType = 'string' | 'number' | 'boolean' | 'object';

const SERIALIZATION_ERROR = "[Serialization Error]";
const DEFAULT_JSON_SPACES = 2;

const handleError = (error: unknown, context: string, additionalContext?: Record<string, unknown>): void => {
  globalErrorHandler.handleError(toError(error), 'runtime', {
    context,
    severity: 'low',
    ...additionalContext,
  });
};

const getValueType = (value: unknown): VariableType => {
  if (value === null || value === undefined) return "string";

  const type = typeof value;
  if (type === "string" || type === "number" || type === "boolean") {
    return type as VariableType;
  }

  return "object";
};

const formatValue = (value: unknown, type: VariableType): string => {
  if (value === null || value === undefined) return "";

  if (type === "object") {
    try {
      return JSON.stringify(value);
    } catch (error) {
      handleError(error, 'variable-serialization');
      return SERIALIZATION_ERROR;
    }
  }

  return String(value);
};

function transformSingleVariable(
  name: string,
  value: unknown,
  scope: string
): TransformedVariable {
  const type = getValueType(value);
  const formattedValue = formatValue(value, type);

  return { name, value: formattedValue, type, scope };
}

export function transformVariablesToArray(
  variables: Map<string, unknown> | Record<string, unknown> | null | undefined,
  defaultScope: string = "global"
): TransformedVariable[] {
  if (!variables) return [];

  const entries = variables instanceof Map
    ? Array.from(variables.entries())
    : Object.entries(variables);

  return entries.map(([key, value]) => transformSingleVariable(key, value, defaultScope));
}

function safeOperation<T>(
  operation: () => T,
  fallback: T,
  errorContext: string
): T {
  try {
    return operation();
  } catch (error) {
    handleError(error, errorContext);
    return fallback;
  }
}

const parseByType = (value: string, type: VariableType): unknown => {
  switch (type) {
    case "number":
      return Number(value);
    case "boolean":
      return value === "true" || value === "1" || value.toLowerCase() === "true";
    case "object":
      return JSON.parse(value);
    case "string":
    default:
      return value;
  }
};

export function parseVariableValue(value: string, type: string): unknown {
  return safeOperation(
    () => parseByType(value, type as VariableType),
    value,
    'variable-parsing'
  );
}

export function isValidJson(value: string): boolean {
  try {
    JSON.parse(value);
    return true;
  } catch {
    return false;
  }
}

export function isJsonObject(value: string): boolean {
  try {
    const parsed = JSON.parse(value);
    return typeof parsed === 'object' && parsed !== null && !Array.isArray(parsed);
  } catch {
    return false;
  }
}

export function formatJsonValue(value: string | object, spaces: number = DEFAULT_JSON_SPACES): string {
  return safeOperation(
    () => {
      const obj = typeof value === "string" ? JSON.parse(value) : value;
      return JSON.stringify(obj, null, spaces);
    },
    typeof value === "string" ? value : String(value),
    'json-formatting'
  );
}

export function safeJsonParse<T = unknown>(value: string, fallback: T | null = null): T | null {
  return safeOperation(
    () => JSON.parse(value) as T,
    fallback,
    'json-parsing'
  );
}

export function safeJsonStringify(value: unknown, spaces: number = 0, fallback: string = ''): string {
  return safeOperation(
    () => JSON.stringify(value, null, spaces),
    fallback,
    'json-stringify'
  );
}

export function validateVariableForm(
  name: string,
  value: string,
  type: string,
  isValidJsonValue: boolean
): ValidationResult {
  if (!name.trim() || !value.trim() || !type) {
    return { isValid: false, errorMessage: "Please fill in all fields" };
  }

  if (type === "object" && !isValidJsonValue) {
    return { isValid: false, errorMessage: "Please correct the JSON format before saving" };
  }

  return { isValid: true };
}

export function convertValueByType(
  value: string,
  type: string
): ConversionResult {
  if (type === "string") {
    return { success: true, value };
  }

  if (type === "object") {
    if (!isValidJson(value)) {
      return { success: false, errorMessage: "Invalid JSON format" };
    }
    return { success: true, value: JSON.parse(value) };
  }

  if (type === "number") {
    if (isNaN(Number(value))) {
      return { success: false, errorMessage: "Value should be a number" };
    }
    return { success: true, value: Number(value) };
  }

  if (type === "boolean") {
    return { success: true, value: value.toLowerCase() === "true" };
  }

  return { success: false, errorMessage: "Invalid type" };
}

export function getVariableModalSize(type: string): "fullscreen" | "2xl" {
  return type === "object" ? "fullscreen" : "2xl";
}

export function getVariableModalContentClassName(type: string, mode: "edit" | "view"): string | undefined {
  if (type !== "object") return undefined;

  return mode === "edit"
    ? standardModalStyles.contentWrapperObjectEdit
    : standardModalStyles.contentWrapperObjectView;
}
