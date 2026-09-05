import { TIMING } from "@/shared/constants/ui.config";
import { Toaster as Sonner, toast } from "sonner";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import clsx from "clsx";
import { createPortal } from "react-dom";
import styles from "./sonner.module.css";

type ToasterProps = React.ComponentProps<typeof Sonner>

const Toaster = ({ duration = TIMING.TOAST_DEFAULT_DURATION, ...props }: ToasterProps) => {
  const isDarkMode = useIsDarkMode();

  const toaster = (
    <Sonner
      theme={isDarkMode ? "dark" : "light"}
      className={clsx("toaster group", isDarkMode && styles.darkModeToast)}
      richColors
      duration={duration}
      {...props}
      style={{ ...props.style, zIndex: "var(--z-index-toast)" }}
    />
  );

  // Portal to body so toasts aren't trapped under modal stacking contexts
  if (typeof document === "undefined") {
    return toaster;
  }

  return createPortal(toaster, document.body);
};

// eslint-disable-next-line react-refresh/only-export-components
export { toast, Toaster };

