import { Button } from '@/design-system/components/ui/button';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import AppLayout from '@/shared/layouts/AppLayout';
import { format } from 'date-fns';
import { KeyRound, Plus, RefreshCw, ShieldOff, Trash2 } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import { CreateTokenDialog } from '../../components/CreateTokenDialog';
import { TokenRevealCard } from '../../components/TokenRevealCard';
import { useAccessTokens } from '../../hooks/useAccessTokens';
import type { ApiToken, CreateApiTokenResponse } from '../../types/apiTokens';
import styles from './AccessTokensPage.module.css';

const AccessTokensPage: React.FC = () => {
  const {
    tokens,
    isLoading,
    refetch,
    createToken,
    revokeToken,
    revokeAllTokens,
    isCreating,
    isRevoking,
    isRevokingAll,
  } = useAccessTokens();

  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showRevokeAllDialog, setShowRevokeAllDialog] = useState(false);
  const [revokeTargetId, setRevokeTargetId] = useState<string | null>(null);
  const [newToken, setNewToken] = useState<CreateApiTokenResponse | null>(null);

  const handleCreate = useCallback(async (name: string, ttlInDays?: number) => {
    const result = await createToken({ name, ttlInDays });
    setNewToken(result.data);
    setShowCreateDialog(false);
  }, [createToken]);

  const handleRevoke = useCallback(async () => {
    if (revokeTargetId) {
      await revokeToken(revokeTargetId);
      setRevokeTargetId(null);
    }
  }, [revokeToken, revokeTargetId]);

  const handleRevokeAll = useCallback(async () => {
    await revokeAllTokens();
    setShowRevokeAllDialog(false);
  }, [revokeAllTokens]);

  const activeTokens = useMemo(() => tokens.filter((t) => !t.revoked), [tokens]);
  const revokedTokens = useMemo(() => tokens.filter((t) => t.revoked), [tokens]);

  const columns = useMemo(() => [
    {
      key: 'name',
      header: 'Name',
      render: (row: ApiToken) => (
        <div className={styles.nameCell}>
          <KeyRound size={14} className={styles.tokenIcon} />
          <span className={styles.tokenName}>{row.name}</span>
        </div>
      ),
    },
    {
      key: 'tokenPrefix',
      header: 'Token',
      render: (row: ApiToken) => (
        <code className={styles.tokenPrefix}>{row.tokenPrefix}••••••••</code>
      ),
    },
    {
      key: 'roles',
      header: 'Roles',
      render: (row: ApiToken) => (
        <div className={styles.rolesCell}>
          {row.roles?.map((role) => (
            <span key={role} className={styles.roleBadge}>{role}</span>
          ))}
        </div>
      ),
    },
    {
      key: 'createdAt',
      header: 'Created',
      render: (row: ApiToken) => (
        <span className={styles.dateCell}>
          {row.createdAt ? format(new Date(row.createdAt), 'MMM dd, yyyy') : '—'}
        </span>
      ),
    },
    {
      key: 'expiresAt',
      header: 'Expires',
      render: (row: ApiToken) => {
        if (!row.expiresAt) return <span className={styles.neverBadge}>Never</span>;
        const isExpired = new Date(row.expiresAt) < new Date();
        return (
          <span className={isExpired ? styles.expiredDate : styles.dateCell}>
            {format(new Date(row.expiresAt), 'MMM dd, yyyy')}
          </span>
        );
      },
    },
    {
      key: 'lastUsedAt',
      header: 'Last Used',
      render: (row: ApiToken) => (
        <span className={styles.dateCell}>
          {row.lastUsedAt ? format(new Date(row.lastUsedAt), 'MMM dd, yyyy HH:mm') : 'Never'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: '',
      render: (row: ApiToken) => (
        <div className={styles.actionsCell}>
          {!row.revoked && (
            <button
              type="button"
              className={styles.revokeButton}
              onClick={() => setRevokeTargetId(row.tokenId)}
              disabled={isRevoking}
              title="Revoke token"
            >
              <Trash2 size={14} />
            </button>
          )}
          {row.revoked && (
            <span className={styles.revokedBadge}>
              <ShieldOff size={12} />
              Revoked
            </span>
          )}
        </div>
      ),
      align: 'right' as const,
    },
  ], [isRevoking]);

  return (
    <AppLayout>
      <div className={styles.page}>
        <div className={styles.header}>
          <div className={styles.titleRow}>
            <div className={styles.titleIconWrap}>
              <KeyRound className={styles.titleIcon} />
            </div>
            <div>
              <h1 className={styles.title}>Access Tokens</h1>
              <p className={styles.subtitle}>Manage API tokens for programmatic access</p>
            </div>
          </div>
          <div className={styles.headerActions}>
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              disabled={isLoading}
              className={styles.actionButton}
            >
              <RefreshCw size={14} className={isLoading ? styles.spinning : ''} />
              Refresh
            </Button>
            {activeTokens.length > 0 && (
              <Button
                variant="destructive"
                size="sm"
                onClick={() => setShowRevokeAllDialog(true)}
                disabled={isRevokingAll}
                className={styles.actionButton}
              >
                <ShieldOff size={14} />
                Revoke All
              </Button>
            )}
            <Button
              variant="primary"
              size="sm"
              onClick={() => setShowCreateDialog(true)}
              className={styles.createButton}
            >
              <Plus size={14} />
              New Token
            </Button>
          </div>
        </div>

        {newToken && (
          <TokenRevealCard
            tokenData={newToken}
            onDismiss={() => setNewToken(null)}
          />
        )}

        <div className={styles.statsRow}>
          <div className={styles.statCard}>
            <span className={styles.statValue}>{activeTokens.length}</span>
            <span className={styles.statLabel}>Active</span>
          </div>
          <div className={styles.statCard}>
            <span className={styles.statValue}>{revokedTokens.length}</span>
            <span className={styles.statLabel}>Revoked</span>
          </div>
          <div className={styles.statCard}>
            <span className={styles.statValue}>{tokens.length}</span>
            <span className={styles.statLabel}>Total</span>
          </div>
        </div>

        <div className={styles.tableWrap}>
          <DataTable<ApiToken>
            data={tokens}
            columns={columns}
            keyExtractor={(row) => row.tokenId}
            loading={isLoading}
            loadingText="Loading tokens..."
            emptyText="No API tokens found. Create one to get started."
            showCard={true}
            stickyHeader={true}
          />
        </div>

        <CreateTokenDialog
          open={showCreateDialog}
          onOpenChange={setShowCreateDialog}
          onSubmit={handleCreate}
          isCreating={isCreating}
        />

        <ConfirmationDialog
          open={!!revokeTargetId}
          onOpenChange={(open) => { if (!open) setRevokeTargetId(null); }}
          title="Revoke Token"
          description="This token will be immediately invalidated. Any applications using it will lose access. This action cannot be undone."
          confirmText="Revoke"
          confirmVariant="destructive"
          onConfirm={handleRevoke}
        />

        <ConfirmationDialog
          open={showRevokeAllDialog}
          onOpenChange={setShowRevokeAllDialog}
          title="Revoke All Tokens"
          description="All active tokens will be immediately invalidated. Every application using your tokens will lose access. This action cannot be undone."
          confirmText="Revoke All"
          confirmVariant="destructive"
          onConfirm={handleRevokeAll}
        />
      </div>
    </AppLayout>
  );
};

export default React.memo(AccessTokensPage);