import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { StorageUtils } from '@/shared/utils/storageUtils';

export interface UserDetails {
  name: string;
  firstName: string;
  givenName?: string;
  email: string;
  loginHint?: string;
  roles: string[];
  profilePicture?: string;
}

export class UserAuthUtils {
  public static formatFullName(fullName?: string): string {
    if (!fullName) return '';
    return fullName.trim();
  }

  public static getFirstName(fullName?: string): string {
    if (!fullName) return '';
    return fullName.split(' ')[0] || '';
  }

  public static getUserDetails(): UserDetails | null {
    return StorageUtils.getItem<UserDetails>(STORAGE_KEYS.USER_DETAILS);
  }

  public static setUserDetails(userDetails: UserDetails): void {
    StorageUtils.setItem(STORAGE_KEYS.USER_DETAILS, userDetails);
  }

  public static isLoggedIn(): boolean {
    return !!this.getUserDetails();
  }

  public static clearUserData(): void {
    StorageUtils.removeItem(STORAGE_KEYS.USER_DETAILS);
  }
}
