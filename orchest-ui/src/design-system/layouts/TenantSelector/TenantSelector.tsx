import clsx from 'clsx';
import { Building2, Check } from 'lucide-react';
import React, { useCallback, useEffect, useState } from 'react';
import { tenantService, TENANT_OPTIONS, type TenantId } from '@/shared/services/TenantService';
import styles from './TenantSelector.module.css';

export const TenantSelector: React.FC = () => {
  const [activeTenant, setActiveTenant] = useState<TenantId>(tenantService.getTenant());
  const [open, setOpen] = useState(false);

  useEffect(() => {
    return tenantService.subscribe(setActiveTenant);
  }, []);

  const handleSelect = useCallback((value: TenantId) => {
    tenantService.setTenant(value);
    setOpen(false);
    window.location.reload();
  }, []);

  const activeOption = TENANT_OPTIONS.find(o => o.value === activeTenant);

  return (
    <div className={styles.container}>
      <button
        type="button"
        className={styles.trigger}
        onClick={() => setOpen(!open)}
        aria-expanded={open}
      >
        <Building2 size={13} className={styles.triggerIcon} />
        <span className={styles.triggerLabel}>{activeOption?.label ?? 'All Tenants'}</span>
        <span className={styles.chevron}>&#9662;</span>
      </button>

      {open && (
        <>
          <div className={styles.backdrop} onClick={() => setOpen(false)} />
          <div className={styles.dropdown}>
            {TENANT_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                className={clsx(styles.option, activeTenant === option.value && styles.optionActive)}
                onClick={() => handleSelect(option.value)}
              >
                <span className={styles.optionLabel}>{option.label}</span>
                {activeTenant === option.value && <Check size={14} className={styles.checkIcon} />}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
};
