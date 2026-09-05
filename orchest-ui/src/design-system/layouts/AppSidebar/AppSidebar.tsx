import { useAuth } from "@/shared/auth";
import { NAV_ITEMS } from "@/shared/constants/navigationConfig";
import { useUIStore } from "@/shared/stores/uiStore";
import { hasPermission } from "../utils/navbarUtils";
import clsx from "clsx";
import { ChevronsLeft } from "lucide-react";
import React, { useEffect } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import { UserDropdown } from "../UserDropdown/UserDropdown";
import styles from "./AppSidebar.module.css";
import { OrchLogoBrand } from "@/shared/components/OrchLogo";

export const AppSidebar: React.FC = () => {
  const location = useLocation();
  const { hasRole } = useAuth();
  const collapsed = useUIStore((state) => state.sidebarCollapsed);
  const toggleSidebar = useUIStore((state) => state.toggleSidebar);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === "b") {
        e.preventDefault();
        toggleSidebar();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [toggleSidebar]);

  const visibleItems = NAV_ITEMS.filter((item) => {
    return hasPermission(item.requiredRole, hasRole);
  });

  return (
    <aside className={clsx(styles.sidebar, collapsed && styles.collapsed)}>
      <div className={styles.header}>
        {collapsed ? (
          <button
            type="button"
            className={styles.logo}
            onClick={toggleSidebar}
            title="Expand sidebar"
          >
            <OrchLogoBrand collapsed={collapsed} />
          </button>
        ) : (
          <Link
            to="/dashboard"
            className={styles.logo}
            title="Go to Dashboard"
          >
            <OrchLogoBrand collapsed={collapsed} />
          </Link>
        )}
        {!collapsed && (
          <button
            type="button"
            className={styles.collapseBtn}
            onClick={toggleSidebar}
            aria-label="Collapse sidebar"
          >
            <ChevronsLeft size={16} />
          </button>
        )}
      </div>

      <nav className={styles.nav}>
        {visibleItems.map((item) => {
          const isActive = item.pathMatch(location.pathname);
          const Icon = item.icon;
          return (
            <NavLink
              key={item.href}
              to={item.href}
              className={clsx(styles.navItem, isActive && styles.navItemActive)}
              title={collapsed ? item.name : undefined}
            >
              <Icon size={18} className={styles.navIcon} />
              {!collapsed && (
                <span className={styles.navLabel}>
                  {item.name}
                  {item.badge && <span className={styles.navBadge}>{item.badge}</span>}
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      <div className={styles.footer}>
        <UserDropdown collapsed={collapsed} />
      </div>
    </aside>
  );
};
