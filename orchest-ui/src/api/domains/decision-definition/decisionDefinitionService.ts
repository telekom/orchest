import { httpClient } from '@/api/core'
import { BaseDefinitionService } from '@/api/core/BaseApiService'
import {
    DecisionDefinitionDTO,
    DecisionDefinitionQueryParams,
    DecisionDefinitionResponse,
    DecisionDefinitionsResponse,
    EvaluateDecisionRequest,
    EvaluateDecisionResponse,
} from '@/api/types/orchest-api'

export class DecisionDefinitionService extends BaseDefinitionService<
  DecisionDefinitionDTO,
  DecisionDefinitionQueryParams
> {
  protected readonly baseUrl = '/orchest'
  protected readonly resourcePath = 'decisionDefinitions'

  async getDecisionDefinitions(
    params?: DecisionDefinitionQueryParams
  ): Promise<DecisionDefinitionsResponse> {
    return httpClient.get<DecisionDefinitionsResponse>(this.getResourceUrl(), {
      params,
    })
  }

  async getDecisionDefinition(definitionId: string): Promise<DecisionDefinitionDTO> {
    const response = await httpClient.get<DecisionDefinitionResponse>(
      this.getResourceUrl(`/${definitionId}`)
    )
    return response.data
  }

  async getDecisionDefinitionByVersion(
    definitionId: string,
    version: number
  ): Promise<DecisionDefinitionDTO> {
    const response = await httpClient.get<DecisionDefinitionResponse>(
      this.getResourceUrl(`/${definitionId}/${version}`)
    )
    return response.data
  }

  async getDecisionXML(decisionId: string, version: string): Promise<{ resourceXML: string }> {
    return httpClient.get<{ resourceXML: string }>(
      this.getResourceUrl(`/${decisionId}/${version}/xml`)
    )
  }

  async listDecisionDefinitionIds(): Promise<DecisionDefinitionDTO[]> {
    const response = await httpClient.get<{ data: DecisionDefinitionDTO[] }>(
      this.getResourceUrl('/ids')
    )
    return response.data
  }

  async evaluateDecision(request: EvaluateDecisionRequest): Promise<EvaluateDecisionResponse> {
    return httpClient.post<EvaluateDecisionResponse>(
      this.getResourceUrl('/evaluate'),
      request
    )
  }

}

export const decisionDefinitionService = new DecisionDefinitionService()
