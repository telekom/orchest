import { toast } from "@/design-system/components/ui/sonner";
import { useIsDarkMode, useUIStore } from "@/shared/stores/uiStore";
import { Moon, Sun } from "lucide-react";
import React from "react";
import styles from "./ThemeToggle.module.css";

export const ThemeToggle: React.FC = () => {
  const isDarkMode = useIsDarkMode();
  const setTheme = useUIStore((state) => state.setTheme);

  const toggleTheme = () => {
    const nextTheme = isDarkMode ? "light" : "dark";
    setTheme(nextTheme);
    toast.success(`${nextTheme === "dark" ? "Dark" : "Light"} mode enabled`);
  };

  return (
    <button
      type="button"
      className={styles.toggle}
      onClick={toggleTheme}
      aria-label={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
      title={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
    >
      {isDarkMode ? <Sun size={14} className={styles.iconSun} /> : <Moon size={14} className={styles.iconMoon} />}
    </button>
  );
};

export default ThemeToggle;
