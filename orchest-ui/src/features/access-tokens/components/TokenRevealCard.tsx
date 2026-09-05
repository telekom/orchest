import { Button } from '@/design-system/components/ui/button';
import { useCopyToClipboard } from '@/shared/hooks/useCopyToClipboard';
import { AlertTriangle, Check, Copy, X } from 'lucide-react';
import React from 'react';
import type { CreateApiTokenResponse } from '../types/apiTokens';
import styles from '../pages/AccessTokensPage/AccessTokensPage.module.css';

interface TokenRevealCardProps {
  tokenData: CreateApiTokenResponse;
  onDismiss: () => void;
}

export const TokenRevealCard: React.FC<TokenRevealCardProps> = ({ tokenData, onDismiss }) => {
  const { copyToClipboard, isCopied } = useCopyToClipboard({ showToast: false });

  return (
    <div className={styles.revealCard}>
      <div className={styles.revealHeader}>
        <div className={styles.revealWarning}>
          <AlertTriangle size={16} />
          <span>Copy this token now. It won't be shown again.</span>
        </div>
        <button type="button" className={styles.revealDismiss} onClick={onDismiss}>
          <X size={14} />
        </button>
      </div>
      <div className={styles.revealBody}>
        <div className={styles.revealMeta}>
          <span className={styles.revealName}>{tokenData.name}</span>
          <span className={styles.revealExpiry}>
            Expires: {tokenData.expiresAt ? new Date(tokenData.expiresAt).toLocaleDateString() : 'Never'}
          </span>
        </div>
        <div className={styles.revealTokenRow}>
          <code className={styles.revealToken}>{tokenData.token}</code>
          <Button
            variant="outline"
            size="sm"
            className={styles.copyButton}
            onClick={() => copyToClipboard(tokenData.token, tokenData.tokenId)}
          >
            {isCopied(tokenData.tokenId) ? <Check size={14} /> : <Copy size={14} />}
            {isCopied(tokenData.tokenId) ? 'Copied' : 'Copy'}
          </Button>
        </div>
      </div>
    </div>
  );
};
