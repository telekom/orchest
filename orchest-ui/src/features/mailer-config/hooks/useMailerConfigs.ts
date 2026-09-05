import { mailerConfigService } from '@/api/domains/mailer-config';
import type { MailerConfigRequest } from '@/api/domains/mailer-config';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { useCallback, useState } from 'react';

const QUERY_KEY = ['mailer-configs'];

export function useMailerConfigs() {
  const [page, setPage] = useState(0);

  const { data, isLoading, error, refetch } = useApiQuery(
    [...QUERY_KEY, page],
    () => mailerConfigService.list(page),
    { staleTime: 30000, showErrorToast: true }
  );

  const createMutation = useApiMutation(
    (req: MailerConfigRequest) => mailerConfigService.create(req),
    { showSuccessToast: true, successMessage: 'Mailer config created', invalidateQueries: [QUERY_KEY] }
  );

  const updateMutation = useApiMutation(
    ({ id, req }: { id: string; req: MailerConfigRequest }) => mailerConfigService.update(id, req),
    { showSuccessToast: true, successMessage: 'Mailer config updated', invalidateQueries: [QUERY_KEY] }
  );

  const deleteMutation = useApiMutation(
    (id: string) => mailerConfigService.delete(id),
    { showSuccessToast: true, successMessage: 'Mailer config deleted', invalidateQueries: [QUERY_KEY] }
  );

  const create = useCallback(
    (req: MailerConfigRequest) => createMutation.mutateAsync(req),
    [createMutation]
  );

  const update = useCallback(
    (id: string, req: MailerConfigRequest) => updateMutation.mutateAsync({ id, req }),
    [updateMutation]
  );

  const remove = useCallback(
    (id: string) => deleteMutation.mutateAsync(id),
    [deleteMutation]
  );

  return {
    configs: data?.content ?? [],
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    page,
    setPage,
    isLoading,
    error,
    refetch,
    create,
    update,
    remove,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}
