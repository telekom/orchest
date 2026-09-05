import { httpClient } from '@/api/core';
import { ResponseDTO } from '@/api/types/types';

export type EnvVariableType = 'SECRET' | 'PLAIN_TEXT';

export interface ProcessEnvVariable {
  id?: string;
  processDefinitionId: string;
  name: string;
  value: string;
  type: EnvVariableType;
  createdAt?: string;
  updatedAt?: string;
}

type ProcessEnvVariableResponse = ResponseDTO<ProcessEnvVariable>;
type ProcessEnvVariableListResponse = ResponseDTO<ProcessEnvVariable[]>;

export class ProcessEnvVariablesService {
  private readonly baseUrl = '/orchest/processDefinitionsEnvs';

  async getAll(): Promise<ProcessEnvVariable[]> {
    const response = await httpClient.get<{ content: ProcessEnvVariable[] }>(this.baseUrl);
    return response.content ?? [];
  }

  async getByProcessDefinitionId(processDefinitionId: string): Promise<ProcessEnvVariable[]> {
    const response = await httpClient.get<ProcessEnvVariableListResponse>(
      `${this.baseUrl}/${processDefinitionId}`
    );
    return response.data;
  }

  async add(variable: ProcessEnvVariable): Promise<ProcessEnvVariable> {
    const response = await httpClient.post<ProcessEnvVariableResponse, ProcessEnvVariable>(
      this.baseUrl,
      variable
    );
    return response.data;
  }

  async update(variable: ProcessEnvVariable): Promise<ProcessEnvVariable> {
    const response = await httpClient.patch<ProcessEnvVariableResponse, ProcessEnvVariable>(
      this.baseUrl,
      variable
    );
    return response.data;
  }

  async remove(variable: ProcessEnvVariable): Promise<ProcessEnvVariable> {
    const response = await httpClient.delete<ProcessEnvVariableResponse>(this.baseUrl, {
      data: variable,
    });
    return response.data;
  }
}

export const processEnvVariablesService = new ProcessEnvVariablesService();
