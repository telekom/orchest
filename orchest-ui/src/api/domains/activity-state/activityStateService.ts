import { httpClient } from '@/api/core';

export interface ActivityState {
  id?: string;
  processDefinitionId: string;
  version: number;
  activityId: string;
  description?: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export class ActivityStateService {
  private readonly baseUrl = '/orchest/activityStates';

  async getAll(): Promise<ActivityState[]> {
    return httpClient.get<ActivityState[]>(this.baseUrl);
  }

  async getByProcessDefinitionId(processDefinitionId: string): Promise<ActivityState[]> {
    return httpClient.get<ActivityState[]>(`${this.baseUrl}/${processDefinitionId}`);
  }

  async getByProcessDefinitionIdAndVersion(processDefinitionId: string, version: number): Promise<ActivityState[]> {
    return httpClient.get<ActivityState[]>(`${this.baseUrl}/${processDefinitionId}/${version}`);
  }

  async upsert(data: ActivityState): Promise<ActivityState> {
    return httpClient.put<ActivityState>(this.baseUrl, data);
  }

  async remove(processDefinitionId: string, version: number, activityId: string): Promise<ActivityState> {
    return httpClient.delete<ActivityState>(`${this.baseUrl}/${processDefinitionId}/${version}/${activityId}`);
  }
}

export const activityStateService = new ActivityStateService();
