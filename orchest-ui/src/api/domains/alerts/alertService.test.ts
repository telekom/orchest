import { beforeEach, describe, expect, it, vi } from 'vitest';

const get = vi.fn();
const post = vi.fn();

vi.mock('@/api/core', () => ({
  httpClient: { get, post },
}));

describe('alertService', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
  });

  it('lists alerts with query params under /orchest/alerts', async () => {
    const { alertService } = await import('./alertService');
    get.mockResolvedValue({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 });

    await alertService.listAlerts({
      state: 'FIRING',
      processDefinitionId: 'order-flow',
      createdFrom: '2026-08-01T00:00',
      createdTo: '2026-08-10T23:59',
      timezone: 'Asia/Kolkata',
      page: 0,
      size: 25,
      sort: '-count',
    });

    expect(get).toHaveBeenCalledWith('/orchest/alerts', {
      params: {
        state: 'FIRING',
        processDefinitionId: 'order-flow',
        createdFrom: '2026-07-31T18:30:00.000Z',
        createdTo: '2026-08-10T18:29:00.000Z',
        page: 0,
        size: 25,
        sort: '-count',
      },
    });
  });

  it('fetches firing stats and runs lifecycle actions', async () => {
    const { alertService } = await import('./alertService');
    get.mockResolvedValue([]);
    post.mockResolvedValue({ id: 'a1', state: 'ACKNOWLEDGED' });

    await alertService.getFiringStats();
    expect(get).toHaveBeenCalledWith('/orchest/alerts/stats/firing');

    await alertService.acknowledge('a1', { actor: 'u@example.com' });
    expect(post).toHaveBeenCalledWith('/orchest/alerts/a1/acknowledge', { actor: 'u@example.com' });

    await alertService.silence('a1', { actor: 'u@example.com' });
    expect(post).toHaveBeenCalledWith('/orchest/alerts/a1/silence', { actor: 'u@example.com' });

    await alertService.unmute('a1');
    expect(post).toHaveBeenCalledWith('/orchest/alerts/a1/unmute');

    await alertService.resolve('a1');
    expect(post).toHaveBeenCalledWith('/orchest/alerts/a1/resolve');

    await alertService.getAlert('a1');
    expect(get).toHaveBeenCalledWith('/orchest/alerts/a1');
  });
});
