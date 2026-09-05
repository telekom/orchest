import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { StorageUtils } from '@/shared/utils/storageUtils';
import { AuthorizationService } from '../services/AuthorizationService';
import { parseJWT } from './jwtUtils';

export const parseIdTokenRoles = (): string[] => {
  const idToken = StorageUtils.getItem<string>(STORAGE_KEYS.ID_TOKEN);
  if (!idToken) return [];

  const payload = parseJWT(idToken);
  const rawRoles = payload?.roles ?? payload?.groups;
  return AuthorizationService.normalizeRoleList(rawRoles);
};
