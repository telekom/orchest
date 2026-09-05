/**
 * Unified Secure Storage Service
 * Handles token and data persistence with automatic expiry management
 * Replaces both TokenStore and StorageUtils with a single, coherent implementation
 */

import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { globalErrorHandler } from '@/shared/error/globalErrorHandler';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import { MOCK_ROLES_STORAGE_KEY } from '../models/roles';
import { getTokenExpiryTime } from '../utils/jwtUtils';

const STORAGE_PREFIX = 'orchest_';
const DEFAULT_EXPIRY_MINUTES = 60;

export interface StorageOptions {
  expiryMinutes?: number;
}

interface StoredData<T = unknown> {
  value: T;
  expiresAt?: number;
}

/**
 * Determines storage location based on data type and environment
 */
function getStorage(key: string): Storage {
  // Mock roles always use localStorage in dev/beta mode
  if (key === MOCK_ROLES_STORAGE_KEY && isNoLoginMode()) {
    return localStorage;
  }
  // All other data uses sessionStorage
  return sessionStorage;
}

function prefixKey(key: string): string {
  return `${STORAGE_PREFIX}${key}`;
}

function handleStorageError(error: unknown, operation: string, key: string): void {
  globalErrorHandler.handleError(
    error instanceof Error ? error : new Error(String(error)),
    'runtime',
    { context: `secure-storage-${operation}`, key, severity: 'low' }
  );
}

/**
 * Unified storage service for tokens and application data
 */
export class SecureStorage {
  private static memoryCache = new Map<string, StoredData>();

  /**
   * Store a value with optional expiry
   */
  static set<T>(key: string, value: T, options: StorageOptions = {}): void {
    if (!key || value === undefined) return;

    try {
      const { expiryMinutes = DEFAULT_EXPIRY_MINUTES } = options;

      // For JWT tokens, extract expiry from token itself
      const expiresAt = this.isTokenKey(key) && typeof value === 'string'
        ? getTokenExpiryTime(value, expiryMinutes)
        : Date.now() + expiryMinutes * 60 * 1000;

      const data: StoredData<T> = { value, expiresAt };

      // Update memory cache
      this.memoryCache.set(key, data);

      // Persist to storage
      const storage = getStorage(key);
      storage.setItem(prefixKey(key), JSON.stringify(data));
    } catch (error) {
      handleStorageError(error, 'set', key);
    }
  }

  /**
   * Retrieve a value, checking expiry and cache
   */
  static get<T>(key: string): T | null {
    if (!key) return null;

    // Check memory cache first
    const cached = this.memoryCache.get(key);
    if (cached && this.isValid(cached)) {
      return cached.value as T;
    }

    try {
      const storage = getStorage(key);
      const raw = storage.getItem(prefixKey(key));

      if (!raw) return null;

      const data: StoredData<T> = JSON.parse(raw);

      if (this.isValid(data)) {
        this.memoryCache.set(key, data);
        return data.value;
      }

      // Expired - clean up
      this.remove(key);
      return null;
    } catch (error) {
      handleStorageError(error, 'get', key);
      return null;
    }
  }

  /**
   * Remove a value from storage and cache
   */
  static remove(key: string): void {
    if (!key) return;

    this.memoryCache.delete(key);

    try {
      const storage = getStorage(key);
      storage.removeItem(prefixKey(key));
    } catch (error) {
      handleStorageError(error, 'remove', key);
    }
  }

  /**
   * Clear all stored data
   */
  static clearAll(includeMockData = false): void {
    this.memoryCache.clear();

    try {
      // Clear session storage
      Object.keys(sessionStorage)
        .filter(key => key.startsWith(STORAGE_PREFIX))
        .forEach(key => sessionStorage.removeItem(key));

      // Clear mock data from localStorage if requested
      if (includeMockData) {
        Object.keys(localStorage)
          .filter(key => key.startsWith(STORAGE_PREFIX))
          .forEach(key => localStorage.removeItem(key));
      }
    } catch (error) {
      handleStorageError(error, 'clear-all', 'all');
    }
  }

  /**
   * Get all cached items (useful for debugging)
   */
  static getCache(): Map<string, StoredData> {
    return new Map(this.memoryCache);
  }

  private static isValid(data: StoredData): boolean {
    if (!data.value) return false;
    if (!data.expiresAt) return true;
    return Date.now() < data.expiresAt;
  }

  private static isTokenKey(key: string): boolean {
    return [
      STORAGE_KEYS.ACCESS_TOKEN,
      STORAGE_KEYS.ID_TOKEN,
      STORAGE_KEYS.REFRESH_TOKEN,
    ].includes(key);
  }
}

// Convenience methods for common operations
export const secureStorage = {
  setToken: (key: string, token: string, options?: StorageOptions) =>
    SecureStorage.set(key, token, options),

  getToken: (key: string) =>
    SecureStorage.get<string>(key),

  setUserDetails: (details: unknown) =>
    SecureStorage.set(STORAGE_KEYS.USER_DETAILS, details),

  getUserDetails: <T>() =>
    SecureStorage.get<T>(STORAGE_KEYS.USER_DETAILS),

  clearUserData: () => {
    SecureStorage.remove(STORAGE_KEYS.USER_DETAILS);
    SecureStorage.remove(STORAGE_KEYS.ACCESS_TOKEN);
    SecureStorage.remove(STORAGE_KEYS.ID_TOKEN);
    SecureStorage.remove(STORAGE_KEYS.REFRESH_TOKEN);
  },
};
