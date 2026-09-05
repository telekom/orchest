export type TenantId = 'MOBILE' | 'FIXED' | 'OTT';

export const TENANT_OPTIONS: { value: TenantId; label: string }[] = [
  { value: 'MOBILE', label: 'Mobile' },
  { value: 'FIXED', label: 'Fixed' },
  { value: 'OTT', label: 'OTT' },
];

const STORAGE_KEY = 'orchest-active-tenant';

class TenantService {
  private tenant: TenantId;
  private listeners: Array<(tenant: TenantId) => void> = [];

  constructor() {
    const stored = localStorage.getItem(STORAGE_KEY) as TenantId | null;
    this.tenant = stored && TENANT_OPTIONS.some(o => o.value === stored) ? stored : 'MOBILE';
  }

  getTenant(): TenantId {
    return this.tenant;
  }

  setTenant(tenant: TenantId) {
    this.tenant = tenant;
    localStorage.setItem(STORAGE_KEY, tenant);
    this.listeners.forEach(fn => fn(tenant));
  }

  subscribe(fn: (tenant: TenantId) => void): () => void {
    this.listeners.push(fn);
    return () => {
      this.listeners = this.listeners.filter(l => l !== fn);
    };
  }
}

export const tenantService = new TenantService();
