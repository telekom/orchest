# OrchesT UI

Enterprise-grade React dashboard for BPMN process orchestration and DMN decision management with real-time monitoring and AI-powered workflow generation.

## Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm run dev              # http://localhost:8080

# Build for production
npm run build            # Production build
npm run build:dev        # Development environment
npm run build:uat        # UAT environment
npm run build:beta       # Beta environment

# Linting
npm run lint             # Check code quality
npm run lint -- --fix    # Auto-fix issues
```

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite
- **Routing**: React Router 7
- **State Management**: TanStack Query (server) + Zustand (UI)
- **Styling**: CSS Modules (100% - Tailwind removed)
- **UI Components**: Radix UI primitives
- **Diagrams**: bpmn-js + dmn-js
- **Authentication**: Azure AD / MSAL

## Project Structure

```
src/
├── api/                    # API layer (services, types)
├── features/               # Domain features (process, decision, workflow)
├── design-system/          # UI components & layouts
├── shared/                 # Cross-cutting utilities
└── router/                 # Route configuration
```

## Key Concepts

### API Services
All API services extend `BaseApiService` and are singletons:
```typescript
import { processInstanceService } from '@/api/domains';
const instances = await processInstanceService.getAll();
```

### Data Fetching
Always use `useApiQuery` and `useApiMutation` (never raw TanStack Query):
```typescript
import { useApiQuery } from '@/shared/hooks';

const { data, isLoading } = useApiQuery(
  ['processes'],
  () => processInstanceService.getAll(),
  { staleTime: 30000 }
);
```

### Styling
CSS Modules exclusively - no Tailwind utility classes:
```typescript
import styles from './Component.module.css';
<div className={styles.container}>...</div>
```

### Path Aliases
Use path aliases everywhere:
```typescript
import { Button } from '@/design-system/components';
import { useApiQuery } from '@/shared/hooks';
```

## Environment Setup

Create `.env` file:
```bash
VITE_BASEPATH=http://localhost:8080
VITE_AI_API_URL=http://localhost:8000
VITE_SSO_CLIENT_ID=your-azure-ad-client-id
VITE_SSO_TENANT_ID=your-azure-ad-tenant-id
VITE_BYPASS_LOGIN=true  # Local dev only
```

## Architecture Guidelines

1. **State Management**
   - Server state → TanStack Query via `useApiQuery`
   - Global UI → Zustand store
   - Local state → `useState`

2. **Component Rules**
   - Max 300 lines (split if larger)
   - Lazy load heavy components (BPMN/DMN viewers)
   - All props must have TypeScript interfaces
   - Every component needs a `.module.css` file

3. **Constants**
   - Shared constants → `/src/shared/constants/`
   - Feature constants → `/src/features/<feature>/constants/`
   - Component constants → Inline (< 5 lines only)

4. **TypeScript**
   - Strict mode enabled
   - No `any` types (use `unknown`)
   - No unused imports/variables

## Routes

- `/processes` - Process instances list
- `/processes/:id` - Process instance details
- `/decisions` - Decision instances list
- `/decisions/:id` - Decision instance details
- `/modeler` - BPMN/DMN modeler
- `/generator` - AI BPMN generator
- `/settings` - Orchestration settings

## Development Workflow

1. **Create a feature**
   ```
   src/features/<feature-name>/
   ├── components/     # Feature components
   ├── pages/          # Route components
   ├── hooks/          # Custom hooks
   ├── types/          # TypeScript types
   └── constants/      # Feature constants
   ```

2. **Add API service**
   ```typescript
   // src/api/domains/<domain>/service.ts
   export class MyService extends BaseApiService<DTO, Params> {
     protected baseUrl = "/api";
     protected resourcePath = "resources";
   }
   export const myService = new MyService();
   ```

3. **Create component**
   ```
   src/design-system/components/<Component>/
   ├── Component.tsx
   └── Component.module.css
   ```

## Authentication

Azure AD / MSAL integration with automatic:
- Bearer token injection
- Token refresh on 401
- Request retry with exponential backoff
- Request ID and timestamp headers

## Deployment

CI/CD via GitLab with Helm:
- **Environments**: dev, teststable, uat, prod-Ref, beta, prod
- **Docker**: Multi-stage builds
- **Security**: SAST, dependency scanning, secret detection

## Comprehensive Guide

For detailed architecture, patterns, conventions, and best practices, see **[CLAUDE.md](./CLAUDE.md)** - the complete developer guide.

## Contributing

1. Follow patterns in CLAUDE.md
2. Use CSS Modules (no Tailwind)
3. Path aliases everywhere (`@/` not `../`)
4. All services extend `BaseApiService`
5. All data fetching via `useApiQuery`
6. TypeScript strict mode
7. Components < 300 lines

## License

Proprietary - Mercedes-Benz Tech Innovation
