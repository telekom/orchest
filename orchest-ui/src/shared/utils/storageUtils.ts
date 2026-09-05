import { globalErrorHandler } from '../error/globalErrorHandler';
import { isNoLoginMode } from './environmentUtils';

export class StorageUtils {
  private static readonly storagePrefix = 'orchest_';
  
  public static getStoragePrefix(): string {
    return this.storagePrefix;
  }

  public static setItem(key: string, value: unknown): void {
    try {
      const prefixedKey = this.getPrefixedKey(key);
      const serializedValue = JSON.stringify(value);
      
      const isMockData = key.includes('mock');
      
      if (isNoLoginMode() && isMockData) {
        localStorage.setItem(prefixedKey, serializedValue);
      } else {
        sessionStorage.setItem(prefixedKey, serializedValue);
      }
    } catch (error) {
      globalErrorHandler.handleError(
        error instanceof Error ? error : new Error(String(error)),
        'runtime',
        {
          context: 'storage-set-item',
          key,
          severity: 'low'
        }
      );
    }
  }

  public static getItem<T>(key: string): T | null {
    try {
      const prefixedKey = this.getPrefixedKey(key);
      
      const isMockData = key.includes('mock');
      
      let item: string | null = null;
      
      if (isNoLoginMode() && isMockData) {
        item = localStorage.getItem(prefixedKey);
      } else {
        item = sessionStorage.getItem(prefixedKey);
      }
      
      return item ? JSON.parse(item) : null;
    } catch (error) {
      globalErrorHandler.handleError(
        error instanceof Error ? error : new Error(String(error)),
        'runtime',
        {
          context: 'storage-get-item',
          key,
          severity: 'low'
        }
      );
      return null;
    }
  }

  public static removeItem(key: string): void {
    try {
      const prefixedKey = this.getPrefixedKey(key);
      
      const isMockData = key.includes('mock');
      
      if (isNoLoginMode() && isMockData) {
        localStorage.removeItem(prefixedKey);
      } else {
        sessionStorage.removeItem(prefixedKey);
      }
    } catch (error) {
      globalErrorHandler.handleError(
        error instanceof Error ? error : new Error(String(error)),
        'runtime',
        {
          context: 'storage-remove-item',
          key,
          severity: 'low'
        }
      );
    }
  }

  public static clearAll(includeMockData: boolean = false): void {
    try {
      Object.keys(sessionStorage)
        .filter(key => key.startsWith(this.storagePrefix))
        .forEach(key => sessionStorage.removeItem(key));
      
      if (includeMockData) {
        Object.keys(localStorage)
          .filter(key => key.startsWith(this.storagePrefix) && key.includes('mock'))
          .forEach(key => localStorage.removeItem(key));
      }
    } catch (error) {
      globalErrorHandler.handleError(
        error instanceof Error ? error : new Error(String(error)),
        'runtime',
        {
          context: 'storage-clear-all',
          severity: 'medium'
        }
      );
    }
  }

  private static getPrefixedKey(key: string): string {
    return `${this.storagePrefix}${key}`;
  }
}
