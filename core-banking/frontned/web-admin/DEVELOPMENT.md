# Admin Panel — 개발 가이드

## 기술 스택

| 영역 | 기술 |
|---|---|
| Framework | Next.js 16 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS |
| Data Table | ag-Grid v35 (Infinite Row Model) |
| HTTP Client | Axios (interceptor 기반 자동 갱신) |
| Auth | JWT — 두 토큰 모두 쿠키 저장 |

---

## 프로젝트 구조

```
src/
├── app/
│   ├── (auth)/login/            # 로그인 페이지
│   └── (dashboard)/dashboard/   # 대시보드 페이지들
│       ├── admin/               # config.ts + page.tsx + [id]/page.tsx
│       ├── account/
│       └── account-transaction/
│
├── components/
│   ├── data-grid/               # 핵심 공통 컴포넌트
│   │   ├── CrudPage.tsx         # CRUD 페이지 전체 조립
│   │   ├── PageTemplate.tsx     # 헤더 + 검색 + 그리드 레이아웃
│   │   ├── DataGrid.tsx         # ag-Grid 래퍼
│   │   ├── SearchBar.tsx        # 검색 폼
│   │   ├── CreateModal.tsx      # 등록 모달
│   │   ├── EditModal.tsx        # 수정/삭제 모달
│   │   ├── ModalShell.tsx       # 모달 공통 껍데기 (backdrop + header)
│   │   ├── FormRenderer.tsx     # 필드 배열 → 입력 폼 렌더링
│   │   ├── DetailPage.tsx       # 상세 페이지
│   │   ├── action-column.tsx    # 그리드 액션 버튼 컬럼
│   │   └── form-utils.ts        # buildEmptyForm / validateFields
│   └── layout/
│       ├── AuthGuard.tsx        # 인증 상태 가드
│       └── Sidebar.tsx          # 사이드바
│
├── hooks/
│   ├── useAuthInit.ts           # 페이지 로드 시 인증 초기화
│   ├── useCrudModal.ts          # 등록/수정/삭제 모달 상태
│   └── useTableData.ts          # 그리드 데이터 패칭 + 검색 + 갱신
│
├── lib/
│   ├── api.ts                   # Axios 인스턴스 + 401 자동 갱신 인터셉터
│   ├── authStore-local.ts       # 클라이언트 쿠키 유틸
│   └── authStore-server.ts      # 서버 컴포넌트용 쿠키 읽기
│
├── types/
│   ├── index.ts                 # API 응답/요청 공통 타입
│   └── page-config.ts           # PageConfig / DetailPageConfig / CrudField
│
└── templates/
    └── NewPage.template.tsx     # 새 페이지 복사용 템플릿
```

---

## 새 CRUD 페이지 추가하기

**`config.ts`와 `page.tsx` 두 파일만 만들면 완성됩니다.**

### 1. config.ts 작성

```ts
// src/app/(dashboard)/dashboard/product/config.ts
import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface ProductItem {
  id: number;
  name: string;
  price: number;
}

interface ProductSearch {
  name: string;
}

export const productConfig: PageConfig<ProductItem, ProductSearch> = {
  endpoint: '/api/v1/product',
  title: '상품 관리',
  addLabel: '상품 등록',
  modalTitle: '상품',
  detailBasePath: '/dashboard/product', // 상세 페이지 사용 시

  initialSearch: { name: '' },
  searchFields: [
    { key: 'name', label: '상품명', placeholder: '상품명 입력' },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id',    headerName: 'ID',   maxWidth: 80 },
    { field: 'name',  headerName: '상품명' },
    { field: 'price', headerName: '가격' },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    { key: 'name',  label: '상품명', placeholder: '상품명 입력' },
    { key: 'price', label: '가격',   type: 'number' },
  ],
};

// 상세 페이지가 필요한 경우
export const productDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/product',
  title: '상품 상세',
  listPath: '/dashboard/product',
};
```

### 2. page.tsx 작성 (3줄)

```tsx
// src/app/(dashboard)/dashboard/product/page.tsx
'use client';
import CrudPage from '@/components/data-grid/CrudPage';
import { productConfig } from './config';
export default function ProductPage() { return <CrudPage config={productConfig} />; }
```

### 3. 상세 페이지 추가 (선택)

```tsx
// src/app/(dashboard)/dashboard/product/[id]/page.tsx
import DetailPage from '@/components/data-grid/DetailPage';
import { productDetailConfig } from '../config';

export default async function ProductDetailPage({
  params,
}: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <DetailPage config={productDetailConfig} id={id} />;
}
```

### 4. 사이드바 메뉴 추가

```tsx
// src/components/layout/Sidebar.tsx
const NAV_ITEMS = [
  ...
  { href: '/dashboard/product', label: '상품 관리' },
];
```

---

## 주요 타입

### `PageConfig<TItem, TSearch>`

| 필드 | 타입 | 설명 |
|---|---|---|
| `endpoint` | `string` | API 기본 경로 |
| `title` | `string` | 페이지 제목 |
| `addLabel` | `string?` | 등록 버튼 레이블 (기본: `'등록'`) |
| `modalTitle` | `string?` | 모달 제목 (기본: `title`) |
| `detailBasePath` | `string?` | 상세 경로 (없으면 상세 버튼 미노출) |
| `searchFields` | `SearchField[]?` | 검색 필드 |
| `initialSearch` | `TSearch?` | 검색 초기값 |
| `columnDefs` | `ColDef[] \| Function` | 그리드 컬럼 |
| `crudFields` | `CrudField[]?` | 등록/수정 폼 필드 |
| `pageSize` | `number?` | 페이지 크기 (기본: 20) |

### `CrudField`

```ts
interface CrudField {
  key: string;
  label: string;
  type?: 'text' | 'email' | 'password' | 'number'; // 기본: 'text'
  placeholder?: string;
  required?: boolean;                               // 기본: true
  validate?: (value: string) => string | undefined; // 커스텀 검증
}
```

---

## 인증 흐름

### 토큰 저장 위치

| 토큰 | 저장 위치 |
|---|---|
| Access Token | `accessToken` 쿠키 |
| Refresh Token | `refreshToken` 쿠키 |

### 클라이언트에서 토큰 읽기/쓰기

```ts
import { COOKIE_KEYS, getToken, setToken, clearTokens } from '@/lib/authStore-local';

getToken(COOKIE_KEYS.accessToken)        // access token 읽기
setToken(COOKIE_KEYS.accessToken, value) // 쿠키에 저장
clearTokens()                            // 두 토큰 모두 삭제 (로그아웃)
```

### 서버 컴포넌트에서 토큰 읽기

```ts
import { getServerTokens } from '@/lib/authStore-server';

const { accessToken, refreshToken } = await getServerTokens();
```

### 401 자동 갱신 흐름

```
API 요청 → 401 응답
  → callRefresh(refreshToken) 호출
    → 성공: 새 토큰 쿠키 저장 + 실패 요청 자동 재시도
    → 실패: clearTokens() + /login 리다이렉트
```

동시에 여러 요청이 401을 받으면 `pendingQueue`에 대기시키고 토큰 갱신이 완료되면 일괄 재시도합니다.

---

## 공통 컴포넌트

### `createActionColumn`

그리드 액션 버튼(상세/수정/삭제)을 만드는 헬퍼.

```ts
// 상세 없이 수정 + 삭제만
createActionColumn(onEdit, onDelete)

// 상세 + 수정 + 삭제
createActionColumn(onEdit, onDelete, onDetail)
```

### `ModalShell`

모달 공통 껍데기. 커스텀 모달이 필요할 때 직접 사용.

```tsx
<ModalShell title="상품 등록" onClose={onClose}>
  {/* 폼 또는 확인 내용 */}
</ModalShell>
```

### `FormRenderer`

`CrudField[]`를 받아 입력 필드를 렌더링.

```tsx
<FormRenderer
  fields={crudFields}
  form={form}
  errors={errors}
  onChange={(key, value) => setForm(f => ({ ...f, [key]: value }))}
/>
```

### `form-utils`

폼 관련 순수 유틸.

```ts
import { buildEmptyForm, validateFields } from '@/components/data-grid/form-utils';

const emptyForm = buildEmptyForm(fields);    // 빈 폼 객체 생성
const errors = validateFields(fields, form); // 검증, 에러 맵 반환
```

---

## API 요청 패턴

```ts
import { api } from '@/lib/api';

await api.get('/api/v1/product', { params: { page: 0, size: 20 } });
await api.get('/api/v1/product/1');
await api.post('/api/v1/product', { name: '상품명' });
await api.patch('/api/v1/product/1', { name: '새 이름' });
await api.delete('/api/v1/product/1');
```

**응답 형식**

```ts
// 단건: ApiResponse<T>
{ success: true, data: T, message: string, timestamp: number }

// 목록: ApiResponse<ApiPageResponse<T>>
{ data: { content: T[], totalElements: number, page: number, size: number } }
```

---

## 환경 변수

```env
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8081
```
