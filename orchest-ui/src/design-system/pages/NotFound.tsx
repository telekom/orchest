import { useEffect } from "react";
import { logger } from '@/shared/utils/logger';
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/design-system/components/ui/button";
import styles from './NotFound.module.css';

const NotFound = () => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    logger.error(
      "404 Error: User attempted to access non-existent route:",
      location.pathname
    );
  }, [location.pathname]);

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <h1 className={styles.title}>404</h1>
        <p className={styles.message}>Oops! Page not found</p>
        <Button
          variant="primary"
          size="sm"
          onClick={() => navigate('/processes')}
          label="Return to Processes"
        />
      </div>
    </div>
  );
};

export default NotFound;
