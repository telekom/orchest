import { httpClient } from "@/api/core";
import { BaseApiService } from "@/api/core/BaseApiService";
import { toISODateTime } from "@/api/core/utils/dateUtils";
import {
    BatchCancelRequest,
    BatchCompensateInstanceRequest,
    BatchRetryRequest,
    CancelInstanceRequest,
    CompensateInstanceRequest,
    CompensateInstanceResponse,
    ProcessInstanceDTO,
    ProcessInstanceQueryParams,
    ProcessInstanceResponse,
    ProcessInstanceScrollDTO,
    ProcessInstanceScrollParams,
    ProcessInstancesResponse,
    ProcessInstanceStats,
    ProcessInvocationAPIResponse,
    ProcessInvocationRequest,
    ProcessInvocationResponse,
    RetryProcessEvent,
    UpdateInstanceRequest,
} from "@/api/types/orchest-api";

export class ProcessInstanceService extends BaseApiService<
  ProcessInstanceDTO,
  ProcessInstanceQueryParams
> {
  protected readonly baseUrl = "/orchest";
  protected readonly resourcePath = "processInstances";

  async createProcessInstance(
    request: ProcessInvocationRequest
  ): Promise<ProcessInvocationResponse> {
    const response = await httpClient.post<ProcessInvocationAPIResponse>(
      this.getResourceUrl('/create'),
      request
    );
    return response.data;
  }

  async getProcessInstances(
    params?: ProcessInstanceQueryParams
  ): Promise<ProcessInstancesResponse> {
    const cleanParams: Record<string, unknown> = {};

    const page = params?.page ?? 0;
    const size = params?.size ?? 50;

    if (params) {
      if (params.processDefinitionId)
        cleanParams.processDefinitionId = params.processDefinitionId;
      if (params.version !== undefined) cleanParams.version = params.version;
      if (params.state && typeof params.state === 'string')
        cleanParams.state = params.state.toUpperCase();
      if (params.searchText && params.searchText.trim() !== "")
        cleanParams.searchText = params.searchText;
      if (params.from) cleanParams.createdFrom = toISODateTime(params.from, params.timezone);
      if (params.to) cleanParams.createdTo = toISODateTime(params.to, params.timezone);

      cleanParams.from = page * size;
      cleanParams.to = (page + 1) * size;

      if (params.sort) cleanParams.sort = params.sort;
      else cleanParams.sort = '-createdAt';
    }

    const scrollResponse = await httpClient.get<ProcessInstanceScrollDTO>(
      this.getResourceUrl('/scroll'),
      { params: cleanParams }
    );

    const content = scrollResponse.content ?? [];
    // The scroll API has no totalElements — synthesise page metadata from hasNext
    // so that the generic useInfiniteInstancesList hook can drive pagination correctly.
    const totalPages = scrollResponse.hasNext ? page + 2 : page + 1;
    const totalElements = scrollResponse.hasNext
      ? (page + 2) * size
      : page * size + content.length;

    return {
      content,
      page: {
        totalPages,
        totalElements,
        number: page,
        size,
      },
    };
  }

  async scrollProcessInstances(
    params: ProcessInstanceScrollParams
  ): Promise<ProcessInstanceScrollDTO> {
    const cleanParams: Record<string, unknown> = {
      from: params.from ?? 0,
      to: params.to,
      sort: params.sort,
    };

    if (params.processDefinitionId) cleanParams.processDefinitionId = params.processDefinitionId;
    if (params.version !== undefined) cleanParams.version = params.version;
    if (params.state) cleanParams.state = params.state.toUpperCase();
    if (params.searchText && params.searchText.trim() !== '') cleanParams.searchText = params.searchText;
    if (params.createdFrom) cleanParams.createdFrom = toISODateTime(params.createdFrom, params.timezone);
    if (params.createdTo) cleanParams.createdTo = toISODateTime(params.createdTo, params.timezone);

    return httpClient.get<ProcessInstanceScrollDTO>(
      this.getResourceUrl('/scroll'),
      { params: cleanParams }
    );
  }

  async getProcessInstancesStats(): Promise<ProcessInstanceStats> {
    return httpClient.get<ProcessInstanceStats>(
      this.getResourceUrl('/stats')
    );
  }

  async getProcessInstance(
    processInstanceId: string
  ): Promise<ProcessInstanceDTO> {
    const response = await httpClient.get<ProcessInstanceResponse>(
      this.getResourceUrl(`/id/${processInstanceId}`)
    );
    return response.data;
  }

  async retryInstance(request: RetryProcessEvent): Promise<string> {
    return httpClient.post<string>(
      this.getResourceUrl('/retry'),
      request
    );
  }

  async modifyInstance(request: UpdateInstanceRequest): Promise<string> {
    return httpClient.patch<string>(
      this.getResourceUrl('/modifyInstance'),
      request
    );
  }

  async cancelInstance(request: CancelInstanceRequest): Promise<string> {
    return httpClient.patch<string>(
      this.getResourceUrl('/cancelInstances'),
      request
    );
  }

  async retryBatch(request: BatchRetryRequest): Promise<string> {
    return httpClient.post<string>(
      this.getResourceUrl('/retryBatch'),
      request
    );
  }

  async cancelBatch(request: BatchCancelRequest): Promise<string> {
    return httpClient.post<string>(
      this.getResourceUrl('/cancelBatch'),
      request
    );
  }

  async compensate(
    request: CompensateInstanceRequest
  ): Promise<CompensateInstanceResponse> {
    return httpClient.post<CompensateInstanceResponse>(
      this.getResourceUrl('/compensate'),
      request
    );
  }

  async compensateBatch(
    request: BatchCompensateInstanceRequest
  ): Promise<string> {
    return httpClient.post<string>(
      this.getResourceUrl('/compensateBatch'),
      request
    );
  }
}

export const processInstanceService = new ProcessInstanceService();
