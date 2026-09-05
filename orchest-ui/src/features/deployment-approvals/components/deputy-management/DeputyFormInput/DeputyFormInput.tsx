import { Input } from '@/design-system/components/ui/input';
import { Button } from '@/design-system/components/ui/button';
import { isValidEmail } from '@/shared/utils';
import { Loader2, Plus } from 'lucide-react';
import React, { useMemo } from 'react';
import styles from './DeputyFormInput.module.css';

interface DeputyFormInputProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  isSubmitting: boolean;
}

export const DeputyFormInput: React.FC<DeputyFormInputProps> = ({
  value,
  onChange,
  onSubmit,
  isSubmitting,
}) => {
  const isInvalid = useMemo(() => {
    const trimmed = value.trim();
    return trimmed.length > 0 && !isValidEmail(trimmed);
  }, [value]);

  const canSubmit = useMemo(() => {
    const trimmed = value.trim();
    return trimmed.length > 0 && isValidEmail(trimmed) && !isSubmitting;
  }, [value, isSubmitting]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && canSubmit) {
      e.preventDefault();
      onSubmit();
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.inputWrapper}>
        <Input
          type="email"
          placeholder="Enter approver email address…"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={isSubmitting}
          aria-label="Approver email address"
          errorMessage={isInvalid ? 'Please enter a valid email address' : undefined}
        />
      </div>
      <Button
        type="button"
        variant="primary"
        size="sm"
        onClick={onSubmit}
        disabled={!canSubmit}
        aria-label="Add approver"
        className={styles.addButton}
      >
        {isSubmitting ? (
          <Loader2 size={14} className={styles.spinner} aria-hidden />
        ) : (
          <Plus size={14} aria-hidden />
        )}
        Add
      </Button>
    </div>
  );
};
