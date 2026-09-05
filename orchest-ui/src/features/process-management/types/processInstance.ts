// Re-export ProcessInstance from shared types for backward compatibility
export { ProcessInstance } from '@/shared/types';

export interface Process {
  id: string;
  name: string;
  description: string;
  version: string;
}

export interface Task {
  taskId: string;
  taskName: string;
  status: string;
  nodeType?: string;
  timestamp: Array<{ timestamp: string; state: string }>;
}

export interface ProcessVariable {
  name: string;
  value: string;
  type: 'string' | 'number' | 'boolean' | 'object';
  scope: 'global' | 'local';
}

export type RawVariableValue = string | number | boolean | null | undefined | { [key: string]: unknown };

export interface UseProcessInstanceReturn {
  instance: ProcessInstance | null;
  loading: boolean;
  error: unknown;
  diagramXml: string;
  diagramLoading: boolean;
  tasks: Task[];
  tasksLoading: boolean;
  variables: ProcessVariable[];
  variablesLoading: boolean;
  fetchInstance: (fullRefresh?: boolean) => Promise<void>;
  getVariables: (variables: Map<string, unknown>) => ProcessVariable[];
}
