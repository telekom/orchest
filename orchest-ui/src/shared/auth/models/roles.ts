export enum UserRoles {
  ADMIN = 'ORCHEST_ADMIN',    // Admin access - all actions allowed
  VIEWER = 'ORCHEST_READ_SENSITIVE',  // Viewer access - can view most content but not sensitive data
  SENSITIVE = 'ORCHEST_READ_NON_SENSITIVE',  // Limited access - cannot view sensitive information
}

/** Highest-privilege Orchest role first — used for X-Active-Role and primary UI behavior */
export const ROLE_PRIORITY_ORDER: UserRoles[] = [
  UserRoles.ADMIN,
  UserRoles.VIEWER,
  UserRoles.SENSITIVE,
];

export const roleHierarchy: Record<UserRoles, UserRoles[]> = {
  [UserRoles.ADMIN]: [UserRoles.ADMIN, UserRoles.VIEWER, UserRoles.SENSITIVE],
  [UserRoles.VIEWER]: [UserRoles.VIEWER, UserRoles.SENSITIVE],
  [UserRoles.SENSITIVE]: [UserRoles.SENSITIVE],
};

export const MOCK_ROLES_STORAGE_KEY = 'mock_user_roles';
