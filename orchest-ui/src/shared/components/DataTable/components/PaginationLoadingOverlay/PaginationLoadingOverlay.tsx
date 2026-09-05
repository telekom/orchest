import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import styles from './PaginationLoadingOverlay.module.css';

interface PaginationLoadingOverlayProps {
  columns: number;
  rows?: number;
}

export const PaginationLoadingOverlay: React.FC<PaginationLoadingOverlayProps> = (_props) => {
  return (
    <div className={styles.overlay}>
      <SpinnerLoader size="lg" />
    </div>
  );
};
