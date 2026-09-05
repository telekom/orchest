import { httpClient } from '@/api/core';
import { ApiRequestConfig } from '@/api/core/httpClient';

interface ConnectorData {
  connectorData: unknown;
}

interface ConnectorTemplates {
  content: ConnectorData[];
}

export class ConnectorService {
  protected readonly baseUrl = '/orchest';
  protected readonly resourcePath = 'connectors';

  protected getResourceUrl(path = ''): string {
    return `${this.baseUrl}/${this.resourcePath}${path}`;
  }

  async getConnectorTemplates(config?: ApiRequestConfig): Promise<ConnectorTemplates> {
    return httpClient.get<ConnectorTemplates>(this.getResourceUrl(), config);
  }
}

export const connectorService = new ConnectorService();
