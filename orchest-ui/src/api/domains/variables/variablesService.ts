import { httpClient } from '@/api/core';
import { BaseApiService } from '@/api/core/BaseApiService';
import { VariablesAPIResponse, VariablesRequest, VariablesResponse } from '@/api/types/orchest-api';

interface VariableEntity {
  processInstanceId: string;
  variables: Record<string, unknown>;
}

export class VariablesService extends BaseApiService<VariableEntity, void> {
  protected readonly baseUrl = '/orchest';
  protected readonly resourcePath = 'variables';

  async modifyVariables(request: VariablesRequest): Promise<VariablesResponse> {
    const response = await httpClient.post<VariablesAPIResponse>(
      this.getResourceUrl(),
      request
    );
    return response.data;
  }

  async getVariables(processInstanceId: string): Promise<VariablesResponse> {
    const response = await httpClient.get<VariablesAPIResponse>(
      this.getResourceUrl(`/${processInstanceId}`)
    );
    return response.data;
  }
}

export const variablesService = new VariablesService();
