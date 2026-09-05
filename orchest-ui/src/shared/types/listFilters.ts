export interface ProcessFilters {
  process: string | null;
  version: string | null;
  searchText: string;
  status: string | null;
  from: string | null;
  to: string | null;
  timezone?: string | null;
  [key: string]: string | null | undefined;
}
