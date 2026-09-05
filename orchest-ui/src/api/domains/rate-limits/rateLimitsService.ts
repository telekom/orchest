import { httpClient } from '@/api/core';
import {
    RateLimit,
    RateLimitResponse
} from '@/api/types/orchest-api';

export class RateLimitsService {
  private readonly baseUrl = '/orchest';

  async upsertRateLimit(rateLimit: RateLimit): Promise<RateLimit> {
    const response = await httpClient.put<RateLimitResponse>(
      `${this.baseUrl}/rateLimit`,
      rateLimit
    );
    return response.data;
  }

  async getRateLimits(): Promise<RateLimit[]> {
    return httpClient.get<RateLimit[]>(`${this.baseUrl}/rateLimits`);
  }

  async getRateLimitForProcessId(processDefinitionId: string): Promise<RateLimit> {
    const response = await httpClient.get<RateLimitResponse>(
      `${this.baseUrl}/rateLimit/${processDefinitionId}`
    );
    return response.data;
  }
}

export const rateLimitsService = new RateLimitsService();
