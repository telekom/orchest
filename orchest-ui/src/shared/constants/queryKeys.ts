export const queryKeys = {
  processDefinitions: {
    all: ['process-definitions'] as const,
    list: (params: { size: number }) => ['process-definitions-list', params] as const,
    detail: (id: string, version: number) => ['process-definition', id, version] as const,
  },
  orchest: {
    /** GET `/orchest/stats` — consolidated process + decision stats for dashboard */
    stats: (
      slice: { from?: string; to?: string; timeZone?: string } | null,
    ) =>
      slice == null
        ? (['orchest', 'stats', 'all'] as const)
        : ([
            'orchest',
            'stats',
            slice.from ?? '',
            slice.to ?? '',
            slice.timeZone ?? '',
          ] as const),
  },
  processInstances: {
    all: ['processInstances'] as const,
    list: (filters: {
      process: string | null;
      version: string | null;
      searchText: string;
      status: string | null;
      from: string | null;
      to: string | null;
      timezone?: string | null;
      page: number;
      size: number;
      sort?: string;
    }) => [
      'processInstances',
      filters.process,
      filters.version,
      filters.searchText,
      filters.status,
      filters.from,
      filters.to,
      filters.timezone ?? null,
      filters.page,
      filters.size,
      filters.sort,
    ] as const,
    detail: (id: string) => ['processInstance', id] as const,
    diagram: (processDefinitionId: string) => ['processDefinition', 'diagram', processDefinitionId] as const,
    tasks: (id: string) => ['processInstance', id, 'tasks'] as const,
    variables: (id: string) => ['processInstance', id, 'variables'] as const,
    stats: () => ['processInstanceStats'] as const,
  },
  decisionDefinitions: {
    all: ['decision-definitions'] as const,
    list: (params: { page: number; size: number }) => ['decision-definitions', params] as const,
    detail: (id: string, version: number) => ['decision-definition', id, version] as const,
  },
  decisionInstances: {
    all: ['decisionInstances'] as const,
    list: (filters: {
      decisionId: string | null;
      version: string | null;
      status: string | null;
      searchText: string | null;
      from: string | null;
      to: string | null;
      page: number;
      size: number;
    }) => [
      'decisionInstances',
      {
        decisionId: filters.decisionId,
        version: filters.version,
        status: filters.status,
        searchText: filters.searchText,
        from: filters.from,
        to: filters.to,
      },
      filters.page,
      filters.size,
    ] as const,
    scroll: (filters: {
      decisionId: string | null;
      version: string | null;
      status: string | null;
      searchText: string | null;
      from: string | null;
      to: string | null;
      timezone?: string | null;
      pageSize: number;
      sort?: string;
    }) => [
      'decisionInstances',
      'scroll',
      {
        decisionId: filters.decisionId,
        version: filters.version,
        status: filters.status,
        searchText: filters.searchText,
        from: filters.from,
        to: filters.to,
        timezone: filters.timezone ?? null,
        pageSize: filters.pageSize,
        sort: filters.sort,
      },
    ] as const,
    detail: (id: string) => ['decisionInstance', id] as const,
    xml: (decisionId: string, version: string) => ['decision-xml', decisionId, version, 'v1'] as const,
  },
  deploymentApprovals: {
    all: ['deployment-approvals'] as const,
    reviews: () => ['deployment-approvals', 'reviews'] as const,
    ownedProcesses: (owner: string) => ['owned-processes', owner] as const,
    definitions: () => ['process-definitions-by-token'] as const,
  },
  connectors: {
    all: () => ['connectors'] as const,
  },
  rateLimits: {
    all: () => ['rateLimits'] as const,
  },
  userTasks: {
    all: ['userTasks'] as const,
    list: (params?: Record<string, unknown>) => ['userTasks', 'list', params] as const,
    detail: (id: string) => ['userTask', id] as const,
    byProcess: (processInstanceId: string) => ['userTasks', 'byProcess', processInstanceId] as const,
  },
  alerts: {
    all: ['alerts'] as const,
    list: (params: Record<string, unknown>) => ['alerts', 'list', params] as const,
    detail: (id: string) => ['alerts', 'detail', id] as const,
    firingStats: () => ['alerts', 'firing-stats'] as const,
    stateCount: (params: Record<string, unknown>) => ['alerts', 'state-count', params] as const,
  },
} as const;
