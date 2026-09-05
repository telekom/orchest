import type { DeploymentApprovalDTO } from '@/api/types/orchest-api';
import { extractDefinitionInfo } from '@/shared/utils';
import { useMemo } from 'react';

interface ProcessDefinitionInfo {
  definitionId: string;
  definitionName: string;
  diagramType: 'bpmn' | 'dmn' | 'unknown';
}

export interface EnrichedApproval extends DeploymentApprovalDTO {
  definitionInfo: ProcessDefinitionInfo;
}

/**
 * Custom hook to enrich approvals with parsed XML definition info
 * Memoizes the result to prevent unnecessary re-computations
 */
export function useEnrichedApprovals(approvals: DeploymentApprovalDTO[]): EnrichedApproval[] {
  return useMemo((): EnrichedApproval[] => {
    return approvals.map((approval) => ({
      ...approval,
      definitionInfo: extractDefinitionInfo(
        approval.resourceDeploymentRequest.resourceUTF8XML
      ),
    }));
  }, [approvals]);
}
