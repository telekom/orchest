import { httpClient } from '@/api/core';

export interface ProcessState {
  id?: string;
  processId: string;
  status: boolean;
}

export class ProcessStateService {
  private readonly baseUrl = '/orchest/processStates';

  async getAll(): Promise<ProcessState[]> {
    return httpClient.get<ProcessState[]>(this.baseUrl);
  }

  async getByProcessId(processDefinitionId: string): Promise<ProcessState> {
    return httpClient.get<ProcessState>(`${this.baseUrl}/${processDefinitionId}`);
  }

  async add(data: ProcessState): Promise<ProcessState> {
    return httpClient.post<ProcessState>(this.baseUrl, data);
  }

  async upsert(data: ProcessState): Promise<ProcessState> {
    return httpClient.put<ProcessState>(this.baseUrl, data);
  }

  async remove(processDefinitionId: string): Promise<ProcessState> {
    return httpClient.delete<ProcessState>(`${this.baseUrl}/${processDefinitionId}`);
  }
}

export const processStateService = new ProcessStateService();
