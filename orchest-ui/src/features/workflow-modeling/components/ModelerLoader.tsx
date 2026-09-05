import React from 'react';
import { OrchLogo } from '@/shared/components/OrchLogo';
import { ModelerType, MODELER_TITLES } from '../constants';

interface ModelerLoaderProps {
  type: ModelerType;
}

export const ModelerLoader: React.FC<ModelerLoaderProps> = ({ type }) => {
  const modelerTitle = MODELER_TITLES[type];

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={`Loading ${modelerTitle} Modeler`}
      style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '1rem', height: '100%' }}
    >
      <OrchLogo size={56} />
      <span style={{ fontSize: '0.85rem', color: 'hsl(var(--muted-foreground))' }}>
        Loading {modelerTitle} Modeler...
      </span>
    </div>
  );
};