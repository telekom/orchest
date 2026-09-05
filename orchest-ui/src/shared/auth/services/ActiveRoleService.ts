import { AuthorizationService } from './AuthorizationService';

class ActiveRoleService {
  private roles: string[] = [];

  setRoles(roles: string[]) {
    this.roles = AuthorizationService.getEffectiveRoles(roles);
  }

  getActiveRole(): string | null {
    return AuthorizationService.getPrimaryRole(this.roles);
  }

  getRoles(): string[] {
    return this.roles;
  }
}

export const activeRoleService = new ActiveRoleService();
