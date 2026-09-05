import { httpClient } from '@/api/core'
import { ApiRequestConfig } from '@/api/core/httpClient'
import {
    DeploymentApprovalDTO,
    DeploymentApprovalResponse,
    DeploymentApprovalsResponse,
    ProcessDefinitionDTO,
    ProcessDefinitionListResponse,
    ProcessOwnershipDTO,
    ProcessOwnershipResponse,
    ProcessOwnershipsResponse,
    RemoveDeputyRequest,
    RequestApprovalRequest,
    SubmitApprovalRequest,
} from '@/api/types/orchest-api'
import { ResponseDTO } from '@/api/types/types'

export class DeploymentApprovalsService {
  protected readonly baseUrl = '/orchest'
  protected readonly resourcePath = 'deploymentApprovals'

  protected getResourceUrl(path = ''): string {
    return `${this.baseUrl}/${this.resourcePath}${path}`
  }

  async getReviewApprovals(
    config?: ApiRequestConfig
  ): Promise<DeploymentApprovalDTO[]> {
    const response = await httpClient.get<DeploymentApprovalsResponse>(
      this.getResourceUrl('/reviews'),
      config
    )
    return response.data
  }

  async requestApproval(
    request: RequestApprovalRequest,
    config?: ApiRequestConfig
  ): Promise<DeploymentApprovalDTO> {
    const response = await httpClient.post<
      DeploymentApprovalResponse,
      RequestApprovalRequest
    >(this.getResourceUrl('/requestApproval'), request, config)
    return response.data
  }

  async submitApproval(
    request: SubmitApprovalRequest,
    config?: ApiRequestConfig
  ): Promise<DeploymentApprovalDTO> {
    const response = await httpClient.post<
      DeploymentApprovalResponse,
      SubmitApprovalRequest
    >(this.getResourceUrl('/submitApproval'), request, config)
    return response.data
  }

  async getOwnedProcesses(
    owner: string,
    config?: ApiRequestConfig
  ): Promise<ProcessOwnershipDTO[]> {
    const response = await httpClient.get<ProcessOwnershipsResponse>(
      this.getResourceUrl('/ownership'),
      {
        ...config,
        params: { owner },
      }
    )
    return response.data
  }

  async getDefinitions(
    config?: ApiRequestConfig
  ): Promise<ProcessDefinitionDTO[]> {
    const response = await httpClient.get<ProcessDefinitionListResponse>(
      this.getResourceUrl('/getDefinitions'),
      config
    )
    return response.data
  }

  async getApprovers(
    definitionId: string,
    config?: ApiRequestConfig
  ): Promise<string[]> {
    const response = await httpClient.get<{ data: string[] }>(
      this.getResourceUrl('/getApprovers'),
      {
        ...config,
        params: { definitionId },
      }
    )
    return response.data
  }

  async addApprovers(
    request: ProcessDefinitionDTO,
    config?: ApiRequestConfig
  ): Promise<ProcessDefinitionDTO> {
    const response = await httpClient.post<
      ResponseDTO<ProcessDefinitionDTO>,
      ProcessDefinitionDTO
    >(this.getResourceUrl('/addApprovers'), request, config)
    return response.data
  }

  async removeDeputy(
    request: RemoveDeputyRequest,
    config?: ApiRequestConfig
  ): Promise<ProcessOwnershipDTO> {
    const params = new URLSearchParams({
      approverId: request.definitionId,
      approverEmail: request.deputyEmail,
    })
    const response = await httpClient.delete<ProcessOwnershipResponse>(
      this.getResourceUrl(`/removeApprover?${params.toString()}`),
      config
    )
    return response.data
  }
}

export const deploymentApprovalsService = new DeploymentApprovalsService()
