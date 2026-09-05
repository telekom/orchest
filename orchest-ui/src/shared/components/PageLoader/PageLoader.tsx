import { OrchLogo } from '@/shared/components/OrchLogo';
import styles from './PageLoader.module.css';

export const PageLoader: React.FC = () => (
  <div className={styles.container}>
    <OrchLogo size={64} />
  </div>
);