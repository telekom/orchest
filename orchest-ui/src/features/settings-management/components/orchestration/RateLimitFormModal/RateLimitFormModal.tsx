import type { RateLimit } from "@/api/domains";
import { Button } from "@/design-system/components/ui/button";
import { Label } from "@/design-system/components/ui/label";
import { useFormValidation, type ValidationRules } from "@/shared/hooks";
import { Switch } from "@/design-system/components/ui/switch";
import { Input } from "@/design-system/components/ui/input";
import { Activity, Cpu } from "lucide-react";
import React from "react";
import styles from "./RateLimitFormModal.module.css";

interface RateLimitFormModalProps {
  isOpen: boolean;
  formData: RateLimit | null;
  configs: RateLimit[];
  saving: boolean;
  onClose: () => void;
  onSave: () => void;
  onInputChange: (field: keyof RateLimit, value: string | number | boolean) => void;
}

interface FormFieldConfig {
  id: keyof RateLimit;
  label: string;
  type?: 'text' | 'number';
  placeholder: string;
  min?: string;
  max?: string;
}

const FORM_FIELDS: FormFieldConfig[] = [
  {
    id: 'processId',
    label: 'Process Name',
    type: 'text',
    placeholder: 'e.g., order-processing',
  },
  {
    id: 'windowDuration',
    label: 'Window Duration',
    type: 'number',
    placeholder: '30',
    min: '1',
    max: '3600',
  },
  {
    id: 'allowedSize',
    label: 'Allowed Size',
    type: 'number',
    placeholder: '75',
    min: '1',
    max: '100',
  },
];

// Static validation rules - no need for useMemo
const VALIDATION_RULES: ValidationRules<Pick<RateLimit, 'processId' | 'windowDuration' | 'allowedSize'>> = {
  processId: (value: string) => {
    if (!value.trim()) return "Process name is required";
    if (value.length < 2) return "Process name must be at least 2 characters";
    return "";
  },
  windowDuration: (value: number) => {
    if (value < 1) return "Window duration must be at least 1";
    if (value > 3600) return "Window duration cannot exceed 3600";
    return "";
  },
  allowedSize: (value: number) => {
    if (value < 1) return "Allowed size must be at least 1";
    if (value > 100) return "Allowed size cannot exceed 100";
    return "";
  },
};

interface FormFieldProps {
  field: FormFieldConfig;
  value: string | number;
  error?: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const FormField: React.FC<FormFieldProps> = ({ field, value, error, onChange }) => {
  return (
    <div className={styles.fieldGroup}>
      <div className={styles.fieldLabelContainer}>
        <span className={styles.fieldLabel}>
          {field.label} <span className={styles.requiredIndicator}>*</span>
        </span>
      </div>
      <Input
        id={field.id}
        type={field.type || 'text'}
        placeholder={field.placeholder}
        value={String(value || '')}
        onChange={onChange}
        min={field.min}
        max={field.max}
        errorMessage={error}
      />
    </div>
  );
};

const RateLimitFormModal: React.FC<RateLimitFormModalProps> = ({
  isOpen,
  formData,
  configs,
  saving,
  onClose,
  onSave,
  onInputChange,
}) => {
  const isNewConfig = formData && formData.id?.startsWith("temp-") && !configs.find((c) => c.id === formData.id);

  const { errors, validateField, validate, hasErrors } = useFormValidation({
    rules: VALIDATION_RULES,
  });

  const handleSave = () => {
    if (!formData) return;

    const validationErrors = validate({
      processId: formData.processId,
      windowDuration: formData.windowDuration,
      allowedSize: formData.allowedSize,
    });

    if (Object.keys(validationErrors).length === 0) {
      onSave();
    }
  };

  const handleInputChange = (field: keyof RateLimit, isNumber: boolean) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = isNumber ? parseInt(e.target.value) || 0 : e.target.value;
    onInputChange(field, value);
    validateField(field as keyof typeof errors, value);
  };

  const [processIdField, ...numericFields] = FORM_FIELDS;

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <h2 className={styles.modalTitle}>{isNewConfig ? "Add Orchestration Settings" : "Edit Orchestration Settings"}</h2>
        {formData && (
          <div className={styles.formContainer}>
            <div className={styles.statusBanner}>
              <div className={styles.statusContent}>
                <div className={`${styles.statusIcon} ${formData.enabled ? styles.statusIconEnabled : styles.statusIconDisabled}`}>
                  <Activity className={styles.statusIconSvg} />
                </div>
                <div className={styles.statusTextContainer}>
                  <Label htmlFor="enabled" className={styles.statusLabel}>
                    Switch To OrchesT
                  </Label>
                  <p className={styles.statusDescription}>
                    {formData.enabled ? "Currently enabled and enforcing limits" : "Currently disabled"}
                  </p>
                </div>
              </div>
              <Switch
                selected={formData.enabled}
                size="sm"
                label=""
                inputProps={{
                  id: "enabled",
                  name: "enabled",
                  onChange: (e) => onInputChange("enabled", e.target.checked)
                }}
              />
            </div>

            <div className={styles.statusBanner}>
              <div className={styles.statusContent}>
                <div className={`${styles.statusIcon} ${formData.switchToNewCamunda ? styles.statusIconEnabled : styles.statusIconDisabled}`}>
                  <Cpu className={styles.statusIconSvg} />
                </div>
                <div className={styles.statusTextContainer}>
                  <Label htmlFor="switchToNewCamunda" className={styles.statusLabel}>
                    Switch to New Camunda
                  </Label>
                  <p className={styles.statusDescription}>
                    {formData.switchToNewCamunda ? "Using new Camunda engine" : "Using New Camunda engine"}
                  </p>
                </div>
              </div>
              <Switch
                selected={formData.switchToNewCamunda}
                size="sm"
                label=""
                inputProps={{
                  id: "switchToNewCamunda",
                  name: "switchToNewCamunda",
                  onChange: (e) => onInputChange("switchToNewCamunda", e.target.checked)
                }}
              />
            </div>

            <div className={styles.fieldsContainer}>
              <FormField
                field={processIdField}
                value={formData.processId}
                error={errors.processId}
                onChange={handleInputChange('processId', false)}
              />

              <div className={styles.twoColumnGrid}>
                {numericFields.map((field) => (
                  <FormField
                    key={field.id}
                    field={field}
                    value={formData[field.id] as number}
                    error={errors[field.id as keyof typeof errors]}
                    onChange={handleInputChange(field.id, true)}
                  />
                ))}
              </div>
            </div>

            <div className={styles.actions}>
              <Button type="button" variant="outline" size="sm" onClick={onClose}>Cancel</Button>
              <Button
                type="button"
                variant="primary"
                size="sm"
                onClick={handleSave}
                disabled={saving || hasErrors()}
              >
                {saving ? 'Saving...' : isNewConfig ? 'Create' : 'Save'}
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default React.memo(RateLimitFormModal);
