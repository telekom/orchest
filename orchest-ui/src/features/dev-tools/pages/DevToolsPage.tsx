import { Dialog, DialogContent, DialogHeader, DialogFooter, DialogTitle, DialogDescription } from "@/design-system/components/ui/dialog";
import { MOCK_ROLES_STORAGE_KEY, UserRoles } from "@/shared/auth/models/roles";
import { useAuth } from "@/shared/auth/context/AuthContext";
import { AuthorizationService } from "@/shared/auth/services/AuthorizationService";
import { parseIdTokenRoles } from "@/shared/auth/utils/idTokenRoles";
import { isNoLoginMode } from "@/shared/utils/environmentUtils";
import { StorageUtils } from "@/shared/utils/storageUtils";
import { useEffect, useReducer } from "react";
import {
    NotificationTriggerCard,
    RoleManagementCard,
    UserCard,
} from "../components";
import { useNotificationTester } from "../hooks";
import { DEV_TOOLS_ACTIONS } from "../constants";
import styles from './DevToolsPage.module.css';

const getMockRoles = (): string[] => {
  return StorageUtils.getItem<string[]>(MOCK_ROLES_STORAGE_KEY) || [];
};

const calculateEffectiveRoles = () => {
  const tokenRoles = isNoLoginMode() ? [] : parseIdTokenRoles();
  const mockRoles = getMockRoles();
  const effectiveRoles = AuthorizationService.getEffectiveRoles(tokenRoles, mockRoles);

  return {
    tokenRoles,
    effectiveRoles,
  };
};

const dispatchRoleChangedEvent = (roles: string[]) => {
  window.dispatchEvent(
    new CustomEvent("rolesUpdated", {
      detail: {
        roles,
        source: "devTools",
        timestamp: Date.now(),
      },
    })
  );
};

interface RoleState {
  isAdmin: boolean;
  isViewer: boolean;
  isSensitive: boolean;
  allRoles: string[];
  idTokenRoles: string[];
}

type RoleAction = {
  type: typeof DEV_TOOLS_ACTIONS.UPDATE_ROLES;
  tokenRoles: string[];
  effectiveRoles: string[];
};

const roleReducer = (state: RoleState, action: RoleAction): RoleState => {
  return {
    idTokenRoles: action.tokenRoles,
    allRoles: action.effectiveRoles,
    isAdmin: AuthorizationService.hasExactRole(action.effectiveRoles, UserRoles.ADMIN),
    isViewer: AuthorizationService.hasExactRole(action.effectiveRoles, UserRoles.VIEWER),
    isSensitive: AuthorizationService.hasExactRole(action.effectiveRoles, UserRoles.SENSITIVE),
  };
};

const initialRoleState: RoleState = {
  isAdmin: false,
  isViewer: false,
  isSensitive: false,
  allRoles: [],
  idTokenRoles: [],
};

const DevToolsPage = () => {
  const { user } = useAuth();
  const [roleState, dispatch] = useReducer(roleReducer, initialRoleState);

  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === STORAGE_KEYS.ID_TOKEN) {
        const { tokenRoles, effectiveRoles } = calculateEffectiveRoles();
        dispatch({
          type: DEV_TOOLS_ACTIONS.UPDATE_ROLES,
          tokenRoles,
          effectiveRoles,
        });
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  useEffect(() => {
    const { tokenRoles, effectiveRoles } = calculateEffectiveRoles();
    dispatch({
      type: DEV_TOOLS_ACTIONS.UPDATE_ROLES,
      tokenRoles,
      effectiveRoles,
    });
  }, [user]);

  const toggleRole = (role: UserRoles | string, isEnabled: boolean) => {
    const mockRoles = getMockRoles().filter(
      (r: string) => r !== role && r !== `OVERRIDE_${role}`
    );

    if (isEnabled) {
      mockRoles.push(String(role));
    }

    StorageUtils.setItem(MOCK_ROLES_STORAGE_KEY, mockRoles);
    AuthorizationService.setMockRoles(mockRoles);

    const { tokenRoles, effectiveRoles } = calculateEffectiveRoles();
    dispatch({
      type: DEV_TOOLS_ACTIONS.UPDATE_ROLES,
      tokenRoles,
      effectiveRoles,
    });

    dispatchRoleChangedEvent(effectiveRoles);
  };

  const { triggerNotification, alertProps } = useNotificationTester();

  return (
    <div>
      <header className={styles.header}>
        <h1 className={styles.title}>Dev Tools</h1>
      </header>

      <div className={styles.cardsGrid}>
        <UserCard
          user={user}
          idTokenRoles={roleState.idTokenRoles}
          allRoles={roleState.allRoles}
        />

        <RoleManagementCard
          isAdmin={roleState.isAdmin}
          isViewer={roleState.isViewer}
          isSensitive={roleState.isSensitive}
          onToggleRole={toggleRole}
        />

        <NotificationTriggerCard
          onTriggerNotification={triggerNotification}
        />
      </div>

      <Dialog
        open={alertProps.open}
        onOpenChange={alertProps.onOpenChange}
        dismissOnBackdropClick={false}
      >
        <DialogContent
          headerText={alertProps.title}
          bodyText={alertProps.message}
          showCloseButton={true}
          closeButtonProps={{
            onClick: () => alertProps.onOpenChange(false),
            'aria-label': 'Close'
          }}
          actionSlot={
            <DialogFooter
              variant="sideBySide"
              mainActionProps={{
                label: "OK",
                onClick: () => alertProps.onOpenChange(false),
                variant: "primary",
                size: "small",
              }}
            />
          }
          actionsLayout="sideBySide"
        />
      </Dialog>
    </div>
  );
};

export default DevToolsPage;
