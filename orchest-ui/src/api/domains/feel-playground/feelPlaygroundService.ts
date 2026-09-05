import { httpClient } from "@/api/core";

const FEEL_BASE = "/orchest/feelPlayground";

export interface FeelValidateRequest {
  expression: string;
}

export interface FeelValidationDTO {
  valid?: boolean;
  error?: string;
}

export interface FeelEvaluateRequest {
  expression: string;
  variables?: Record<string, unknown>;
}

export interface FeelEvaluationDTO {
  success?: boolean;
  result?: unknown;
  error?: string;
}

interface ResponseDTO<T> {
  meta?: unknown;
  data?: T;
}

const unwrap = <T>(raw: ResponseDTO<T> | T | undefined | null): T => {
  if (raw && typeof raw === "object" && "data" in (raw as Record<string, unknown>)) {
    return (raw as ResponseDTO<T>).data as T;
  }
  return raw as T;
};

const PUBLIC_REQUEST_CONFIG = { skipAuthHeader: true } as const;

export class FeelPlaygroundService {
  async validate(expression: string): Promise<FeelValidationDTO> {
    const body: FeelValidateRequest = { expression };
    const raw = await httpClient.post<ResponseDTO<FeelValidationDTO> | FeelValidationDTO>(
      `${FEEL_BASE}/validate`,
      body,
      PUBLIC_REQUEST_CONFIG,
    );
    return unwrap<FeelValidationDTO>(raw) ?? {};
  }

  async evaluate(
    expression: string,
    variables?: Record<string, unknown>,
  ): Promise<FeelEvaluationDTO> {
    const body: FeelEvaluateRequest = { expression, variables };
    const raw = await httpClient.post<ResponseDTO<FeelEvaluationDTO> | FeelEvaluationDTO>(
      `${FEEL_BASE}/evaluate`,
      body,
      PUBLIC_REQUEST_CONFIG,
    );
    return unwrap<FeelEvaluationDTO>(raw) ?? {};
  }
}

export const feelPlaygroundService = new FeelPlaygroundService();
