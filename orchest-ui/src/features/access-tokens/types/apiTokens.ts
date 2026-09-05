export interface ApiToken {
  tokenId: string;
  name: string;
  tokenPrefix: string;
  roles: string[];
  createdAt: string;
  expiresAt: string;
  lastUsedAt: string;
  revoked: boolean;
}

export interface CreateApiTokenRequest {
  name: string;
  ttlInDays?: number;
}

export interface CreateApiTokenResponse {
  tokenId: string;
  name: string;
  token: string;
  roles: string[];
  createdAt: string;
  expiresAt: string;
}

export interface ApiTokenListResponse {
  meta: Record<string, unknown>;
  data: ApiToken[];
}

export interface ApiTokenCreateResponse {
  meta: Record<string, unknown>;
  data: CreateApiTokenResponse;
}
