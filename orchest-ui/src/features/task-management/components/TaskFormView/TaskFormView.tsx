import React from 'react';
import { Input } from '@/design-system/components/ui/input';
import { FileJson } from 'lucide-react';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { getBadgeStyleForVariableType } from '@/shared/utils/badgeUtils';
import styles from './TaskFormView.module.css';

interface TaskFormViewProps {
  variables: Record<string, unknown>;
  onChange: (variables: Record<string, unknown>) => void;
  readOnly?: boolean;
}

const convertToOriginalType = (value: string, originalValue: unknown): unknown => {
  if (typeof originalValue === 'number') {
    const num = parseFloat(value);
    return isNaN(num) ? value : num;
  }
  if (typeof originalValue === 'boolean') {
    return value.toLowerCase() === 'true';
  }
  return value;
};

export const TaskFormView: React.FC<TaskFormViewProps> = ({
  variables,
  onChange,
  readOnly = false,
}) => {
  const handleInputChange = (key: string, inputValue: string) => {
    onChange({
      ...variables,
      [key]: convertToOriginalType(inputValue, variables[key]),
    });
  };

  const variableEntries = Object.entries(variables);

  if (variableEntries.length === 0) {
    return (
      <div className={styles.emptyState}>
        <FileJson className={styles.emptyIcon} />
        <p className={styles.emptyText}>No variables to display</p>
        <p className={styles.emptyDescription}>
          This task has no input variables
        </p>
      </div>
    );
  }

  return (
    <div className={styles.formContainer}>
      {variableEntries.map(([key, value]) => {
        const fieldType = typeof value === 'number' ? 'number' : 'text';
        const stringValue = value !== null && value !== undefined ? String(value) : '';
        const variableType = typeof value;
        const badgeStyle = getBadgeStyleForVariableType(variableType);
        const TypeIcon = badgeStyle.icon;

        return (
          <div key={key} className={styles.fieldGroup}>
            <div className={styles.fieldLabelContainer}>
              <span className={styles.fieldLabel}>{key}</span>
              <Badge variant={badgeStyle.variant} icon={TypeIcon ? <TypeIcon /> : undefined} className={badgeStyle.className}>
                {variableType}
              </Badge>
            </div>
            <Input
              size="sm"
              labelText=""
              placeholderText={`Enter ${key}`}
              value={stringValue}
              disabled={readOnly}
              inputProps={{
                id: `task-var-${key}`,
                type: fieldType,
                onChange: (e: React.ChangeEvent<HTMLInputElement>) =>
                  handleInputChange(key, e.target.value),
              }}
            />
          </div>
        );
      })}
    </div>
  );
};
