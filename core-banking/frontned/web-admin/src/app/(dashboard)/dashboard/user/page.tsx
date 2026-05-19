/**
 * ================================================================
 *  새 CRUD 페이지 복사용 템플릿
 * ================================================================
 *
 *  [사용법] 아래 두 파일을 복사하고 TODO 항목만 채우세요
 *
 *  1. config.ts  → src/app/(dashboard)/dashboard/<이름>/config.ts
 *  2. page.tsx   → src/app/(dashboard)/dashboard/<이름>/page.tsx
 *
 *  [필수 수정 항목]
 *  config.ts 에서 TODO 5개만 채우면 페이지 완성
 *
 * ================================================================
 */

// ──────────────────────────────────────────────────────────────
//  config.ts  ← 이 블록을 config.ts 파일로 저장
// ──────────────────────────────────────────────────────────────
/*

import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

// TODO 1. 아이템 타입 (백엔드 응답 필드와 일치)
export interface SampleItem {
  id: number;
  name: string;
  // email: string;
}

// TODO 2. 검색 파라미터 타입
interface SampleSearch {
  name: string;
}

export const sampleConfig: PageConfig<SampleItem, SampleSearch> = {

  // TODO 3. API 엔드포인트
  endpoint: '/api/v1/sample',

  // TODO 4. 레이블
  title: '샘플 관리',
  addLabel: '샘플 등록',
  modalTitle: '샘플',

  // ── 검색바 필드 (없애려면 두 줄 삭제)
  initialSearch: { name: '' },
  searchFields: [
    { key: 'name', label: '이름', placeholder: '이름 검색' },
  ],

  // ── 테이블 컬럼
  // 수정/삭제 불필요 → ColDef[] 직접 작성
  // 수정/삭제 필요  → (onEdit, onDelete) => [..., createActionColumn(onEdit, onDelete)]
  columnDefs: (onEdit, onDelete): ColDef[] => [
    { field: 'id',   headerName: 'ID',   maxWidth: 80 },
    { field: 'name', headerName: '이름', flex: 1 },
    createActionColumn(onEdit, onDelete),
  ],

  // TODO 5. CRUD 폼 필드 (없애려면 crudFields 줄 삭제 → 읽기 전용 페이지)
  crudFields: [
    {
      key: 'name',
      label: '이름',
      placeholder: '이름 입력',
      // required: true  (기본값)
      // validate: (v) => v.length >= 2 ? undefined : '2자 이상 입력하세요.',
    },
    // {
    //   key: 'email',
    //   label: '이메일',
    //   type: 'email',
    //   validate: (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
    //     ? undefined : '이메일 형식이 아닙니다.',
    // },
    // {
    //   key: 'password',
    //   label: '비밀번호',
    //   type: 'password',
    //   validate: (v) => v.length >= 6 ? undefined : '6자 이상 입력하세요.',
    // },
  ],
};

*/

// ──────────────────────────────────────────────────────────────
//  page.tsx  ← 이 블록을 page.tsx 파일로 저장 (수정 불필요)
// ──────────────────────────────────────────────────────────────

'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { userConfig } from '@/features/user/config'
// TODO: config import 경로를 실제 파일에 맞게 수정
// import { sampleConfig } from './config';

// export default function SamplePage() {
//   return <CrudPage config={sampleConfig} />;
// }

export default function UserPage() {
  return <CrudPage config={userConfig} />;
}
