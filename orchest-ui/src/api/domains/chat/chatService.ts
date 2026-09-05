import { httpClient } from "@/api/core";
import { tenantService } from "@/shared/services/TenantService";

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export type ChatType = 'DETAILED_ANALYSIS' | 'INCIDENT_ANALYSIS' | 'SUGGEST_FIX';

export interface ChatRequest {
  userMessage: string;
  chatType?: ChatType;
}

class ChatService {
  private readonly path = "/orchest/chats";

  async analyseStream(request: ChatRequest, signal?: AbortSignal): Promise<ReadableStream<Uint8Array>> {
    const url = `${httpClient.getBaseURL().replace(/\/$/, '')}${this.path}/analyse`;

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Tenant-Id": tenantService.getTenant(),
      },
      body: JSON.stringify(request),
      signal,
    });

    if (!response.ok || !response.body) {
      throw new Error(`Chat request failed: ${response.status} ${response.statusText}`);
    }

    return response.body;
  }
}

export const chatService = new ChatService();
