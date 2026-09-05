export const HTTP_STATUS = {
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  VALIDATION_FAILED: 422,
  RATE_LIMITED: 429,
  INTERNAL_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504,
} as const;

export const MIME_TYPES = {
  JSON: 'application/json',
  XML: 'application/xml',
  TEXT_XML: 'text/xml',
  EVENT_STREAM: 'text/event-stream',
  MULTIPART_FORM: 'multipart/form-data',
  PNG: 'image/png',
  SVG: 'image/svg+xml',
} as const;

export const EXTERNAL_API_DOMAINS = {
  OPENAI: 'api.openai.com',
  ANTHROPIC: 'api.anthropic.com',
} as const;

export const PRIVATE_IP_PATTERNS = [
  'localhost',
  '127.0.0.1',
  '0.0.0.0',
  '192.168.',
  '10.',
  '172.16.', '172.17.', '172.18.', '172.19.',
  '172.20.', '172.21.', '172.22.', '172.23.',
  '172.24.', '172.25.', '172.26.', '172.27.',
  '172.28.', '172.29.', '172.30.', '172.31.',
  'metadata.google.internal',
  '169.254.169.254',
] as const;
