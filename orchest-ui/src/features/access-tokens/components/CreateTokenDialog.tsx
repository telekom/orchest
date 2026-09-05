import { Button } from '@/design-system/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/design-system/components/ui/dialog';
import { Input } from '@/design-system/components/ui/input';
import { Label } from '@/design-system/components/ui/label';
import { Select } from '@/design-system/components/ui/select';
import { Loader2, Plus } from 'lucide-react';
import React, { useCallback, useState } from 'react';
import styles from '../pages/AccessTokensPage/AccessTokensPage.module.css';

interface CreateTokenDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (name: string, ttlInDays?: number) => Promise<void>;
  isCreating: boolean;
}

const TTL_OPTIONS = [
  { value: '7', label: '7 days' },
  { value: '30', label: '30 days' },
  { value: '60', label: '60 days' },
  { value: '90', label: '90 days' },
  { value: '180', label: '180 days' },
  { value: '365', label: '365 days' },
  { value: 'none', label: 'No expiration' },
];

export const CreateTokenDialog: React.FC<CreateTokenDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  isCreating,
}) => {
  const [name, setName] = useState('');
  const [ttl, setTtl] = useState('30');

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    const ttlInDays = ttl === 'none' ? undefined : parseInt(ttl, 10);
    await onSubmit(name.trim(), ttlInDays);
    setName('');
    setTtl('30');
  }, [name, ttl, onSubmit]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className={styles.dialogContent}>
        <DialogHeader>
          <DialogTitle className={styles.dialogTitle}>
            <Plus size={16} className={styles.dialogTitleIcon} />
            Generate New Token
          </DialogTitle>
          <DialogDescription className={styles.dialogDescription}>
            Create an API token for programmatic access. The token will only be shown once.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formField}>
            <Label htmlFor="token-name" className={styles.formLabel}>Token Name</Label>
            <Input
              id="token-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. CI/CD Pipeline, Monitoring"
              maxLength={100}
              autoFocus
              className={styles.formInput}
            />
            <span className={styles.formHint}>{name.length}/100 characters</span>
          </div>

          <div className={styles.formField}>
            <Label className={styles.formLabel}>Expiration</Label>
            <Select
              value={ttl}
              onValueChange={setTtl}
              items={TTL_OPTIONS}
              size="sm"
            />
          </div>

          <DialogFooter className={styles.dialogFooter}>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              disabled={!name.trim() || isCreating}
            >
              {isCreating ? (
                <>
                  <Loader2 size={14} className={styles.spinning} />
                  Generating...
                </>
              ) : (
                'Generate Token'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};