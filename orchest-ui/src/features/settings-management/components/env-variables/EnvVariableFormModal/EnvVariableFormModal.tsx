import type { EnvVariableType, ProcessEnvVariable } from '@/api/domains/process-env-variables';
import { Button } from '@/design-system/components/ui/button';
import { Input } from '@/design-system/components/ui/input';
import React, { useCallback, useEffect, useState } from 'react';
import styles from './EnvVariableFormModal.module.css';

interface EnvVariableFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (variable: ProcessEnvVariable) => Promise<void>;
  editingVariable: ProcessEnvVariable | null;
  processDefinitionId: string;
  variableType: EnvVariableType;
  saving: boolean;
}

interface FormState {
  name: string;
  value: string;
}

interface FormErrors {
  name?: string;
  value?: string;
}

const EnvVariableFormModal: React.FC<EnvVariableFormModalProps> = ({
  isOpen,
  onClose,
  onSave,
  editingVariable,
  processDefinitionId,
  variableType,
  saving,
}) => {
  const isEdit = !!editingVariable;

  const [form, setForm] = useState<FormState>({ name: '', value: '' });
  const [errors, setErrors] = useState<FormErrors>({});

  useEffect(() => {
    if (isOpen) {
      if (editingVariable) {
        setForm({ name: editingVariable.name, value: editingVariable.value });
      } else {
        setForm({ name: '', value: '' });
      }
      setErrors({});
    }
  }, [isOpen, editingVariable]);

  const validate = useCallback((): boolean => {
    const newErrors: FormErrors = {};
    if (!form.name.trim()) {
      newErrors.name = 'Variable name is required';
    } else if (form.name.length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    } else if (!/^[a-zA-Z_][a-zA-Z0-9_.-]*$/.test(form.name)) {
      newErrors.name = 'Name must start with a letter or underscore, and contain only letters, numbers, underscores, dots, or hyphens';
    }

    if (!form.value.trim()) {
      newErrors.value = 'Value is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [form]);

  const handleSave = useCallback(async () => {
    if (!validate()) return;

    const variable: ProcessEnvVariable = {
      ...(editingVariable?.id ? { id: editingVariable.id } : {}),
      processDefinitionId,
      name: form.name.trim(),
      value: form.value,
      type: variableType,
    };

    await onSave(variable);
  }, [validate, editingVariable, processDefinitionId, form, variableType, onSave]);

  const handleFieldChange = useCallback((field: keyof FormState) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  }, []);

  const typeLabel = variableType === 'SECRET' ? 'Sensitive' : 'Environment';

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <h2 className={styles.title}>{isEdit ? `Edit ${typeLabel} Variable` : `Add ${typeLabel} Variable`}</h2>
        <div className={styles.formContainer}>
          <div className={styles.fieldGroup}>
            <span className={styles.fieldLabel}>
              Variable Name <span className={styles.required}>*</span>
            </span>
            <Input
              placeholder="e.g., DATABASE_URL"
              value={form.name}
              onChange={handleFieldChange('name')}
              type="text"
              disabled={isEdit}
              errorMessage={errors.name}
            />
          </div>

          <div className={styles.fieldGroup}>
            <span className={styles.fieldLabel}>
              Value <span className={styles.required}>*</span>
            </span>
            <Input
              placeholder={variableType === 'SECRET' ? '••••••••' : 'Enter value'}
              value={form.value}
              onChange={handleFieldChange('value')}
              type={variableType === 'SECRET' ? 'password' : 'text'}
              errorMessage={errors.value}
            />
          </div>

          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Process:</span>
            <span className={styles.infoValue}>{processDefinitionId}</span>
          </div>

          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Type:</span>
            <span className={styles.infoValue}>{variableType}</span>
          </div>

          <div className={styles.actions}>
            <Button type="button" variant="outline" size="sm" onClick={onClose}>Cancel</Button>
            <Button
              type="button"
              variant="primary"
              size="sm"
              onClick={handleSave}
              disabled={saving || !form.name.trim() || !form.value.trim()}
            >
              {saving ? 'Saving...' : isEdit ? 'Update' : 'Create'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(EnvVariableFormModal);
