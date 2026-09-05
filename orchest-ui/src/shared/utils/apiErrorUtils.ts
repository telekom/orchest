import { toast } from "@/design-system/components/ui/sonner";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";

interface ApiErrorHandlerOptions {
  operation: string;
  successMessage?: string;
  errorMessage?: string;
  onSuccess?: () => void;
}

/**
 * Wrapper for API operations with standardized success/error handling
 * Uses the global error handler for consistent error processing across the app
 */
export async function handleApiOperation<T>(
  apiCall: () => Promise<T>,
  options: ApiErrorHandlerOptions
): Promise<{ success: boolean; data?: T }> {
  const {
    operation,
    successMessage,
    errorMessage,
    onSuccess,
  } = options;

  try {
    const data = await apiCall();

    if (successMessage) {
      toast.success(successMessage);
    }

    onSuccess?.();

    return { success: true, data };
  } catch (error) {
    // Use global error handler for consistent error processing
    globalErrorHandler.handleError(
      error instanceof Error ? error : new Error(String(error)),
      'api',
      { operation }
    );

    // Show custom error message if provided
    if (errorMessage) {
      toast.error(errorMessage);
    }

    return { success: false };
  }
}
