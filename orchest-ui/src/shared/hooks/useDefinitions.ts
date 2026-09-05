import { useApiQuery } from "./useApiQuery";

interface UseDefinitionsConfig<T> {
  queryKey: readonly unknown[];
  fetchFn: () => Promise<{ content: T[] } | T[]>;
  showErrorToast?: boolean;
  enabled?: boolean;
}

interface UseDefinitionsReturn<T> {
  definitions: T[];
  isLoading: boolean;
  refetch: () => void;
  error: unknown;
}

export function useDefinitions<T>({
  queryKey,
  fetchFn,
  showErrorToast = false,
  enabled = true,
}: UseDefinitionsConfig<T>): UseDefinitionsReturn<T> {
  const { data, isLoading, refetch, error } = useApiQuery(
    queryKey,
    async () => {
      const result = await fetchFn();
      // Normalize response - handle both { content: [...] } and [...] formats
      return Array.isArray(result) ? { content: result } : result;
    },
    {
      showErrorToast,
      enabled,
    }
  );

  return {
    definitions: data?.content || [],
    isLoading,
    refetch,
    error,
  };
}
