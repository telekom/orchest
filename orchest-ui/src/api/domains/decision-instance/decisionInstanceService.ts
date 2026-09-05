import { httpClient } from '@/api/core';
import { BaseApiService } from '@/api/core/BaseApiService';
import { toISODateTime } from '@/api/core/utils/dateUtils';
import {
    DecisionDefinitionDTO,
    DecisionDefinitionsResponse,
    DecisionInstanceDTO,
    DecisionInstanceQueryParams,
    DecisionInstanceResponse,
    DecisionInstanceScrollDTO,
    DecisionInstanceScrollParams,
    DecisionInstancesResponse,
    ResourceDeploymentAPIResponse,
    ResourceDeploymentRequest,
    ResourceDeploymentResponse,
} from '@/api/types/orchest-api';

export class DecisionInstanceService extends BaseApiService<
  DecisionInstanceDTO,
  DecisionInstanceQueryParams
> {
  protected readonly baseUrl = '/orchest';
  protected readonly resourcePath = 'decisionInstances';

  async getDecisionInstances(
    params?: DecisionInstanceQueryParams,
  ): Promise<DecisionInstancesResponse> {
    const cleanParams: Record<string, unknown> = {};

    if (params) {
      if (params.decisionId) cleanParams.decisionId = params.decisionId;
      if (params.version !== undefined) cleanParams.version = params.version;
      if (params.state) cleanParams.state = params.state;
      if (params.searchText && params.searchText.trim() !== '')
        cleanParams.searchText = params.searchText;
      if (params.from) cleanParams.from = toISODateTime(params.from, params.timezone);
      if (params.to) cleanParams.to = toISODateTime(params.to, params.timezone);
      if (params.page !== undefined) cleanParams.page = params.page;
      if (params.size !== undefined) cleanParams.size = params.size;
    }

    return httpClient.get<DecisionInstancesResponse>(
      this.getResourceUrl(),
      { params: cleanParams }
    );
  }

  async scrollDecisionInstances(
    params: DecisionInstanceScrollParams,
  ): Promise<DecisionInstanceScrollDTO> {
    const cleanParams: Record<string, unknown> = {
      from: params.from ?? 0,
      to: params.to,
      sort: params.sort,
    };

    if (params.decisionId) cleanParams.decisionId = params.decisionId;
    if (params.version !== undefined) cleanParams.version = params.version;
    if (params.state) cleanParams.state = params.state;
    if (params.searchText && params.searchText.trim() !== '')
      cleanParams.searchText = params.searchText;
    if (params.executedFrom) cleanParams.executedFrom = toISODateTime(params.executedFrom, params.timezone);
    if (params.executedTo) cleanParams.executedTo = toISODateTime(params.executedTo, params.timezone);

    return httpClient.get<DecisionInstanceScrollDTO>(
      this.getResourceUrl('/scroll'),
      { params: cleanParams }
    );
  }

  async getDecisionInstance(decisionInstanceId: string): Promise<DecisionInstanceDTO> {
    const response = await httpClient.get<DecisionInstanceResponse>(
      this.getResourceUrl(`/${decisionInstanceId}`)
    );
    return response.data;
  }

  async deployResource(request: ResourceDeploymentRequest): Promise<ResourceDeploymentResponse> {
    const response = await httpClient.post<ResourceDeploymentAPIResponse>(
      this.getResourceUrl('/upload'),
      request
    );
    return response.data;
  }

  async getDecisionIDs(): Promise<DecisionDefinitionDTO[]> {
    const response = await httpClient.get<DecisionDefinitionsResponse>(
      `${this.baseUrl}/decisionDefinitions`,
      { params: { page: 0, size: 1000 } }
    );
    return response.content;
  }

  async getDecisionXML(decisionId: string, version: number): Promise<DecisionDefinitionDTO> {
    const response = await httpClient.get<{ data: DecisionDefinitionDTO }>(
      `${this.baseUrl}/decisionDefinitions/${decisionId}/${version}`
    );
    return response.data;
  }
}

export const decisionInstanceService = new DecisionInstanceService();
