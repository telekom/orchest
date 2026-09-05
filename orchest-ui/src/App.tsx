import { Toaster as Sonner } from "@/design-system/components/ui/sonner";
import { TooltipProvider } from "@/design-system/components/ui/tooltip/tooltip";
import { AuthProvider } from "@/shared/auth";
import { InactivityGuard } from "@/shared/auth/components/InactivityGuard/InactivityGuard";
import { AmbientBackground, MotionProvider } from "@/shared/components";
import { environment } from "@/shared/constants/environment";
import { DiagramProvider } from "@/shared/context/DiagramContext";
import { ErrorBoundary } from "@/shared/error/ErrorBoundary";
import { OnboardingProvider } from "@/shared/onboarding";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthenticatedApp } from "./router/AuthenticatedApp";
import { createAppRouter } from "./router/router";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 0,
      gcTime: 5 * 60 * 1000,
      refetchOnMount: true,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      notifyOnChangeProps: ['data', 'error'],
      structuralSharing: true,
      retry: (failureCount, error: unknown) => {
        if (error && typeof error === "object" && "response" in error) {
          const response = (error as { response?: { status?: number } }).response;
          if (response?.status && response.status >= 400 && response.status < 500) {
            return false;
          }
        }
        return failureCount < 3;
      },
    }
  },
});

const router = createAppRouter();

function App() {
  return (
    <ErrorBoundary level="app">
      <MotionProvider>
        <AmbientBackground />
        <QueryClientProvider client={queryClient}>
          <TooltipProvider>
            <DiagramProvider>
              <AuthProvider>
                  {!environment.bypassLogin && (
                    <InactivityGuard timeoutMinutes={60} warningMinutes={2} />
                  )}
                  <OnboardingProvider>
                    <AuthenticatedApp router={router} />
                  </OnboardingProvider>
                  <Sonner position="top-right" offset="72px" richColors closeButton />
              </AuthProvider>
            </DiagramProvider>
          </TooltipProvider>
        </QueryClientProvider>
      </MotionProvider>
    </ErrorBoundary>
  );
}

export default App;
