import { httpClient } from '@/api/core/httpClient';
import type { AlertingMailerConfig, MailerConfigRequest, PagedMailerConfigResponse } from './types';

const BASE_PATH = '/orchest/alerts/mailer-configs';

class MailerConfigService {
  async list(page = 0, size = 20): Promise<PagedMailerConfigResponse> {
    return httpClient.get<PagedMailerConfigResponse>(BASE_PATH, { params: { page, size } });
  }

  async getById(id: string): Promise<AlertingMailerConfig> {
    return httpClient.get<AlertingMailerConfig>(`${BASE_PATH}/${id}`);
  }

  async create(request: MailerConfigRequest): Promise<AlertingMailerConfig> {
    return httpClient.post<AlertingMailerConfig>(BASE_PATH, request);
  }

  async update(id: string, request: MailerConfigRequest): Promise<AlertingMailerConfig> {
    return httpClient.put<AlertingMailerConfig>(`${BASE_PATH}/${id}`, request);
  }

  async delete(id: string): Promise<void> {
    return httpClient.delete(`${BASE_PATH}/${id}`);
  }
}

export const mailerConfigService = new MailerConfigService();
