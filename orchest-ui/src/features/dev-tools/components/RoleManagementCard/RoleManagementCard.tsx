import { Card } from '@/design-system/components/ui/card';
import { Label } from '@/design-system/components/ui/label';
import { UserRoles } from '@/shared/auth';
import { Switch } from '@/design-system/components/ui/switch';
import { FC } from 'react';
import styles from './RoleManagementCard.module.css';

interface RoleManagementCardProps {
  isAdmin: boolean;
  isViewer: boolean;
  isSensitive: boolean;
  onToggleRole: (role: UserRoles | string, isEnabled: boolean) => void;
}

export const RoleManagementCard: FC<RoleManagementCardProps> = ({
  isAdmin,
  isViewer,
  isSensitive,
  onToggleRole
}) => {
  return (
    <Card className={styles.card}>
      <div className={styles.cardContent}>
        <h2 className={styles.title}>
          Role Management
        </h2>

        <div className={styles.rolesContainer}>
          <div className={styles.roleItem}>
            <div className={styles.roleInfo}>
              <Label htmlFor="admin-role" className={styles.roleLabel}>Admin Role</Label>
              <span className={styles.roleDescription}>Full access to all features</span>
            </div>
            <Switch
              selected={isAdmin}
              size="sm"
              label=""
              inputProps={{
                id: "admin-role",
                name: "admin-role",
                onChange: (e) => onToggleRole(UserRoles.ADMIN, e.target.checked)
              }}
            />
          </div>

          <div className={styles.roleItem}>
            <div className={styles.roleInfo}>
              <Label htmlFor="viewer-role" className={styles.roleLabel}>Viewer Role</Label>
              <span className={styles.roleDescription}>Can view all content</span>
            </div>
            <Switch
              selected={isViewer}
              size="sm"
              label=""
              inputProps={{
                id: "viewer-role",
                name: "viewer-role",
                onChange: (e) => onToggleRole(UserRoles.VIEWER, e.target.checked)
              }}
            />
          </div>

          <div className={styles.roleItem}>
            <div className={styles.roleInfo}>
              <Label htmlFor="sensitive-role" className={styles.roleLabel}>Sensitive Role</Label>
              <span className={styles.roleDescription}>Limited access - some content is hidden</span>
            </div>
            <Switch
              selected={isSensitive}
              size="sm"
              label=""
              inputProps={{
                id: "sensitive-role",
                name: "sensitive-role",
                onChange: (e) => onToggleRole(UserRoles.SENSITIVE, e.target.checked)
              }}
            />
          </div>
        </div>
      </div>
    </Card>
  );
};
