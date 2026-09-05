import { z } from 'zod';
import { logger } from '../utils/logger';
import { getEffectiveImportMetaEnv } from './runtimeConfig';

const envSchema = z.object({
  VITE_BASEPATH: z.string().optional().default(''),
  VITE_AI_API_URL: z.string().optional().default(''),
  VITE_BPMN_PROMPT_BASEPATH: z.string().optional().default(''),
  VITE_BYPASS_LOGIN: z.string().optional().default('false'),
  VITE_SSO_CLIENT_ID: z.string().optional().default(''),
  VITE_MICROSOFT_AUTHORITY: z.string().optional().default(''),
  VITE_SSO_TENANT_ID: z.string().optional().default(''),
  VITE_REDIRECT_URI: z.string().optional(),
  VITE_SSO_LOGOUT_REDIRECT_URI: z.string().optional(),
  VITE_ALLOWED_EXTERNAL_DOMAINS: z.string().optional().default(''),
  // Deployment environment (dev, teststable, uat, prod-ref, beta, prod) —
  // sourced from ConfigMap at runtime. Distinct from Vite's import.meta.env.MODE,
  // which is fixed at build time to "production" or "development".
  VITE_MODE: z.string().optional().default('development'),
  VITE_ENABLE_QUOTE_BANNER: z.string().optional().default('false'),
  VITE_ENGINE_ACTUATOR_URL: z.string().optional().default(''),
});

const parseEnv = () => {
  try {
    return envSchema.parse(getEffectiveImportMetaEnv());
  } catch (error) {
    if (error instanceof z.ZodError) {
      logger.error('❌ Environment validation failed:');
      error.errors.forEach((err) => {
        logger.error(`  - ${err.path.join('.')}: ${err.message}`);
      });
      throw new Error('Invalid environment configuration. Check console for details.');
    }
    throw error;
  }
};

const env = parseEnv();

export const environment = {
  apiUrl: env.VITE_BASEPATH,
  aiApiUrl: env.VITE_AI_API_URL,
  bpmnPromptBasePath: env.VITE_BPMN_PROMPT_BASEPATH,
  bypassLogin: env.VITE_BYPASS_LOGIN === 'true',
  auth: {
    clientId: env.VITE_SSO_CLIENT_ID,
    authority: `${env.VITE_MICROSOFT_AUTHORITY}/${env.VITE_SSO_TENANT_ID}`,
    redirectUri: env.VITE_REDIRECT_URI || window.location.origin,
    logoutRedirectUri: env.VITE_SSO_LOGOUT_REDIRECT_URI || window.location.origin,
    scopes: ['openid', 'profile', 'User.Read'],
  },
  security: {
    allowedExternalDomains: env.VITE_ALLOWED_EXTERNAL_DOMAINS
      ? env.VITE_ALLOWED_EXTERNAL_DOMAINS.split(',').map(domain => domain.trim()).filter(Boolean)
      : [],
  },
  mode: env.VITE_MODE,
  enableQuoteBanner: env.VITE_ENABLE_QUOTE_BANNER === 'true',
  engineActuatorUrl: env.VITE_ENGINE_ACTUATOR_URL,
} as const;

export type Environment = typeof environment;
  