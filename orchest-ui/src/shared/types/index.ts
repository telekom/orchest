import { Theme } from '@/shared/enums';
import { SequenceExecution } from '@/api/types/orchest-api';

export interface ApiResponse<T = unknown> {
  data: T;
  success: boolean;
  message?: string;
  errors?: string[];
}

/**
 * ProcessInstance - UI Layer Domain Model
 *
 * Represents process instance data in a format optimized for UI components.
 * This type is converted from ProcessInstanceDTO (API response) via typeMapping utilities.
 *
 * Use this type in all UI components, hooks, and pages. DO NOT use ProcessInstanceDTO directly in UI code.
 *
 * Key differences from ProcessInstanceDTO:
 * - Uses simplified field names (processId vs processDefinitionId, status vs state)
 * - Includes UI-friendly computed fields (processName, processVersion)
 * - Optimized structure for display and filtering
 *
 * @see ProcessInstanceDTO in @/api/types/orchest-api for API layer representation
 * @see mapDomainToContextProcessInstance in @/shared/utils/typeMapping for conversion logic
 */
export interface ProcessInstance {
  [key: string]: unknown;
  processId: string;
  parentProcessId: string;
  processName: string;
  processVersion: string;
  sequenceExecutions: Record<string, SequenceExecution>;
  status: string;
  startDate: string;
  endDate: string | null;
  incidentMessage?: string;
  lastActivityAt?: string;
  activeElements?: string[];
}

export interface DecisionInstance {
  [key: string]: unknown;
  decisionId: string;
  decisionInstanceId: string;
  processInstanceId: string;
  inputVariables: Record<string, unknown>;
  outputVariables: Record<string, unknown>;
  resourceUTF8XML: string;
  state: string;
  version: string;
  executedAt: string;
  matchedRuleId?: string | string[];
}

export interface OrchestrationConfig {
  id: string;
  windowDuration: number;
  allowedSize: number;
  processId: string;
  enabled: boolean;
}

export interface ProcessStats {
  total: number;
  active?: number;
  running?: number;
  completed?: number;
  failed?: number;
  cancelled?: number;
}

export interface FilterState {
  process: string | null;
  version: string | null;
  searchText: string;
  status: string | null;
  dateRange?: {
    start: Date;
    end: Date;
  };
}

export interface UIState {
  sidebarOpen: boolean;
  loading: boolean;
  error: string | null;
  theme: Theme;
}

export interface Environment {
  name: string;
  apiUrl: string;
  aiApiUrl: string;
  auth: {
    clientId: string;
    authority: string;
    redirectUri: string;
    scopes: string[];
  };
}

export interface ApiError {
  message: string;
  status: number;
  code?: string;
  details?: Record<string, unknown>;
}

export interface SortConfig {
  field: string;
  direction: 'asc' | 'desc';
}

export interface BpmnElement {
  id: string;
  type: string;
  name?: string;
  businessObject?: unknown;
}

export interface DmnTable {
  id: string;
  name: string;
  inputs: DmnInput[];
  outputs: DmnOutput[];
  rules: DmnRule[];
}

export interface DmnInput {
  id: string;
  label: string;
  type: string;
  expression?: string;
}

export interface DmnOutput {
  id: string;
  label: string;
  type: string;
}

export interface DmnRule {
  id: string;
  inputValues: Record<string, unknown>;
  outputValues: Record<string, unknown>;
}

export interface FormField {
  name: string;
  type: "text" | "number" | "boolean" | "select" | "date";
  label: string;
  required: boolean;
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    message?: string;
  };
  options?: { value: string; label: string }[];
}

export interface ChartDataPoint {
  label: string;
  value: number;
  color?: string;
}

export interface TimeSeriesDataPoint {
  timestamp: string;
  value: number;
  category?: string;
}
