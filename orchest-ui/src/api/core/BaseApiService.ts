import { httpClient } from './httpClient'
import { ApiRequestConfig, PagedResponse } from './httpClient'

export abstract class BaseApiService<TEntity, TQueryParams = void, TCreateDTO = Partial<TEntity>, TUpdateDTO = Partial<TEntity>> {
  protected abstract readonly baseUrl: string

  protected abstract readonly resourcePath: string

  protected getResourceUrl(path = ''): string {
    return `${this.baseUrl}/${this.resourcePath}${path}`
  }

  async getAll(
    params?: TQueryParams,
    config?: ApiRequestConfig
  ): Promise<PagedResponse<TEntity>> {
    return httpClient.getPaged<TEntity>(
      this.getResourceUrl(),
      params as Record<string, unknown>,
      config
    )
  }

  async getById(id: string | number, config?: ApiRequestConfig): Promise<TEntity> {
    return httpClient.get<TEntity>(this.getResourceUrl(`/${id}`), config)
  }

  async create(data: TCreateDTO, config?: ApiRequestConfig): Promise<TEntity> {
    return httpClient.post<TEntity, TCreateDTO>(this.getResourceUrl(), data, config)
  }

  async update(
    id: string | number,
    data: TUpdateDTO,
    config?: ApiRequestConfig
  ): Promise<TEntity> {
    return httpClient.put<TEntity, TUpdateDTO>(this.getResourceUrl(`/${id}`), data, config)
  }

  async patch(
    id: string | number,
    data: Partial<TUpdateDTO>,
    config?: ApiRequestConfig
  ): Promise<TEntity> {
    return httpClient.patch<TEntity, Partial<TUpdateDTO>>(
      this.getResourceUrl(`/${id}`),
      data,
      config
    )
  }

  async delete(id: string | number, config?: ApiRequestConfig): Promise<void> {
    return httpClient.delete<void>(this.getResourceUrl(`/${id}`), config)
  }

  async batchDelete(ids: (string | number)[], config?: ApiRequestConfig): Promise<void> {
    return httpClient.post<void, { ids: (string | number)[] }>(
      this.getResourceUrl('/batch-delete'),
      { ids },
      config
    )
  }

  async exists(id: string | number, config?: ApiRequestConfig): Promise<boolean> {
    try {
      await this.getById(id, config)
      return true
    } catch {
      return false
    }
  }

  async count(params?: Partial<TQueryParams>, config?: ApiRequestConfig): Promise<number> {
    const response = await this.getAll(params as TQueryParams, config)
    return response.page.totalElements
  }
}

export abstract class BaseDefinitionService<TEntity, TQueryParams = void> extends BaseApiService<
  TEntity,
  TQueryParams
> {
  // uploadDefinition has been removed - use deploymentApprovalsService.requestApproval instead

  async getByIdAndVersion(
    id: string,
    version: number,
    config?: ApiRequestConfig
  ): Promise<TEntity> {
    return httpClient.get<TEntity>(this.getResourceUrl(`/${id}/${version}`), config)
  }

  async getVersions(id: string, config?: ApiRequestConfig): Promise<TEntity[]> {
    return httpClient.get<TEntity[]>(this.getResourceUrl(`/${id}/versions`), config)
  }

  async getLatest(id: string, config?: ApiRequestConfig): Promise<TEntity> {
    return httpClient.get<TEntity>(this.getResourceUrl(`/${id}/latest`), config)
  }
}
