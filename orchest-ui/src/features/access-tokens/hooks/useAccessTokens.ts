import { apiTokensService } from '@/api/domains/api-tokens';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { useCallback } from 'react';
import type { ApiTokenListResponse, CreateApiTokenRequest } from '../types/apiTokens';

const TOKENS_QUERY_KEY = ['api-tokens'];

export function useAccessTokens() {
  const {
    data,
    isLoading,
    error,
    refetch,
  } = useApiQuery<ApiTokenListResponse>(
    TOKENS_QUERY_KEY,
    () => apiTokensService.listTokens(),
    { staleTime: 30000, showErrorToast: true }
  );

  const createMutation = useApiMutation(
    (request: CreateApiTokenRequest) => apiTokensService.createToken(request),
    {
      showSuccessToast: true,
      successMessage: 'Token created successfully',
      invalidateQueries: [TOKENS_QUERY_KEY],
    }
  );

  const revokeMutation = useApiMutation(
    (tokenId: string) => apiTokensService.revokeToken(tokenId),
    {
      showSuccessToast: true,
      successMessage: 'Token revoked',
      invalidateQueries: [TOKENS_QUERY_KEY],
    }
  );

  const revokeAllMutation = useApiMutation(
    () => apiTokensService.revokeAllTokens(),
    {
      showSuccessToast: true,
      successMessage: 'All tokens revoked',
      invalidateQueries: [TOKENS_QUERY_KEY],
    }
  );

  const createToken = useCallback(
    async (request: CreateApiTokenRequest) => {
      return createMutation.mutateAsync(request);
    },
    [createMutation]
  );

  const revokeToken = useCallback(
    async (tokenId: string) => {
      return revokeMutation.mutateAsync(tokenId);
    },
    [revokeMutation]
  );

  const revokeAllTokens = useCallback(
    async () => {
      return revokeAllMutation.mutateAsync(undefined);
    },
    [revokeAllMutation]
  );

  return {
    tokens: data?.data ?? [],
    isLoading,
    error,
    refetch,
    createToken,
    revokeToken,
    revokeAllTokens,
    isCreating: createMutation.isPending,
    isRevoking: revokeMutation.isPending,
    isRevokingAll: revokeAllMutation.isPending,
  };
}
