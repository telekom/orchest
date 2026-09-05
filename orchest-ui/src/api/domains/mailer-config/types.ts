export interface AlertRecipients {
  to: string[];
  cc: string[];
  bcc: string[];
}

export interface AlertingMailerConfig {
  id: string;
  processId: string;
  alertingRecipient: AlertRecipients;
  createdAt: string;
  lastModifiedAt: string;
}

export interface MailerConfigRequest {
  processId: string;
  alertingRecipient: AlertRecipients;
}

export interface PagedMailerConfigResponse {
  content: AlertingMailerConfig[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
