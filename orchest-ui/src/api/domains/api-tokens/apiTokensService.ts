import { httpClient } from '@/api/core';
import type {
  ApiTokenCreateResponse,
  ApiTokenListResponse,
  CreateApiTokenRequest,
} from '@/features/access-tokens/types/apiTokens';

export class ApiTokensService {
  private readonly baseUrl = '/orchest/apiTokens';

  async listTokens(): Promise<ApiTokenListResponse> {
    return httpClient.get<ApiTokenListResponse>(this.baseUrl);
  }

  async createToken(request: CreateApiTokenRequest): Promise<ApiTokenCreateResponse> {
    return httpClient.post<ApiTokenCreateResponse>(this.baseUrl, request);
  }

  async revokeToken(tokenId: string): Promise<void> {
    await httpClient.delete(`${this.baseUrl}/${tokenId}`);
  }

  async revokeAllTokens(): Promise<void> {
    await httpClient.delete(this.baseUrl);
  }
}

export const apiTokensService = new ApiTokensService();
