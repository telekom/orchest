import type { AlertingMailerConfig } from '@/api/domains/mailer-config';
import { Button } from '@/design-system/components/ui/button';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import { format } from 'date-fns';
import { ChevronLeft, ChevronRight, Pencil, Plus, RefreshCw, Trash2 } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import { useMailerConfigs } from '@/features/mailer-config/hooks/useMailerConfigs';
import { MailerConfigFormModal } from '@/features/mailer-config/components/MailerConfigFormModal';
import styles from './AlertMailerPanel.module.css';

const AlertMailerPanel: React.FC = () => {
  const {
    configs, totalPages, page, setPage,
    isLoading, refetch, create, update, remove,
    isCreating, isUpdating, isDeleting,
  } = useMailerConfigs();

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<AlertingMailerConfig | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);

  const handleCreate = useCallback(async (req: Parameters<typeof create>[0]) => {
    await create(req);
  }, [create]);

  const handleUpdate = useCallback(async (req: Parameters<typeof create>[0]) => {
    if (editing) {
      await update(editing.id, req);
      setEditing(null);
    }
  }, [update, editing]);

  const handleDelete = useCallback(async () => {
    if (deleteTarget) {
      await remove(deleteTarget);
      setDeleteTarget(null);
    }
  }, [remove, deleteTarget]);

  const columns = useMemo(() => [
    {
      key: 'processId',
      header: 'Process ID',
      render: (row: AlertingMailerConfig) => <code className={styles.processId}>{row.processId}</code>,
    },
    {
      key: 'to',
      header: 'To',
      render: (row: AlertingMailerConfig) => (
        <span className={styles.emails}>{row.alertingRecipient.to.join(', ') || '—'}</span>
      ),
    },
    {
      key: 'cc',
      header: 'CC',
      render: (row: AlertingMailerConfig) => (
        <span className={styles.emails}>{row.alertingRecipient.cc.join(', ') || '—'}</span>
      ),
    },
    {
      key: 'bcc',
      header: 'BCC',
      render: (row: AlertingMailerConfig) => (
        <span className={styles.emails}>{row.alertingRecipient.bcc.join(', ') || '—'}</span>
      ),
    },
    {
      key: 'lastModifiedAt',
      header: 'Last Modified',
      render: (row: AlertingMailerConfig) => (
        <span>{row.lastModifiedAt ? format(new Date(row.lastModifiedAt), 'MMM dd, yyyy HH:mm') : '—'}</span>
      ),
    },
    {
      key: 'actions',
      header: '',
      render: (row: AlertingMailerConfig) => (
        <div className={styles.actionsCell}>
          <button type="button" className={styles.iconBtn} onClick={() => setEditing(row)} title="Edit">
            <Pencil size={14} />
          </button>
          <button type="button" className={styles.iconBtn} onClick={() => setDeleteTarget(row.id)} title="Delete">
            <Trash2 size={14} />
          </button>
        </div>
      ),
      align: 'right' as const,
    },
  ], []);

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Alert Mailer Settings</h2>
          <p className={styles.subtitle}>Configure email recipients for process alert notifications</p>
        </div>
        <div className={styles.headerActions}>
          <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isLoading}>
            <RefreshCw size={14} className={isLoading ? styles.spinning : ''} />
            Refresh
          </Button>
          <Button variant="primary" size="sm" onClick={() => setFormOpen(true)}>
            <Plus size={14} />
            New Config
          </Button>
        </div>
      </div>

      <DataTable<AlertingMailerConfig>
        data={configs}
        columns={columns}
        keyExtractor={(row) => row.id}
        loading={isLoading}
        loadingText="Loading mailer configs..."
        emptyText="No mailer configs found. Create one to get started."
        showCard={true}
        stickyHeader={true}
      />

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
            <ChevronLeft size={14} /> Prev
          </Button>
          <span className={styles.pageInfo}>Page {page + 1} of {totalPages}</span>
          <Button variant="outline" size="sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>
            Next <ChevronRight size={14} />
          </Button>
        </div>
      )}

      <MailerConfigFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
        isSubmitting={isCreating}
      />

      <MailerConfigFormModal
        open={!!editing}
        onClose={() => setEditing(null)}
        onSubmit={handleUpdate}
        initial={editing}
        isSubmitting={isUpdating}
      />

      <ConfirmationDialog
        open={!!deleteTarget}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Delete Mailer Config"
        description="This will permanently remove the alert email configuration for this process. This action cannot be undone."
        confirmText="Delete"
        confirmVariant="destructive"
        onConfirm={handleDelete}
      />
    </div>
  );
};

export default React.memo(AlertMailerPanel);
