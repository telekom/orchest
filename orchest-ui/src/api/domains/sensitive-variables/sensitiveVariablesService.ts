import { httpClient } from '@/api/core';

export type SensitiveVariableType = 'HIDE' | 'SHOW';

export interface SensitiveVariable {
  name: string;
  value?: string;
  type: SensitiveVariableType;
}

export interface ProcessSensitiveVariables {
  id?: string;
  processDefinitionId: string;
  variables: SensitiveVariable[];
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

interface ResponseDTO<T> {
  data: T;
}

export class SensitiveVariablesService {
  private readonly baseUrl = '/orchest/processDefinitionsSensitiveVariables';

  async getAll(page = 0, size = 100): Promise<PageResponse<ProcessSensitiveVariables>> {
    return httpClient.get<PageResponse<ProcessSensitiveVariables>>(this.baseUrl, {
      params: { page, size },
    });
  }

  async getByProcessDefinitionId(processDefinitionId: string): Promise<ProcessSensitiveVariables[]> {
    const response = await httpClient.get<ResponseDTO<ProcessSensitiveVariables[]>>(
      `${this.baseUrl}/${processDefinitionId}`
    );
    return response.data ?? [];
  }

  async add(data: ProcessSensitiveVariables): Promise<ProcessSensitiveVariables> {
    const response = await httpClient.post<ResponseDTO<ProcessSensitiveVariables>>(this.baseUrl, data);
    return response.data;
  }

  async update(data: ProcessSensitiveVariables): Promise<ProcessSensitiveVariables> {
    const response = await httpClient.patch<ResponseDTO<ProcessSensitiveVariables>, ProcessSensitiveVariables>(
      this.baseUrl,
      data
    );
    return response.data;
  }

  async delete(data: ProcessSensitiveVariables): Promise<void> {
    await httpClient.delete(this.baseUrl, { data });
  }
}

export const sensitiveVariablesService = new SensitiveVariablesService();