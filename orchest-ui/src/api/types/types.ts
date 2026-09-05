
export interface Meta {
  code: number;
  message: string;
}

export interface ResponseDTO<T = unknown> {
  meta: Meta;
  data: T;
}

export interface ApiResponse<T = unknown> {
  data: T;
  message?: string;
  success: boolean;
  timestamp: string;
}

export interface PagedResponse<T = unknown> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first?: boolean;
    last?: boolean;
    numberOfElements?: number;
    empty?: boolean;
    sort?: SortObject;
    pageable?: PageableObject;
  };
}

export interface SortObject {
  empty: boolean;
  unsorted: boolean;
  sorted: boolean;
}

export interface PageableObject {
  sort: SortObject;
  offset: number;
  unpaged: boolean;
  pageSize: number;
  paged: boolean;
  pageNumber: number;
}

export type ErrorResponse = ApiResponse<{ error: string; details?: string }>;
export type StringResponse = ApiResponse<string>;
export type BooleanResponse = ApiResponse<boolean>;
export type NumberResponse = ApiResponse<number>;
export type GenericObjectResponse = ApiResponse<Record<string, unknown>>;
export type GenericArrayResponse<T = unknown> = ApiResponse<T[]>;

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

export type BaseListResponse<T> = PagedResponse<T>;
