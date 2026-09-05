import { useCallback, useEffect, useMemo, useState } from 'react';

export type ValidationRule<T> = (value: T) => string;
export type ValidationRules<T> = {
  [K in keyof T]?: ValidationRule<T[K]>;
};
export type ValidationErrors<T> = Partial<Record<keyof T, string>>;

export interface UseFormValidationOptions<T> {
  rules: ValidationRules<T>;
  initialErrors?: ValidationErrors<T>;
  validateOnMount?: boolean;
}

/**
 * Hook for form validation with per-field error tracking
 */
export function useFormValidation<T extends Record<string, unknown>>(
  options: UseFormValidationOptions<T>
) {
  const { rules, initialErrors = {}, validateOnMount = false } = options;

  const [errors, setErrors] = useState<ValidationErrors<T>>(initialErrors);

  const validateField = useCallback(
    <K extends keyof T>(field: K, value: T[K]): string => {
      const rule = rules[field];
      if (!rule) return '';

      const errorMessage = rule(value);

      setErrors((prev) => {
        if (!errorMessage) {
           
          const { [field]: _, ...rest } = prev;
          return rest;
        }
        return { ...prev, [field]: errorMessage };
      });

      return errorMessage;
    },
    [rules]
  );

  const validate = useCallback(
    (values: T): ValidationErrors<T> => {
      const newErrors: ValidationErrors<T> = {};

      (Object.keys(rules) as Array<keyof T>).forEach((field) => {
        const rule = rules[field];
        if (rule) {
          const errorMessage = rule(values[field]);
          if (errorMessage) {
            newErrors[field] = errorMessage;
          }
        }
      });

      setErrors(newErrors);
      return newErrors;
    },
    [rules]
  );

  const hasErrors = useCallback((): boolean => {
    return Object.keys(errors).length > 0 && Object.values(errors).some((error) => error !== '');
  }, [errors]);

  const clearErrors = useCallback(() => {
    setErrors({});
  }, []);

  const clearFieldError = useCallback(<K extends keyof T>(field: K) => {
    setErrors((prev) => {
       
      const { [field]: _, ...rest } = prev;
      return rest;
    });
  }, []);

  const getFieldError = useCallback(
    <K extends keyof T>(field: K): string | undefined => {
      return errors[field];
    },
    [errors]
  );

  const hasFieldError = useCallback(
    <K extends keyof T>(field: K): boolean => {
      return !!(errors[field] && errors[field] !== '');
    },
    [errors]
  );

  const errorCount = useMemo(() => {
    return Object.values(errors).filter((error) => error !== '').length;
  }, [errors]);

  useEffect(() => {
    if (validateOnMount && Object.keys(rules).length > 0) {
      setErrors(initialErrors);
    }
  }, [validateOnMount, rules, initialErrors]);

  return {
    errors,
    validateField,
    validate,
    hasErrors,
    clearErrors,
    clearFieldError,
    getFieldError,
    hasFieldError,
    errorCount,
  };
}
