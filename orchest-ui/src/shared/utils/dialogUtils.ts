/**
 * Utility for closing dialogs with a delay to allow animations to complete
 * @param setIsOpen - Function to set the dialog open state
 * @param delayMs - Delay in milliseconds (default: 150ms)
 */
export const closeDialogWithDelay = async (
  setIsOpen: (open: boolean) => void,
  delayMs: number = 150
): Promise<void> => {
  setIsOpen(false);
  await new Promise(resolve => setTimeout(resolve, delayMs));
};
