/**
 * Runtime configuration bridge.
 *
 * Env values that must vary per deployment (backend URLs, SSO IDs, etc.) are
 * injected at container start via /runtime-config.js, which sets
 * window.__ORCHEST_RUNTIME_CONFIG__. We merge those over import.meta.env so a
 * single build artifact can run in any environment.
 *
 * MODE stays build-time — Vite uses it for tree-shaking.
 */

export const RUNTIME_CONFIG_GLOBAL = '__ORCHEST_RUNTIME_CONFIG__' as const;

type RuntimeConfig = Record<string, string | undefined>;

declare global {
  interface Window {
    __ORCHEST_RUNTIME_CONFIG__?: RuntimeConfig;
  }
}

const isMeaningful = (value: unknown): value is string =>
  typeof value === 'string' && value.length > 0 && !value.startsWith('${');

export const getRuntimeConfig = (): RuntimeConfig => {
  if (typeof window === 'undefined') return {};
  return window[RUNTIME_CONFIG_GLOBAL] ?? {};
};

export const getEffectiveImportMetaEnv = (): ImportMetaEnv => {
  const runtime = getRuntimeConfig();
  const merged: Record<string, unknown> = { ...import.meta.env };
  for (const [key, value] of Object.entries(runtime)) {
    if (isMeaningful(value)) merged[key] = value;
  }
  return merged as ImportMetaEnv;
};
