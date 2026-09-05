import React from 'react';
import styles from './MethodBadge.module.css';

interface MethodBadgeProps {
  method: string;
}

const METHOD_CLASSES: Record<string, string> = {
  GET: styles.get,
  POST: styles.post,
  PUT: styles.put,
  PATCH: styles.patch,
  DELETE: styles.delete,
};

const MethodBadge: React.FC<MethodBadgeProps> = ({ method }) => {
  const upper = method.toUpperCase();
  const variantClass = METHOD_CLASSES[upper] || styles.default;

  return (
    <span className={`${styles.badge} ${variantClass}`}>
      {upper}
    </span>
  );
};

export default React.memo(MethodBadge);