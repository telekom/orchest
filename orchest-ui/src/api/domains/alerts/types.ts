export type AlertState = 'FIRING' | 'ACKNOWLEDGED' | 'SILENCED' | 'RESOLVED' | 'DISABLED';

export interface AlertRecipients {
  to?: string[];
  cc?: string[];
  bcc?: string[];
}

export interface AlertResponse {
  id: string;
  source?: string;
  alertKey?: string;
  fingerprint?: string;
  state: AlertState;
  count: number;
  severity?: string;
  metadata?: Record<string, string>;
  subject?: string;
  body?: string;
  recipients?: AlertRecipients;
  resendIntervalMs?: number;
  createdAt?: string;
  updatedAt?: string;
  lastTriggeredAt?: string;
  resolvedAt?: string;
  acknowledgedAt?: string;
  acknowledgedBy?: string;
  silencedAt?: string;
  silencedBy?: string;
}

export interface PagedAlertResponse {
  content: AlertResponse[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface AlertStats {
  processDefinitionId: string;
  version: string;
  totalCount: number;
}

export interface ActorRequest {
  actor: string;
}

export interface AlertListParams {
  state?: AlertState | null;
  processDefinitionId?: string | null;
  createdFrom?: string | null;
  createdTo?: string | null;
  timezone?: string | null;
  page?: number;
  size?: number;
  sort?: string;
}
