# Task Management Feature

## Overview

The Task Management feature provides a comprehensive UI for managing BPMN User Tasks in OrchesT. It enables users to view, claim, complete, and manage tasks assigned to them or their candidate groups.

## Features

- **Task Queue**: View all available tasks with filtering and sorting
- **Task Details**: View task information, forms, and variables
- **Task Actions**: Claim, unclaim, complete, and reassign tasks
- **Auto-navigate**: Automatically navigates to the next task after completing one
- **Process Integration**: View process diagram with highlighted task activity and navigate to process instances
- **BPMN Diagram**: Interactive process diagram showing the current task's position in the workflow
- **Browser Notifications**: Receive notifications when new tasks are assigned to you
- **Keyboard Shortcuts**: Navigate and manage tasks using keyboard (j/k, Ctrl+C, Ctrl+Enter, etc.)
- **Accessibility**: Full ARIA labels, semantic HTML, and screen reader support
- **Responsive Design**: Mobile-friendly split-view layout

## Directory Structure

```
src/features/task-management/
├── components/          # Feature-specific components
│   ├── TaskTile/       # Task card in queue
│   ├── TaskPriorityBadge/  # Priority label badge
│   ├── TaskFilter/     # Filter controls and builder
│   ├── TaskQueue/      # Task list with sorting
│   ├── TaskDetailsPanel/   # Task details and content
│   ├── TaskAssignmentControls/  # Claim/unclaim/assign buttons
│   ├── TaskForm/       # Camunda form renderer (placeholder)
│   ├── TaskVariablesEditor/  # Variable table editor
│   └── TaskProcessDiagram/   # BPMN diagram viewer (implemented)
├── pages/              # Route components
│   └── TasksPage/      # Main tasks page with split layout
├── hooks/              # Feature hooks
│   ├── useUserTasks    # Query all tasks
│   ├── useUserTask     # Query single task
│   ├── useClaimTask    # Mutation: claim task
│   ├── useUnclaimTask  # Mutation: unclaim task
│   ├── useCompleteTask # Mutation: complete task
│   ├── useAssignTask   # Mutation: reassign task
│   └── useTaskNotifications  # Browser notifications (implemented)
├── types/              # TypeScript types
│   └── index.ts        # Filter, state, and data types
├── constants/          # Feature constants
│   ├── taskState.ts    # Task state enum
│   ├── taskPriority.ts # Priority mappings
│   ├── filterOptions.ts # Filter and sort options
│   ├── messages.ts     # Toast messages
│   ├── storageKeys.ts  # localStorage keys
│   └── index.ts        # Barrel exports
└── README.md           # This file
```

## API Integration

The feature uses the `UserTaskService` from `@/api/domains/user-tasks`:

- `GET /orchest/userTasks` - List all tasks
- `GET /orchest/userTasks/{taskId}` - Get task details
- `GET /orchest/userTasks/process/{processInstanceId}` - Get tasks for process
- `POST /orchest/userTasks/{taskId}/claim` - Claim task
- `POST /orchest/userTasks/{taskId}/unclaim` - Unclaim task
- `POST /orchest/userTasks/{taskId}/complete` - Complete task
- `PATCH /orchest/userTasks/{taskId}/assign` - Reassign task

## Data Flow

1. **Data Fetching**: All hooks use `useApiQuery` and `useApiMutation` wrappers
2. **Server State**: Managed by TanStack Query (no local state duplication)
3. **UI State**: Filters, sort, and preferences stored in localStorage
4. **Optimistic Updates**: Mutations use optimistic UI for instant feedback

## Usage Examples

### Using Hooks in Components

```typescript
import { useUserTasks, useClaimTask } from '@/features/task-management/hooks';

function MyComponent() {
  const { data: tasks, isLoading } = useUserTasks({ state: 'CREATED' });
  const claimMutation = useClaimTask();

  const handleClaim = (taskId: string) => {
    claimMutation.mutate(taskId);
  };

  // ...
}
```

### Importing Constants

```typescript
import {
  TaskState,
  TASK_MESSAGES,
  DEFAULT_FILTERS,
  getPriorityLabel,
} from '@/features/task-management/constants';

const label = getPriorityLabel(75); // 'High'
```

## Styling

All components use **CSS Modules exclusively** (no Tailwind). Each component has its own `.module.css` file:

```typescript
import styles from './TaskTile.module.css';

<div className={styles.container}>
  <h3 className={styles.title}>{task.taskName}</h3>
</div>
```

## State Management

- **Server state**: TanStack Query via `useApiQuery`/`useApiMutation`
- **Filter preferences**: localStorage via `TASK_STORAGE_KEYS`
- **Notification permissions**: localStorage + browser API
- **No Zustand store**: Feature is self-contained

## Performance Optimizations

✅ **Implemented:**
- Lazy loading of heavy components (BPMN diagram, planned)
- React.memo() on TaskQueue, TaskFilter, TaskTile, TaskPriorityBadge, TaskAssignmentControls, TaskVariablesEditor
- useMemo() for computed values (variableEntries in editor)
- useCallback() for all event handlers in TasksPage and components
- Query staleTime tuning (30s for lists, 10s for details, 20s for process tasks)

## Accessibility

✅ **Fully Implemented:**
- Semantic HTML (button, listbox, option roles)
- ARIA labels for all interactive elements (tasks, buttons, controls)
- Keyboard shortcuts for navigation and actions:
  - `j` - Next task
  - `k` - Previous task
  - `/` - Focus search
  - `?` - Show help
- Screen reader support with proper roles and live regions
- Focus management (roving tabindex in task list)
- WCAG AA contrast compliance

## Responsive Design

- **Mobile** (<768px): Stacked layout (queue above, details below)
- **Tablet** (768-1024px): Side-by-side with adjustable split
- **Desktop** (>1024px): Optimized split view

## Browser Support

- Chrome, Firefox, Safari, Edge (latest versions)
- Mobile: iOS Safari, Chrome
- Requires ES2020+ support

## Related Documentation

- [API Service Pattern](../../api/core/README.md)
- [useApiQuery Hook](../../shared/hooks/README.md)
- [CSS Modules Guide](../../design-system/README.md)
- [CLAUDE.md Project Guidelines](../../../CLAUDE.md)
