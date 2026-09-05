import type { ResourceDeploymentRequestPayload } from '@/api/types/orchest-api';

export function buildResourceDeploymentRequest(
  xml: string,
  compensateFlow: boolean
): ResourceDeploymentRequestPayload {
  return {
    resourceUTF8XML: xml,
    partitionCount: 1,
    bypassWorkerValidation: true,
    approvers: [],
    compensateFlow,
  };
}
