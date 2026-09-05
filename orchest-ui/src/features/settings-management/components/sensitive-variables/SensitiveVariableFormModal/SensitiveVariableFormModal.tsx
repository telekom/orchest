import type { SensitiveVariable, SensitiveVariableType } from '@/api/domains/sensitive-variables';
import { Button } from '@/design-system/components/ui/button';
import { Input } from '@/design-system/components/ui/input';
import { Select } from '@/design-system/components/ui/select';
import React, { useCallback, useEffect, useState } from 'react';
import styles from './SensitiveVariableFormModal.module.css';

interface SensitiveVariableFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (variable: SensitiveVariable) => Promise<void>;
  editingVariable: SensitiveVariable | null;
  editingIndex: number | null;
  processDefinitionId: string;
  saving: boolean;
}

interface FormState {
  name: string;
  value: string;
  type: SensitiveVariableType;
}

interface FormErrors {
  name?: string;
}

const TYPE_OPTIONS = [
  { label: 'HIDE', value: 'HIDE' },
  { label: 'SHOW', value: 'SHOW' },
];

const SensitiveVariableFormModal: React.FC<SensitiveVariableFormModalProps> = ({
  isOpen,
  onClose,
  onSave,
  editingVariable,
  processDefinitionId,
  saving,
}) => {
  const isEdit = !!editingVariable;

  const [form, setForm] = useState<FormState>({ name: '', value: '', type: 'HIDE' });
  const [errors, setErrors] = useState<FormErrors>({});

  useEffect(() => {
    if (isOpen) {
      if (editingVariable) {
        setForm({ name: editingVariable.name, value: editingVariable.value ?? '', type: editingVariable.type });
      } else {
        setForm({ name: '', value: '', type: 'HIDE' });
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

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [form]);

  const handleSave = useCallback(async () => {
    if (!validate()) return;

    const variable: SensitiveVariable = {
      name: form.name.trim(),
      type: form.type,
      ...(form.type === 'HIDE' && form.value.trim() ? { value: form.value } : {}),
    };

    await onSave(variable);
  }, [validate, form, onSave]);

  const handleFieldChange = useCallback((field: keyof FormState) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  }, []);

  const handleTypeChange = useCallback((value: string) => {
    setForm((prev) => ({ ...prev, type: value as SensitiveVariableType, value: '' }));
  }, []);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <h2 className={styles.title}>{isEdit ? 'Edit Sensitive Variable' : 'Add Sensitive Variable'}</h2>
        <div className={styles.formContainer}>
          <div className={styles.fieldGroup}>
            <span className={styles.fieldLabel}>
              Variable Name <span className={styles.required}>*</span>
            </span>
            <Input
              placeholder="e.g., API_SECRET_KEY"
              value={form.name}
              onChange={handleFieldChange('name')}
              type="text"
              disabled={isEdit}
              errorMessage={errors.name}
            />
          </div>

          <div className={styles.fieldGroup}>
            <span className={styles.fieldLabel}>Type</span>
            <Select
              label=""
              value={form.type}
              items={TYPE_OPTIONS}
              onValueChange={handleTypeChange}
              size="sm"
            />
            <span className={styles.typeHint}>
              {form.type === 'SHOW' ? 'Variable value will be visible in process instances' : 'Variable value will be hidden in process instances'}
            </span>
          </div>

          {form.type === 'HIDE' && (
            <div className={styles.fieldGroup}>
              <span className={styles.fieldLabel}>Value</span>
              <Input
                placeholder="Enter override value (optional)"
                value={form.value}
                onChange={handleFieldChange('value')}
                type="password"
              />
            </div>
          )}

          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Process:</span>
            <span className={styles.infoValue}>{processDefinitionId}</span>
          </div>

          <div className={styles.actions}>
            <Button type="button" variant="outline" size="sm" onClick={onClose}>Cancel</Button>
            <Button
              type="button"
              variant="primary"
              size="sm"
              onClick={handleSave}
              disabled={saving || !form.name.trim()}
            >
              {saving ? 'Saving...' : isEdit ? 'Update' : 'Create'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(SensitiveVariableFormModal);