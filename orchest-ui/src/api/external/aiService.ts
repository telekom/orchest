import { httpClient } from "@/api/core/httpClient";
import { environment } from "@/shared/constants/environment";
import { MIME_TYPES } from "@/shared/constants";
import { logger } from "@/shared/utils/logger";


export interface ProjectResponse {
  projectId: string;
  name: string;
  status?: string;
}

export interface BpmnResponse {
  bpmn: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp?: string;
}

export interface BpmnProcess {
  id?: string;
  name?: string;
  xml?: string;
  [key: string]: unknown;
}

export interface BpmnJsonResponse {
  elements: Array<{
    id: string;
    name?: string;
    type: string;
    [key: string]: unknown;
  }>;
}

export interface StatusResponse {
  status: string;
}


class AIService {


  async generateProject(
    projectName: string,
    bpmnPayload: string,
    groupName: string
  ): Promise<ProjectResponse> {
    const url = `${environment.aiApiUrl}/createProject`;

    return httpClient.external<ProjectResponse>(url, {
      method: "POST",
      body: JSON.stringify({
        projectName,
        bpmnPayload,
        groupName,
      }),
    });
  }

  async generateBPMN(prompt: string): Promise<BpmnResponse> {
    const url = `${environment.aiApiUrl}/createBpmn`;

    return httpClient.external<BpmnResponse>(url, {
      method: "POST",
      body: JSON.stringify({ prompt }),
    });
  }

  async checkCodeGenerationStatus(projectId: string): Promise<StatusResponse> {
    const url = `${environment.aiApiUrl}/checkStatus/${projectId}`;

    return httpClient.external<StatusResponse>(url, {
      method: "GET",
    });
  }

  async downloadProject(projectId: string): Promise<void> {
    try {
      const url = `${environment.aiApiUrl}/downloadProject/${projectId}`;
      const blob = await httpClient.external<Blob>(url, {
        method: "GET",
      });

      const fileURL = window.URL.createObjectURL(blob as Blob);
      const link = document.createElement("a");
      link.href = fileURL;
      link.download = `project-${projectId}.zip`;

      document.body.appendChild(link);
      link.click();

      document.body.removeChild(link);
      window.URL.revokeObjectURL(fileURL);
    } catch (error) {
      logger.error("Error downloading ZIP file:", error);
      throw error;
    }
  }


  async createBpmnJson(xml: string): Promise<BpmnJsonResponse> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/bpmn_to_json`;

    return httpClient.external(url, {
      method: "POST",
      body: JSON.stringify({ bpmn_xml: xml }),
    });
  }

  async processBpmnDiagram(bpmnXml: string): Promise<{ layoutedXml: string }> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/process-bpmn`;

    return httpClient.external<{ layoutedXml: string }>(url, {
      method: "POST",
      body: JSON.stringify({ bpmnXml }),
    });
  }


  async generateBpmnFromChat(
    messageHistory: ChatMessage[],
    process: BpmnProcess,
    model: string,
    needsToBeFinalComment: boolean = false
  ): Promise<ReadableStream<Uint8Array> | null> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/talk`;

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": MIME_TYPES.JSON,
        'X-Request-Id': crypto.randomUUID(),
        'X-Request-Timestamp': new Date().toISOString(),
      },
      body: JSON.stringify({
        message_history: messageHistory,
        process,
        model,
        needs_to_be_final_comment: needsToBeFinalComment,
      }),
    });

    if (!response.ok || !response.body) {
      throw new Error(`Failed to generate BPMN: ${response.statusText}`);
    }

    return response.body;
  }

  async determineIntent(messageHistory: ChatMessage[], model: string): Promise<{ intent: string }> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/determine_intent`;

    return httpClient.external<{ intent: string }>(url, {
      method: "POST",
      body: JSON.stringify({
        message_history: messageHistory,
        model,
      }),
    });
  }

  async modifyBpmn(
    messageHistory: ChatMessage[],
    process: BpmnProcess,
    model: string
  ): Promise<ReadableStream<Uint8Array> | null> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/modify`;

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": MIME_TYPES.JSON,
        'X-Request-Id': crypto.randomUUID(),
        'X-Request-Timestamp': new Date().toISOString(),
      },
      body: JSON.stringify({
        message_history: messageHistory,
        process,
        model,
      }),
    });

    if (!response.ok || !response.body) {
      throw new Error(`Failed to modify BPMN: ${response.statusText}`);
    }

    return response.body;
  }

  async getAvailableProviders(): Promise<Record<string, boolean>> {
    const promptBasePath = environment.bpmnPromptBasePath;
    const url = `${promptBasePath}/available_providers`;

    return httpClient.external<Record<string, boolean>>(url);
  }
}


export const aiService = new AIService();
export default aiService;
