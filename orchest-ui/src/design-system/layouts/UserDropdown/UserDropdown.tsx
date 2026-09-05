import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/design-system/components/ui/dropdown-menu";
import { useAuth, UserRoles } from "@/shared/auth";
import { ROUTES } from "@/shared/constants/routes";
import { logger } from '@/shared/utils/logger';
import clsx from "clsx";
import { Activity, BarChart3, CheckSquare, KeyRound, LogOut, Radio, ScrollText, User, type LucideIcon } from "lucide-react";
import React, { useMemo } from "react";
import { Link, useLocation } from "react-router-dom";
import styles from "./UserDropdown.module.css";

interface UserMenuItem {
  id: string;
  label: string;
  path: string;
  icon: LucideIcon;
  adminOnly?: boolean;
}

const USER_MENU_ITEMS: UserMenuItem[] = [
  { id: 'approvals', label: 'Approvals', path: ROUTES.APPROVALS, icon: CheckSquare, adminOnly: true },
  { id: 'utility', label: 'Utility', path: '/utility', icon: Radio },
  { id: 'status', label: 'Status', path: '/status', icon: Activity },
  { id: 'usage', label: 'Usage', path: ROUTES.USAGE, icon: BarChart3 },
  { id: 'audit-trail', label: 'Audit Trail', path: ROUTES.AUDIT_TRAIL, icon: ScrollText },
  { id: 'access-tokens', label: 'Access Tokens', path: ROUTES.ACCESS_TOKENS, icon: KeyRound },
];

const isMenuPathActive = (pathname: string, path: string): boolean =>
  pathname === path || pathname.startsWith(`${path}/`);

export const UserDropdown: React.FC<{ collapsed?: boolean }> = ({
  collapsed = false,
}) => {
  const { user, logout, hasRole } = useAuth();
  const isAdmin = hasRole(UserRoles.ADMIN, true);
  const location = useLocation();
  const displayName = user?.name || "User";

  const visibleItems = useMemo(
    () => USER_MENU_ITEMS.filter((item) => !item.adminOnly || isAdmin),
    [isAdmin],
  );

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      logger.error("Logout error:", error);
    }
  };

  return (
    <DropdownMenu modal={false}>
      <div className={styles.triggerWrapper}>
        <DropdownMenuTrigger asChild>
          <button type="button" className={styles.trigger}>
            <User size={16} className={styles.triggerIcon} />
            {!collapsed && <span className={styles.triggerName}>{displayName}</span>}
          </button>
        </DropdownMenuTrigger>
      </div>

      <DropdownMenuContent align="end" side={collapsed ? "right" : "top"} className={styles.dropdownContent}>
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = isMenuPathActive(location.pathname, item.path);

          return (
            <DropdownMenuItem
              key={item.id}
              className={clsx(styles.menuItem, isActive && styles.menuItemActive)}
              asChild
              aria-current={isActive ? 'page' : undefined}
            >
              <Link to={item.path}>
                <Icon className={styles.menuItemIcon} />
                <span>{item.label}</span>
              </Link>
            </DropdownMenuItem>
          );
        })}

        <DropdownMenuItem
          className={styles.menuItem}
          onClick={handleLogout}
        >
          <LogOut className={styles.menuItemIcon} />
          <span>Logout</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default UserDropdown;
