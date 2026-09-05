import { FlaskConical, GitBranch, LayoutDashboard, ListTodo, LucideIcon, PenTool, Settings, Sparkles, Workflow } from 'lucide-react';
import { UserRoles } from '../auth/models/roles';
import { ROUTES } from './routes';

export interface NavItem {
  name: string;
  icon: LucideIcon;
  href: string;
  pathMatch: (path: string) => boolean;
  /** Optional — omit to make the item visible to every authenticated user. */
  requiredRole?: string;
  /** Optional badge label shown next to the nav item name. */
  badge?: string;
}

export const NAV_ITEMS: NavItem[] = [
  {
    name: 'Dashboard',
    icon: LayoutDashboard,
    href: ROUTES.DASHBOARD,
    pathMatch: (path: string) => path === ROUTES.DASHBOARD || path === '/dashboard',
  },
  {
    name: 'Processes',
    icon: Workflow,
    href: ROUTES.PROCESSES,
    pathMatch: (path: string) => path === ROUTES.PROCESSES || path.startsWith('/processes/'),
    requiredRole: UserRoles.SENSITIVE,
  },
  {
    name: 'Decisions',
    icon: GitBranch,
    href: ROUTES.DECISIONS,
    pathMatch: (path: string) => path === ROUTES.DECISIONS || path.startsWith('/decisions/'),
    requiredRole: UserRoles.SENSITIVE,
  },
  {
    name: 'Tasks',
    icon: ListTodo,
    href: ROUTES.TASKS,
    pathMatch: (path: string) => path === ROUTES.TASKS || path.startsWith('/tasks/'),
    requiredRole: UserRoles.ADMIN,
  },
  {
    name: 'Settings',
    icon: Settings,
    href: ROUTES.SETTINGS,
    pathMatch: (path: string) => path === ROUTES.SETTINGS,
    requiredRole: UserRoles.SENSITIVE,
  },
  {
    name: 'Modeler',
    icon: PenTool,
    href: ROUTES.MODELER,
    pathMatch: (path: string) => path === ROUTES.MODELER || path.startsWith('/modeler/'),
    requiredRole: UserRoles.VIEWER,
  },
  {
    name: 'AI',
    icon: Sparkles,
    href: ROUTES.AI_CHAT,
    pathMatch: (path: string) => path === ROUTES.AI_CHAT,
    badge: 'Beta',
  },
  {
    name: 'FEEL Playground',
    icon: FlaskConical,
    href: ROUTES.FEEL_PLAYGROUND,
    pathMatch: (path: string) => path === ROUTES.FEEL_PLAYGROUND,
  },
];
