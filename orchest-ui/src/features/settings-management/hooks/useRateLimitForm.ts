import type { RateLimit } from '@/api/domains';
import { useCallback, useState } from 'react';
import { createNewRateLimit, validateForm } from '../utils/rateLimitUtils';

export interface UseRateLimitFormReturn {
  formData: RateLimit | null;
  isOpen: boolean;
  openForEdit: (config: RateLimit) => void;
  openForNew: () => void;
  updateField: (field: keyof RateLimit, value: string | number | boolean) => void;
  close: () => void;
  validate: () => string[];
}

export function useRateLimitForm(): UseRateLimitFormReturn {
  const [formData, setFormData] = useState<RateLimit | null>(null);
  const [isOpen, setIsOpen] = useState(false);

  const openForEdit = useCallback((config: RateLimit) => {
    const normalized: RateLimit = {
      switchToNewCamunda: false,
      ...JSON.parse(JSON.stringify(config)),
    };
    setFormData(normalized);
    setIsOpen(true);
  }, []);

  const openForNew = useCallback(() => {
    setFormData(createNewRateLimit());
    setIsOpen(true);
  }, []);

  const updateField = useCallback((field: keyof RateLimit, value: string | number | boolean) => {
    setFormData((prev) => (prev ? { ...prev, [field]: value } : null));
  }, []);

  const close = useCallback(() => {
    setFormData(null);
    setIsOpen(false);
  }, []);

  const validate = useCallback(() => {
    return formData ? validateForm(formData) : [];
  }, [formData]);

  return {
    formData,
    isOpen,
    openForEdit,
    openForNew,
    updateField,
    close,
    validate,
  };
}
