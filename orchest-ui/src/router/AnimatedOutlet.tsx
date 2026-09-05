import { PageTransition } from '@/shared/components';
import { Outlet, useLocation } from 'react-router-dom';

/** Wraps route outlet with fade+slide page transition. */
export function AnimatedOutlet() {
  const location = useLocation();

  return (
    <PageTransition key={location.pathname}>
      <Outlet />
    </PageTransition>
  );
}
