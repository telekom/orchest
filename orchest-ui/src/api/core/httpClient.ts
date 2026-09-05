import { PagedResponse } from "@/api/types/types";
import { EXTERNAL_API_DOMAINS, MIME_TYPES } from "@/shared/constants";
import { HTTP_CLIENT_CONFIG } from "@/shared/constants/apiConfig";
import { environment } from "@/shared/constants/environment";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import axios, { AxiosError, AxiosInstance, AxiosResponse } from "axios";
import { v4 as uuidv4 } from "uuid";
import { enrichRequest } from "./interceptors/requestInterceptor";
import { enrichErrorContext, handle401Error } from "./interceptors/responseInterceptor";
import { ApiRequestConfig } from "./types";
import { executeRetry, parseSSEStream, shouldRetryRequest, validateExternalUrl } from "./utils";

export type { PagedResponse } from "@/api/types/types";
export type { ApiError, ApiRequestConfig, ApiResponse } from "./types";

class ApiClient {
  private instance: AxiosInstance;
  private readonly baseURL: string;
  private readonly timeout: number = HTTP_CLIENT_CONFIG.DEFAULT_TIMEOUT;
  private readonly allowedExternalDomains: string[];

  constructor() {
    this.baseURL = environment.apiUrl;
    this.allowedExternalDomains = [
      environment.aiApiUrl,
      EXTERNAL_API_DOMAINS.OPENAI,
      EXTERNAL_API_DOMAINS.ANTHROPIC,
      ...environment.security.allowedExternalDomains
    ].filter(Boolean);
    this.instance = this.createAxiosInstance();
    this.setupInterceptors();
  }

  private createAxiosInstance(): AxiosInstance {
    return axios.create({
      baseURL: this.baseURL,
      timeout: this.timeout,
      headers: {
        'Content-Type': MIME_TYPES.JSON,
        'Accept': MIME_TYPES.JSON,
      },
    });
  }

  private setupInterceptors(): void {
    this.instance.interceptors.request.use(
      enrichRequest,
      (error: AxiosError) => Promise.reject(globalErrorHandler.handleApiError(error))
    );

    this.instance.interceptors.response.use(
      (response: AxiosResponse) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as ApiRequestConfig;

        try {
          return await handle401Error(error, this.instance);
        } catch {
          const handledError = enrichErrorContext(error, originalRequest);

          if (shouldRetryRequest(error, originalRequest)) {
            return executeRetry(originalRequest, this.instance);
          }

          return Promise.reject(handledError);
        }
      }
    );
  }

  async get<T = unknown>(url: string, config?: ApiRequestConfig): Promise<T> {
    const response = await this.instance.get<T>(url, config);
    return response.data;
  }

  async post<T = unknown, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.instance.post<T>(url, data, config);
    return response.data;
  }

  async put<T = unknown, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.instance.put<T>(url, data, config);
    return response.data;
  }

  async patch<T = unknown, D = unknown>(url: string, data?: D, config?: ApiRequestConfig): Promise<T> {
    const response = await this.instance.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T = unknown>(url: string, config?: ApiRequestConfig): Promise<T> {
    const response = await this.instance.delete<T>(url, config);
    return response.data;
  }

  async getPaged<T = unknown>(url: string, params?: Record<string, unknown>, config?: ApiRequestConfig): Promise<PagedResponse<T>> {
    return this.get<PagedResponse<T>>(url, { ...config, params });
  }

  async postForm<T = unknown>(url: string, formData: FormData, config?: ApiRequestConfig): Promise<T> {
    return this.post<T>(url, formData, {
      ...config,
      headers: {
        ...config?.headers,
        'Content-Type': MIME_TYPES.MULTIPART_FORM,
      },
    });
  }

  async downloadFile(url: string, config?: ApiRequestConfig): Promise<Blob> {
    const response = await this.instance.get(url, {
      ...config,
      responseType: 'blob',
    });
    return response.data;
  }

  async external<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
    validateExternalUrl(url, this.allowedExternalDomains);

    // Use the canonical HTTPS URL returned by the URL parser to prevent SSRF
    const safeUrl = new URL(url).href;

    const response = await fetch(safeUrl, {
      headers: {
        'Content-Type': MIME_TYPES.JSON,
        'X-Request-Id': uuidv4(),
        'X-Request-Timestamp': new Date().toISOString(),
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`External API Error: ${response.status} ${response.statusText}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes(MIME_TYPES.JSON)) {
      return response.json();
    }

    return response.text() as unknown as T;
  }

  async *stream<T = unknown>(url: string, config?: ApiRequestConfig): AsyncGenerator<T, void, unknown> {
    if (!url.startsWith('/')) {
      throw new Error('Stream URLs must be relative paths starting with /');
    }

    const headers: Record<string, string> = {
      'Accept': MIME_TYPES.EVENT_STREAM,
      'Cache-Control': 'no-cache',
    };

    if (config?.headers) {
      Object.entries(config.headers).forEach(([key, value]) => {
        if (typeof value === 'string') {
          headers[key] = value;
        }
      });
    }

    // Construct a validated absolute URL from the known base and the relative path
    const safeUrl = new URL(url, this.baseURL).href;

    const response = await fetch(safeUrl, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      throw new Error(`Stream Error: ${response.status} ${response.statusText}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('Response body is not readable');
    }

    yield* parseSSEStream<T>(reader);
  }

  getBaseURL(): string {
    return this.baseURL;
  }

  getInstance(): AxiosInstance {
    return this.instance;
  }
}

export const httpClient = new ApiClient();
export const apiClient = httpClient;
export default httpClient;
