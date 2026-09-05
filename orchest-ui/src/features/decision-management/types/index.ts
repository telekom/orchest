// Re-export API types for convenience
export type {
    DecisionDefinitionDTO,
    DecisionDefinitionResponse,
    DecisionDefinitionsResponse, DecisionInstanceDTO, DecisionInstanceQueryParams, DecisionInstanceResponse,
    DecisionInstancesResponse
} from '@/api/types/orchest-api';

// Re-export shared types
export type { DecisionInstance } from '@/shared/types';

// Hook-related types
export interface DecisionFilters {
  decisionId: string | null;
  version: string | null;
  searchText: string | null;
  status: string | null;
  from: string | null;
  to: string | null;
  timezone?: string | null;
}

export interface DecisionInstancesResult {
  instances: DecisionInstance[];
  isLoading: boolean;
  totalPages: number;
  totalElements: number;
  refetch: () => void;
  error?: unknown;
}

// Component prop types
export interface DecisionDiagramViewerProps {
  decisionId?: string;
  version?: number;
  matchedRuleid?: string;
  xml?: string;
  className?: string;
}
