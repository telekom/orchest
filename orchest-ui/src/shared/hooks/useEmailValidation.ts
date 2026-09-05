import { isValidEmail } from '@/shared/utils';
import { useMemo } from 'react';

export interface EmailValidationResult {
  isValid: boolean;
  isInvalid: boolean;
  error: string | null;
}

/**
 * Custom hook for email validation with memoization
 * Provides validation state and error messages for form inputs
 *
 * @param email - Email address to validate
 * @param options - Validation options
 * @returns Validation result with isValid, isInvalid, and error message
 *
 * @example
 * ```tsx
 * const { isInvalid, error } = useEmailValidation(email);
 * return (
 *   <>
 *     <Input value={email} aria-invalid={isInvalid} />
 *     {error && <span className={styles.error}>{error}</span>}
 *   </>
 * );
 * ```
 */
export const useEmailValidation = (
  email: string,
  options: {
    /**
     * Only show error when there's user input (default: true)
     * Prevents showing "invalid" state on mount before user types
     */
    onlyWhenDirty?: boolean;
    /**
     * Custom error message (default: "Please enter a valid email address")
     */
    errorMessage?: string;
  } = {}
): EmailValidationResult => {
  const { onlyWhenDirty = true, errorMessage = 'Please enter a valid email address' } = options;

  return useMemo(() => {
    const trimmed = email.trim();

    // Empty input is neither valid nor invalid (unless required is handled elsewhere)
    if (trimmed.length === 0) {
      return {
        isValid: false,
        isInvalid: false,
        error: null,
      };
    }

    // Check if email is valid
    const valid = isValidEmail(trimmed);

    // Only show invalid state if dirty check is disabled or there's input
    const showInvalid = !onlyWhenDirty || trimmed.length > 0;

    return {
      isValid: valid,
      isInvalid: !valid && showInvalid,
      error: !valid && showInvalid ? errorMessage : null,
    };
  }, [email, onlyWhenDirty, errorMessage]);
};
