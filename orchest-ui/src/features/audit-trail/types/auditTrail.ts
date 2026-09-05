export interface AuditTrailEntry {
  id: string;
  userEmail: string;
  roles: string[];
  admin: boolean;
  httpMethod: string;
  path: string;
  responseStatus: number;
  correlationId: string;
  createdAt: string;
}

export type AuditFilterMode = 'user' | 'time' | 'path';

export interface UserFilterParams {
  email: string;
  limit?: number;
}

export interface TimeFilterParams {
  from: string;
  to: string;
  limit?: number;
}

export interface PathFilterParams {
  path: string;
  from: string;
  to: string;
  limit?: number;
}