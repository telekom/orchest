/**
 * SECURITY: Security Utilities
 *
 * Provides input validation, sanitization, and XSS prevention utilities
 * for protecting against common web vulnerabilities.
 */

import { logger } from '@/shared/utils/logger';

/**
 * SECURITY: HTML Entity Encoding
 * Encodes special characters to prevent XSS attacks
 */
export function encodeHTML(str: string): string {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

/**
 * SECURITY: Decode HTML entities
 */
export function decodeHTML(str: string): string {
  const div = document.createElement('div');
  div.innerHTML = str;
  return div.textContent || '';
}

/**
 * SECURITY: Sanitize HTML to prevent XSS
 * Removes all HTML tags and encodes special characters
 */
export function sanitizeHTML(input: string): string {
  if (!input) return '';

  // Remove all HTML tags
  let sanitized = input.replace(/<[^>]*>/g, '');

  // Encode special characters
  sanitized = encodeHTML(sanitized);

  return sanitized;
}

/**
 * SECURITY: Sanitize user input for safe display
 * More aggressive than sanitizeHTML - suitable for user-generated content
 */
export function sanitizeUserInput(input: string): string {
  if (!input) return '';

  // Remove script tags and their content
  let sanitized = input.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');

  // Remove event handlers
  sanitized = sanitized.replace(/on\w+\s*=\s*["'][^"']*["']/gi, '');
  sanitized = sanitized.replace(/on\w+\s*=\s*[^\s>]*/gi, '');

  // Remove javascript: protocol
  sanitized = sanitized.replace(/javascript:/gi, '');

  // Remove data: protocol (can be used for XSS)
  sanitized = sanitized.replace(/data:text\/html/gi, '');

  // Remove all HTML tags
  sanitized = sanitized.replace(/<[^>]*>/g, '');

  // Encode special characters
  sanitized = encodeHTML(sanitized);

  return sanitized.trim();
}

/**
 * SECURITY: Validate email format
 */
export function isValidEmail(email: string): boolean {
  if (!email) return false;

  // RFC 5322 compliant email regex (simplified)
  const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

  return emailRegex.test(email) && email.length <= 254;
}

/**
 * SECURITY: Validate URL format
 */
export function isValidURL(url: string): boolean {
  if (!url) return false;

  try {
    const parsed = new URL(url);
    // Only allow http and https protocols
    return ['http:', 'https:'].includes(parsed.protocol);
  } catch {
    return false;
  }
}

/**
 * SECURITY: Sanitize URL to prevent javascript: and data: protocols
 */
export function sanitizeURL(url: string): string {
  if (!url) return '';

  // Remove dangerous protocols
  const sanitized = url.replace(/^(javascript|data|vbscript):/gi, '');

  // Validate the URL
  if (!isValidURL(sanitized)) {
    return '';
  }

  return sanitized;
}

/**
 * SECURITY: Validate filename to prevent path traversal
 */
export function isValidFilename(filename: string): boolean {
  if (!filename) return false;

  // Check for path traversal attempts
  if (filename.includes('..') || filename.includes('/') || filename.includes('\\')) {
    return false;
  }

  // Check for null bytes
  if (filename.includes('\0')) {
    return false;
  }

  // Allow only safe characters
  const safeFilenameRegex = /^[a-zA-Z0-9._-]+$/;
  return safeFilenameRegex.test(filename);
}

/**
 * SECURITY: Sanitize filename
 */
export function sanitizeFilename(filename: string): string {
  if (!filename) return '';

  // Remove path components
  let sanitized = filename.replace(/^.*[\\\/]/, '');

  // Remove dangerous characters
  sanitized = sanitized.replace(/[^\w\s.-]/g, '');

  // Remove null bytes
  sanitized = sanitized.replace(/\0/g, '');

  // Limit length
  if (sanitized.length > 255) {
    sanitized = sanitized.substring(0, 255);
  }

  return sanitized.trim();
}

/**
 * SECURITY: Validate JSON string
 */
export function isValidJSON(str: string): boolean {
  if (!str) return false;

  try {
    JSON.parse(str);
    return true;
  } catch {
    return false;
  }
}

/**
 * SECURITY: Safe JSON parse with fallback
 */
export function safeJSONParse<T = unknown>(str: string, fallback: T): T {
  try {
    return JSON.parse(str) as T;
  } catch {
    return fallback;
  }
}

/**
 * SECURITY: Validate integer input
 */
export function isValidInteger(value: unknown, min?: number, max?: number): boolean {
  const num = Number(value);

  if (!Number.isInteger(num)) {
    return false;
  }

  if (min !== undefined && num < min) {
    return false;
  }

  if (max !== undefined && num > max) {
    return false;
  }

  return true;
}

/**
 * SECURITY: Validate string length
 */
export function isValidLength(str: string, min: number, max: number): boolean {
  if (!str) return min === 0;
  return str.length >= min && str.length <= max;
}

/**
 * SECURITY: Check for SQL injection patterns
 * Note: This is NOT a replacement for parameterized queries on the backend!
 * This is just a client-side sanity check.
 */
export function containsSQLInjectionPattern(input: string): boolean {
  if (!input) return false;

  const sqlPatterns = [
    /(\bUNION\b.*\bSELECT\b)/gi,
    /(\bSELECT\b.*\bFROM\b)/gi,
    /(\bINSERT\b.*\bINTO\b)/gi,
    /(\bUPDATE\b.*\bSET\b)/gi,
    /(\bDELETE\b.*\bFROM\b)/gi,
    /(\bDROP\b.*\bTABLE\b)/gi,
    /(\bEXEC\b|\bEXECUTE\b)/gi,
    /(;.*--)|(--.*$)/gi,
    /('.*OR.*'.*=.*')/gi,
  ];

  return sqlPatterns.some(pattern => pattern.test(input));
}

/**
 * SECURITY: Check for command injection patterns
 */
export function containsCommandInjectionPattern(input: string): boolean {
  if (!input) return false;

  const commandPatterns = [
    /[;&|`$()]/g,
    /\.\.\//g,
    /\${/g,
  ];

  return commandPatterns.some(pattern => pattern.test(input));
}

/**
 * SECURITY: Validate and sanitize process ID
 * Process IDs should be alphanumeric with hyphens and underscores only
 */
export function sanitizeProcessId(processId: string): string {
  if (!processId) return '';

  // Remove any characters that aren't alphanumeric, hyphen, or underscore
  let sanitized = processId.replace(/[^a-zA-Z0-9_-]/g, '');

  // Limit length
  if (sanitized.length > 100) {
    sanitized = sanitized.substring(0, 100);
  }

  return sanitized.trim();
}

/**
 * SECURITY: Validate BPMN/DMN XML content
 * Basic validation to ensure XML structure is valid
 */
export function isValidXML(xml: string): boolean {
  if (!xml) return false;

  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xml, 'text/xml');

    // Check for parser errors
    const parseError = doc.querySelector('parsererror');
    return !parseError;
  } catch {
    return false;
  }
}

/**
 * SECURITY: Escape regex special characters
 */
export function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * SECURITY: Check if string contains only safe characters for display
 */
export function isSafeForDisplay(str: string): boolean {
  if (!str) return true;

  // Check for control characters (except newline, tab, carriage return)
  const controlCharsRegex = /[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/;

  return !controlCharsRegex.test(str);
}

/**
 * SECURITY: Rate limit validation
 * Validates rate limit configuration values
 */
export interface RateLimitValidation {
  isValid: boolean;
  errors: string[];
}

export function validateRateLimitConfig(
  processId: string,
  windowDuration: number,
  allowedSize: number
): RateLimitValidation {
  const errors: string[] = [];

  // Validate process ID
  if (!processId || processId.trim().length < 2) {
    errors.push('Process ID must be at least 2 characters');
  }

  if (processId.length > 100) {
    errors.push('Process ID must not exceed 100 characters');
  }

  if (containsSQLInjectionPattern(processId) || containsCommandInjectionPattern(processId)) {
    errors.push('Process ID contains invalid characters');
  }

  // Validate window duration
  if (!isValidInteger(windowDuration, 1, 3600)) {
    errors.push('Window duration must be between 1 and 3600 seconds');
  }

  // Validate allowed size
  if (!isValidInteger(allowedSize, 1, 100)) {
    errors.push('Allowed size must be between 1 and 100');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * SECURITY: Content Security Policy utilities
 */

export interface CSPViolation {
  documentURI: string;
  violatedDirective: string;
  effectiveDirective: string;
  originalPolicy: string;
  blockedURI: string;
  statusCode: number;
}

/**
 * Log CSP violations (can be sent to backend for monitoring)
 */
export function handleCSPViolation(violation: CSPViolation): void {
  if (import.meta.env.DEV) {
    logger.warn('CSP Violation:', violation);
  }

  // In production, send to backend monitoring
  // This would be implemented based on your monitoring infrastructure
}

/**
 * Setup CSP violation reporting
 */
export function setupCSPReporting(): void {
  document.addEventListener('securitypolicyviolation', (e) => {
    handleCSPViolation({
      documentURI: e.documentURI,
      violatedDirective: e.violatedDirective,
      effectiveDirective: e.effectiveDirective,
      originalPolicy: e.originalPolicy,
      blockedURI: e.blockedURI,
      statusCode: e.statusCode,
    });
  });
}
