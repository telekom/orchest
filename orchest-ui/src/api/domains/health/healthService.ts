import { environment } from '@/shared/constants/environment';
import axios from 'axios';

export type HealthStatus = 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';

export interface DiskSpaceDetails {
  total: number;
  free: number;
  threshold: number;
  path: string;
  exists: boolean;
}

export interface HealthComponent<TDetails = Record<string, unknown>> {
  status: HealthStatus;
  details?: TDetails;
}

export interface ActuatorHealthResponse {
  status: HealthStatus;
  components: {
    diskSpace?: HealthComponent<DiskSpaceDetails>;
    livenessState?: HealthComponent;
    mongo?: HealthComponent<{ version?: string }>;
    readinessState?: HealthComponent;
    kafka?: HealthComponent<{ version?: string }>;
    [key: string]: HealthComponent | undefined;
  };
}

type ActuatorPropertyValue = string | { value?: unknown };

export interface ActuatorEnvPropertySource {
  name: string;
  properties?: Record<string, ActuatorPropertyValue>;
}

export interface ActuatorEnvResponse {
  propertySources?: ActuatorEnvPropertySource[];
}

export interface EngineEnvInfo {
  appVersion: string | null;
}

function requireActuatorHealthUrl(): string {
  const url = environment.engineActuatorUrl;
  if (!url) throw new Error('VITE_ENGINE_ACTUATOR_URL is not configured');
  return url;
}

/** Derive `/env` from the configured health URL (`…/actuator/health` → `…/actuator/env`). */
export function toActuatorEnvUrl(healthUrl: string): string {
  if (/\/health\/?$/.test(healthUrl)) {
    return healthUrl.replace(/\/health\/?$/, '/env');
  }
  return `${healthUrl.replace(/\/$/, '')}/env`;
}

function readPropertyValue(raw: ActuatorPropertyValue | undefined): string | null {
  if (raw == null) return null;
  if (typeof raw === 'string') {
    const trimmed = raw.trim();
    return trimmed || null;
  }
  if (typeof raw === 'object' && 'value' in raw) {
    const value = raw.value;
    if (value == null) return null;
    const trimmed = String(value).trim();
    return trimmed || null;
  }
  return null;
}

export function extractAppVersion(data: ActuatorEnvResponse): string | null {
  const source = data.propertySources?.find((entry) => entry.name === 'systemEnvironment');
  return readPropertyValue(source?.properties?.APP_VERSION);
}

export const healthService = {
  getHealth: async (): Promise<ActuatorHealthResponse> => {
    const url = requireActuatorHealthUrl();
    const { data } = await axios.get<ActuatorHealthResponse>(url, {
      timeout: 10_000,
    });
    return data;
  },

  getEnv: async (): Promise<EngineEnvInfo> => {
    const url = toActuatorEnvUrl(requireActuatorHealthUrl());
    const { data } = await axios.get<ActuatorEnvResponse>(url, {
      timeout: 10_000,
    });
    return { appVersion: extractAppVersion(data) };
  },
};
