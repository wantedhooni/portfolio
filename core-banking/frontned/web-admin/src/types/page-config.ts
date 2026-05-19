import type { ColDef } from 'ag-grid-community';
import type { SearchField } from '@/components/data-grid/SearchBar';

export type EditMode = 'edit' | 'delete';

export interface CrudField {
  key: string;
  label: string;
  type?: 'text' | 'email' | 'password' | 'number';
  placeholder?: string;
  required?: boolean;
  validate?: (value: string) => string | undefined;
}

// ── 목록 페이지 설정 ──────────────────────────────────────────
export interface PageConfig<
  TItem = Record<string, unknown>,
  TSearch = Record<string, string>,
> {
  endpoint: string;
  title: string;
  addLabel?: string;
  modalTitle?: string;

  searchFields?: SearchField<TSearch>[];
  initialSearch?: TSearch;

  // - ColDef[]  : 정적 컬럼 (읽기 전용)
  // - Function  : onEdit / onDelete / onDetail 콜백을 받아 액션 컬럼 포함
  columnDefs:
    | ColDef[]
    | ((
        onEdit: (item: TItem) => void,
        onDelete: (item: TItem) => void,
        onDetail?: (item: TItem) => void,
      ) => ColDef[]);

  crudFields?: CrudField[];
  pageSize?: number;

  // 상세 페이지 경로 (설정 시 상세보기 버튼 활성화)
  // 예) '/dashboard/admin' → 클릭 시 /dashboard/admin/{id} 이동
  detailBasePath?: string;
}

// ── 상세 페이지 설정 ──────────────────────────────────────────
export interface DetailField {
  key: string;
  label: string;
}

export interface DetailPageConfig {
  endpoint: string;       // GET /endpoint/{id}
  title: string;
  listPath: string;       // 뒤로가기 경로
  fields?: DetailField[]; // 표시할 필드 (없으면 전체 표시)
}
