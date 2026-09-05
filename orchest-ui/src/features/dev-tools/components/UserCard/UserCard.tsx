import { Badge } from '@/design-system/components/ui/badge/badge';
import { Card } from '@/design-system/components/ui/card';
import { UserRoles } from '@/shared/auth/models/roles';
import { AuthorizationService } from '@/shared/auth/services/AuthorizationService';
import { UserDetails } from '@/shared/auth/utils/userAuthUtils';
import { FC } from 'react';
import styles from './UserCard.module.css';
import clsx from 'clsx';

interface UserCardProps {
  user: UserDetails | null;
  idTokenRoles: string[];
  allRoles: string[];
}

export const UserCard: FC<UserCardProps> = ({ user, idTokenRoles, allRoles }) => {
  return (
    <Card className={styles.card}>
      <div className={styles.container}>
        <h2 className={styles.title}>User</h2>

        <div className={styles.content}>
          <div className={styles.infoGrid}>
            <div>
              <p className={styles.infoLabel}>Name</p>
              <p className={styles.infoValue}>{user?.name || "Not available"}</p>
            </div>
            <div>
              <p className={styles.infoLabel}>Email</p>
              <p className={styles.infoValue}>{user?.email || "Not available"}</p>
            </div>
          </div>

          <div className={styles.rolesSection}>
            <div className={styles.rolesBox}>
              <p className={styles.rolesTitle}>ID Token Roles</p>
              <div className={styles.rolesList}>
                {idTokenRoles.length > 0 ? (
                  idTokenRoles.map((role) => (
                    <Badge
                      key={role}
                      variant="outline"
                      className={clsx(styles.roleBadge, styles.roleBadgeDefault)}
                    >
                      {role}
                    </Badge>
                  ))
                ) : (
                  <span className={styles.noRoles}>No roles in ID token</span>
                )}
              </div>
            </div>

            <div className={styles.rolesBox}>
              <p className={styles.rolesTitle}>Active Roles</p>
              <div className={styles.rolesList}>
                <Badge
                  variant="outline"
                  className={clsx(
                    styles.roleBadge,
                    AuthorizationService.hasExactRole(allRoles, UserRoles.ADMIN)
                      ? styles.roleBadgeActive
                      : styles.roleBadgeInactive
                  )}
                >
                  {UserRoles.ADMIN} {AuthorizationService.hasExactRole(allRoles, UserRoles.ADMIN) && "✓"}
                </Badge>

                <Badge
                  variant="outline"
                  className={clsx(
                    styles.roleBadge,
                    AuthorizationService.hasExactRole(allRoles, UserRoles.VIEWER)
                      ? styles.roleBadgeActive
                      : styles.roleBadgeInactive
                  )}
                >
                  {UserRoles.VIEWER} {AuthorizationService.hasExactRole(allRoles, UserRoles.VIEWER) && "✓"}
                </Badge>

                <Badge
                  variant="outline"
                  className={clsx(
                    styles.roleBadge,
                    AuthorizationService.hasExactRole(allRoles, UserRoles.SENSITIVE)
                      ? styles.roleBadgeActive
                      : styles.roleBadgeInactive
                  )}
                >
                  {UserRoles.SENSITIVE} {AuthorizationService.hasExactRole(allRoles, UserRoles.SENSITIVE) && "✓"}
                </Badge>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};
