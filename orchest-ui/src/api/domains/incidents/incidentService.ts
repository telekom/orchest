import { httpClient } from '@/api/core';

export interface RaiseIncidentRequest {
  processInstanceId: string;
  incidentMessage: string;
}

export interface BatchRaiseIncidentRequest {
  processInstanceIds: string[];
  incidentMessage: string;
}

export class IncidentService {
  private readonly baseUrl = '/orchest/incidents';

  async raise(data: RaiseIncidentRequest): Promise<string> {
    return httpClient.post<string>(`${this.baseUrl}/raise`, data);
  }

  async raiseBatch(data: BatchRaiseIncidentRequest): Promise<string> {
    return httpClient.post<string>(`${this.baseUrl}/raise/batch`, data);
  }

  async resolve(processInstanceId: string): Promise<string> {
    return httpClient.post<string>(`${this.baseUrl}/resolve/${processInstanceId}`);
  }
}

export const incidentService = new IncidentService();