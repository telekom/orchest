import { httpClient } from '@/api/core';
import { toISODateTime } from '@/api/core/utils/dateUtils';
import type {
  ActorRequest,
  AlertListParams,
  AlertResponse,
  AlertStats,
  PagedAlertResponse,
} from './types';

export class AlertService {
  private readonly baseUrl = '/orchest/alerts';

  async listAlerts(params: AlertListParams = {}): Promise<PagedAlertResponse> {
    return httpClient.get<PagedAlertResponse>(this.baseUrl, {
      params: {
        state: params.state ?? undefined,
        processDefinitionId: params.processDefinitionId ?? undefined,
        createdFrom: params.createdFrom
          ? toISODateTime(params.createdFrom, params.timezone ?? undefined)
          : undefined,
        createdTo: params.createdTo
          ? toISODateTime(params.createdTo, params.timezone ?? undefined)
          : undefined,
        page: params.page,
        size: params.size,
        sort: params.sort,
      },
    });
  }

  async getAlert(id: string): Promise<AlertResponse> {
    return httpClient.get<AlertResponse>(`${this.baseUrl}/${id}`);
  }

  async getFiringStats(): Promise<AlertStats[]> {
    return httpClient.get<AlertStats[]>(`${this.baseUrl}/stats/firing`);
  }

  async acknowledge(id: string, body: ActorRequest): Promise<AlertResponse> {
    return httpClient.post<AlertResponse>(`${this.baseUrl}/${id}/acknowledge`, body);
  }

  async silence(id: string, body: ActorRequest): Promise<AlertResponse> {
    return httpClient.post<AlertResponse>(`${this.baseUrl}/${id}/silence`, body);
  }

  async unmute(id: string): Promise<AlertResponse> {
    return httpClient.post<AlertResponse>(`${this.baseUrl}/${id}/unmute`);
  }

  async resolve(id: string): Promise<AlertResponse> {
    return httpClient.post<AlertResponse>(`${this.baseUrl}/${id}/resolve`);
  }
}

export const alertService = new AlertService();
