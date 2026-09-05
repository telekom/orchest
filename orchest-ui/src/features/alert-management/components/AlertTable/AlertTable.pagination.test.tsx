import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AlertTable } from './AlertTable';

const mocks = vi.hoisted(() => ({
  dataTable: vi.fn(() => null),
}));

vi.mock('@/shared/components/DataTable/DataTable', () => ({
  DataTable: mocks.dataTable,
}));

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

describe('AlertTable pagination', () => {
  afterEach(() => {
    mocks.dataTable.mockClear();
  });

  it('forwards page-size changes', () => {
    const container = document.createElement('div');
    const root = createRoot(container);
    const onPageSizeChange = vi.fn();

    act(() => {
      root.render(
        <AlertTable
          alerts={[]}
          page={0}
          pageSize={25}
          totalElements={0}
          totalPages={0}
          sort="-count"
          onSelect={vi.fn()}
          onSort={vi.fn()}
          onPageChange={vi.fn()}
          onPageSizeChange={onPageSizeChange}
        />,
      );
    });

    const props = mocks.dataTable.mock.lastCall?.[0];
    props.pagination.onPageSizeChange(50);

    expect(onPageSizeChange).toHaveBeenCalledWith(50);

    act(() => root.unmount());
  });
});
