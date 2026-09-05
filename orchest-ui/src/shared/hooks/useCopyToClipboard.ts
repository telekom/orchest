import { useCallback, useState } from "react";
import { toast } from "@/design-system/components/ui/sonner";
import { TIMING } from "@/shared/constants";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";

interface UseCopyToClipboardOptions {
  timeout?: number;
  successMessage?: string;
  errorMessage?: string;
  showToast?: boolean;
}

interface UseCopyToClipboardReturn {
  copyToClipboard: (text: string, itemId?: string) => Promise<boolean>;
  copiedItems: Record<string, boolean>;
  isCopied: (itemId: string) => boolean;
  clearCopied: () => void;
}

export function useCopyToClipboard(
  options: UseCopyToClipboardOptions = {}
): UseCopyToClipboardReturn {
  const {
    timeout = TIMING.COPY_FEEDBACK_DURATION,
    successMessage = "Copied to clipboard",
    errorMessage = "Failed to copy to clipboard",
    showToast = true,
  } = options;

  const [copiedItems, setCopiedItems] = useState<Record<string, boolean>>({});

  const copyToClipboard = useCallback(
    async (text: string, itemId?: string): Promise<boolean> => {
      try {
        await navigator.clipboard.writeText(text);

        if (itemId) {
          setCopiedItems((prev) => ({ ...prev, [itemId]: true }));

          setTimeout(() => {
            setCopiedItems((prev) => ({ ...prev, [itemId]: false }));
          }, timeout);
        }

        if (showToast) {
          toast.success(successMessage);
        }

        return true;
      } catch (error) {
        globalErrorHandler.handleError(
          error instanceof Error ? error : new Error(String(error)),
          'runtime',
          {
            context: 'copy-to-clipboard',
            severity: 'low'
          }
        );

        if (showToast) {
          toast.error(errorMessage);
        }

        return false;
      }
    },
    [timeout, successMessage, errorMessage, showToast]
  );

  const isCopied = useCallback(
    (itemId: string): boolean => {
      return copiedItems[itemId] === true;
    },
    [copiedItems]
  );

  const clearCopied = useCallback(() => {
    setCopiedItems({});
  }, []);

  return {
    copyToClipboard,
    copiedItems,
    isCopied,
    clearCopied,
  };
}
