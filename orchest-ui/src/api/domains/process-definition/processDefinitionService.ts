import { httpClient } from '@/api/core'
import { BaseDefinitionService } from '@/api/core/BaseApiService'
import {
    ProcessDefinitionListResponse,
    ProcessDefinitionQueryParams,
    ProcessDefinitionResponse,
    ProcessDefinitionsResponse,
    ResourceDefinitionDTO,
} from '@/api/types/orchest-api'

export class ProcessDefinitionService extends BaseDefinitionService<
  ResourceDefinitionDTO,
  ProcessDefinitionQueryParams
> {
  protected readonly baseUrl = '/orchest'
  protected readonly resourcePath = 'processDefinitions'

  async getProcessDefinitions(
    params?: ProcessDefinitionQueryParams
  ): Promise<ProcessDefinitionsResponse> {
    return httpClient.get<ProcessDefinitionsResponse>(this.getResourceUrl(), {
      params,
    })
  }

  async getProcessDefinition(
    processDefinitionId: string,
    version: number
  ): Promise<ResourceDefinitionDTO> {
    const response = await httpClient.get<ProcessDefinitionResponse>(
      this.getResourceUrl(`/${processDefinitionId}/${version}`)
    )
    return response.data
  }

  async listProcessDefinitionIds(): Promise<ResourceDefinitionDTO[]> {
    const response = await httpClient.get<ProcessDefinitionListResponse>(
      this.getResourceUrl('/ids')
    )
    return response.data
  }

}

export const processDefinitionService = new ProcessDefinitionService()
