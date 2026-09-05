import { httpClient } from "@/api/core";
import { BaseApiService } from "@/api/core/BaseApiService";
import {
    AssignTaskRequest,
    CompleteTaskRequest,
    UserTaskDTO,
    UserTaskQueryParams,
    UserTaskResponse,
    UserTasksResponse,
} from "@/api/types/orchest-api";

/**
 * UserTaskService - API service for managing BPMN User Tasks
 *
 * Provides methods for:
 * - Listing and querying user tasks
 * - Claiming and unclaiming tasks
 * - Completing tasks with variables
 * - Reassigning tasks to different users
 *
 * @extends BaseApiService
 */
export class UserTaskService extends BaseApiService<
  UserTaskDTO,
  UserTaskQueryParams
> {
  protected readonly baseUrl = "/orchest";
  protected readonly resourcePath = "userTasks";

  /**
   * Get all user tasks available to the authenticated user
   * Filters tasks based on assignment rules (assignee, candidate users/groups)
   *
   * @param params - Optional query parameters for filtering and pagination
   * @returns Promise resolving to array of user tasks
   */
  async getUserTasks(params?: UserTaskQueryParams): Promise<UserTasksResponse> {
    const cleanParams: Record<string, unknown> = {};

    if (params) {
      if (params.state) cleanParams.state = params.state;
      if (params.assignee) cleanParams.assignee = params.assignee;
      if (params.candidateGroup) cleanParams.candidateGroup = params.candidateGroup;
      if (params.processDefinitionId) cleanParams.processDefinitionId = params.processDefinitionId;
      if (params.processInstanceId) cleanParams.processInstanceId = params.processInstanceId;
      if (params.page !== undefined) cleanParams.page = params.page;
      if (params.size !== undefined) cleanParams.size = params.size;
      if (params.sort) cleanParams.sort = params.sort;
    }

    return httpClient.getPaged<UserTaskDTO>(
      this.getResourceUrl(),
      cleanParams
    );
  }

  /**
   * Get detailed information about a specific user task
   *
   * @param taskId - Unique identifier of the task
   * @returns Promise resolving to user task details
   */
  async getUserTask(taskId: string): Promise<UserTaskDTO> {
    const response = await httpClient.get<UserTaskResponse>(
      this.getResourceUrl(`/${taskId}`)
    );
    return response.data;
  }

  /**
   * Get all user tasks for a specific process instance
   *
   * @param processInstanceId - ID of the process instance
   * @returns Promise resolving to array of user tasks
   */
  async getUserTasksByProcess(processInstanceId: string): Promise<UserTaskDTO[]> {
    const response = await httpClient.get<unknown>(
      this.getResourceUrl(`/process/${processInstanceId}`),
      { params: { size: 100, sort: '-createdAt' } }
    );

    if (Array.isArray(response)) {
      return response as UserTaskDTO[];
    }

    if (response && typeof response === 'object') {
      const body = response as Record<string, unknown>;

      if (Array.isArray(body.content)) {
        return body.content as UserTaskDTO[];
      }

      if (Array.isArray(body.data)) {
        return body.data as UserTaskDTO[];
      }

      if (body.data && typeof body.data === 'object') {
        const nested = body.data as Record<string, unknown>;
        if (Array.isArray(nested.content)) {
          return nested.content as UserTaskDTO[];
        }
      }
    }

    return [];
  }

  /**
   * Claim an unclaimed task for the authenticated user
   * User must be authorized via assignment rules (assignee, candidate user, or candidate group member)
   *
   * @param taskId - ID of the task to claim
   * @returns Promise resolving to updated task details
   */
  async claimTask(taskId: string): Promise<UserTaskDTO> {
    const response = await httpClient.post<UserTaskResponse>(
      this.getResourceUrl(`/${taskId}/claim`)
    );
    return response.data;
  }

  /**
   * Unclaim (release) a claimed task back to the candidate pool
   * Only the claimant can unclaim their task
   *
   * @param taskId - ID of the task to unclaim
   * @returns Promise resolving to updated task details
   */
  async unclaimTask(taskId: string): Promise<UserTaskDTO> {
    const response = await httpClient.post<UserTaskResponse>(
      this.getResourceUrl(`/${taskId}/unclaim`)
    );
    return response.data;
  }

  /**
   * Complete a user task with optional output variables
   * Resumes process execution after task completion
   *
   * @param taskId - ID of the task to complete
   * @param request - Request containing output variables
   * @returns Promise resolving to completed task details
   */
  async completeTask(
    taskId: string,
    request: CompleteTaskRequest
  ): Promise<UserTaskDTO> {
    const response = await httpClient.post<UserTaskResponse, CompleteTaskRequest>(
      this.getResourceUrl(`/${taskId}/complete`),
      request
    );
    return response.data;
  }

  /**
   * Reassign a task to a different user
   * Requires admin privileges
   *
   * @param taskId - ID of the task to reassign
   * @param request - Request containing new assignee
   * @returns Promise resolving to updated task details
   */
  async assignTask(
    taskId: string,
    request: AssignTaskRequest
  ): Promise<UserTaskDTO> {
    const response = await httpClient.patch<UserTaskResponse, AssignTaskRequest>(
      this.getResourceUrl(`/${taskId}/assign`),
      request
    );
    return response.data;
  }
}

/**
 * Singleton instance of UserTaskService
 * Import and use this instance throughout the application
 */
export const userTaskService = new UserTaskService();
