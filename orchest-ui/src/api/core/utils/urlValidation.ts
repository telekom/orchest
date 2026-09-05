import { PRIVATE_IP_PATTERNS } from "@/shared/constants";

/**
 * Checks if a hostname belongs to a private network
 */
export function isPrivateNetwork(hostname: string): boolean {
  return PRIVATE_IP_PATTERNS.some(pattern => hostname.includes(pattern));
}

/**
 * Checks if a hostname matches allowed external domains
 */
export function isAllowedDomain(hostname: string, allowedDomains: string[]): boolean {
  return allowedDomains.some(allowed => {
    if (!allowed) return false;
    const allowedUrl = new URL(allowed.startsWith('http') ? allowed : `https://${allowed}`);
    return hostname === allowedUrl.hostname || hostname.endsWith(`.${allowedUrl.hostname}`);
  });
}

/**
 * Validates external URL for security requirements
 * @throws {Error} if URL violates security policies
 */
export function validateExternalUrl(url: string, allowedDomains: string[]): void {
  try {
    const parsedUrl = new URL(url);
    const hostname = parsedUrl.hostname.toLowerCase();

    if (isPrivateNetwork(hostname)) {
      throw new Error('Access to internal/private networks is not allowed');
    }

    if (!isAllowedDomain(hostname, allowedDomains)) {
      throw new Error(`External domain ${hostname} is not whitelisted`);
    }

    if (parsedUrl.protocol !== 'https:') {
      throw new Error('Only HTTPS URLs are allowed for external requests');
    }
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error('Invalid URL format');
    }
    throw error;
  }
}
