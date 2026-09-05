import React from 'react';
import styles from './StatusCode.module.css';

interface StatusCodeProps {
  code: number;
}

function getStatusClass(code: number): string {
  if (code >= 200 && code < 300) return styles.success;
  if (code >= 300 && code < 400) return styles.redirect;
  if (code >= 400 && code < 500) return styles.clientError;
  if (code >= 500) return styles.serverError;
  return styles.unknown;
}

const StatusCode: React.FC<StatusCodeProps> = ({ code }) => {
  return (
    <span className={`${styles.status} ${getStatusClass(code)}`}>
      <span className={styles.dot} />
      {code}
    </span>
  );
};

export default React.memo(StatusCode);