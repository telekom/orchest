import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import tseslint from "typescript-eslint";

export default tseslint.config(
  { ignores: ["dist", "node_modules", ".claude"] },
  {
    extends: [...tseslint.configs.recommended],
    files: ["**/*.{ts,tsx}"],
    plugins: {
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
    },
    languageOptions: {
      ecmaVersion: 2020,
    },
    rules: {
      // TypeScript
      "@typescript-eslint/no-unused-vars": ["warn", {
        argsIgnorePattern: "^_",
        varsIgnorePattern: "^_",
        destructuredArrayIgnorePattern: "^_",
        caughtErrorsIgnorePattern: "^_",
      }],
      "@typescript-eslint/no-explicit-any": "warn",

      // React Hooks (critical for correctness)
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",

      // React Refresh (Vite HMR)
      "react-refresh/only-export-components": ["warn", {
        allowConstantExport: true,
      }],

      // General quality
      "no-console": ["warn", { allow: ["warn", "error"] }],
    },
  },
  // Allow all console methods in logger utility
  {
    files: ["**/utils/logger.ts"],
    rules: {
      "no-console": "off",
    },
  }
);
