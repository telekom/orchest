import type { AlertingMailerConfig, MailerConfigRequest } from '@/api/domains/mailer-config';
import { Button } from '@/design-system/components/ui/button';
import { Combobox } from '@/design-system/components/ui/combobox';
import { processDefinitionService } from '@/api/domains';
import { useApiQuery } from '@/shared/hooks';
import { X } from 'lucide-react';
import React, { useEffect, useMemo, useState } from 'react';
import { EmailTagInput } from './EmailTagInput';
import styles from './MailerConfigFormModal.module.css';

interface Props {
  open: boolean;
  onClose: () => void;
  onSubmit: (req: MailerConfigRequest) => Promise<void>;
  initial?: AlertingMailerConfig | null;
  isSubmitting: boolean;
}

export const MailerConfigFormModal: React.FC<Props> = ({ open, onClose, onSubmit, initial, isSubmitting }) => {
  const [selectedProcessIds, setSelectedProcessIds] = useState<string[]>([]);
  const [to, setTo] = useState<string[]>([]);
  const [cc, setCc] = useState<string[]>([]);
  const [bcc, setBcc] = useState<string[]>([]);

  const isEditMode = !!initial;

  const { data: processes, isLoading: processesLoading } = useApiQuery(
    ['process-definitions-list'],
    () => processDefinitionService.listProcessDefinitionIds(),
    { staleTime: 60000 }
  );

  const processOptions = useMemo(() =>
    (processes ?? [])
      .filter(p => !selectedProcessIds.includes(p.definitionId))
      .map(p => ({ value: p.definitionId, label: p.definitionId })),
    [processes, selectedProcessIds]
  );

  useEffect(() => {
    if (initial) {
      setSelectedProcessIds([initial.processId]);
      setTo(initial.alertingRecipient.to);
      setCc(initial.alertingRecipient.cc);
      setBcc(initial.alertingRecipient.bcc);
    } else {
      setSelectedProcessIds([]);
      setTo([]);
      setCc([]);
      setBcc([]);
    }
  }, [initial, open]);

  if (!open) return null;

  const handleAddProcess = (value: string) => {
    if (value && !selectedProcessIds.includes(value)) {
      setSelectedProcessIds(prev => [...prev, value]);
    }
  };

  const handleRemoveProcess = (value: string) => {
    setSelectedProcessIds(prev => prev.filter(id => id !== value));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (to.length === 0 || selectedProcessIds.length === 0) return;

    for (const processId of selectedProcessIds) {
      await onSubmit({ processId, alertingRecipient: { to, cc, bcc } });
    }
    onClose();
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <h2 className={styles.title}>{isEditMode ? 'Edit' : 'Create'} Mailer Config</h2>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <span className={styles.label}>
              {isEditMode ? 'Process' : 'Processes'}
            </span>
            {!isEditMode && selectedProcessIds.length > 0 && (
              <div className={styles.chipList}>
                {selectedProcessIds.map(id => (
                  <span key={id} className={styles.chip}>
                    {id}
                    <button type="button" className={styles.chipRemove} onClick={() => handleRemoveProcess(id)}>
                      <X size={12} />
                    </button>
                  </span>
                ))}
              </div>
            )}
            <Combobox
              items={processOptions}
              value={isEditMode ? selectedProcessIds[0] : undefined}
              onSelect={(v) => isEditMode ? setSelectedProcessIds(v ? [v] : []) : handleAddProcess(v)}
              placeholder={isEditMode ? 'Select a process...' : 'Add a process...'}
              searchPlaceholder="Search process..."
              emptyMessage="No processes found"
              disabled={isEditMode}
              loading={processesLoading}
              filterOptions
            />
          </div>

          <div className={styles.field}>
            <span className={styles.label}>To *</span>
            <EmailTagInput emails={to} onChange={setTo} placeholder="Add recipient email..." required />
          </div>

          <div className={styles.field}>
            <span className={styles.label}>CC</span>
            <EmailTagInput emails={cc} onChange={setCc} placeholder="Add CC email..." />
          </div>

          <div className={styles.field}>
            <span className={styles.label}>BCC</span>
            <EmailTagInput emails={bcc} onChange={setBcc} placeholder="Add BCC email..." />
          </div>

          <div className={styles.actions}>
            <Button type="button" variant="outline" size="sm" onClick={onClose}>Cancel</Button>
            <Button type="submit" variant="primary" size="sm" disabled={isSubmitting || selectedProcessIds.length === 0 || to.length === 0}>
              {isSubmitting ? 'Saving...' : isEditMode ? 'Update' : `Create${selectedProcessIds.length > 1 ? ` (${selectedProcessIds.length})` : ''}`}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
