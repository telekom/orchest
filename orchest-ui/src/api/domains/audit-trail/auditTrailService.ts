import { httpClient } from '@/api/core';
import type {
  AuditTrailEntry,
  PathFilterParams,
  TimeFilterParams,
  UserFilterParams,
} from '@/features/audit-trail/types/auditTrail';

export class AuditTrailService {
  private readonly baseUrl = '/orchest/auditTrails';

  async getByUser(params: UserFilterParams): Promise<AuditTrailEntry[]> {
    return httpClient.get<AuditTrailEntry[]>(`${this.baseUrl}/user`, { params });
  }

  async getByTimeRange(params: TimeFilterParams): Promise<AuditTrailEntry[]> {
    return httpClient.get<AuditTrailEntry[]>(`${this.baseUrl}/time`, { params });
  }

  async getByPath(params: PathFilterParams): Promise<AuditTrailEntry[]> {
    return httpClient.get<AuditTrailEntry[]>(`${this.baseUrl}/path`, { params });
  }
}

export const auditTrailService = new AuditTrailService();