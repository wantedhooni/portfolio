# 도메인 템플릿 사용 가이드 (현재 기준)

이 문서는 현재 코드 기준으로 `src/app/_dumy_domain` 템플릿을 복사해 신규 도메인 페이지를 만드는 방법을 설명합니다.

## 1. 폴더 복사
원하는 리소스 이름으로 템플릿을 복사합니다.

```bash
cd frontend/ui-admin
cp -R src/app/_dumy_domain src/app/domain/products
```

## 2. 파일 구조 확인
복사 후 아래 구조가 있어야 합니다.

- `src/app/domain/products/layout.tsx`
- `src/app/domain/products/constants.ts`
- `src/app/domain/products/page.tsx`
- `src/app/domain/products/create/page.tsx`
- `src/app/domain/products/edit/[id]/page.tsx`
- `src/app/domain/products/show/[id]/page.tsx`

## 3. constants.ts 수정
`src/app/domain/products/constants.ts`에서 최소 아래 상수를 맞춥니다.

```ts
export const RESOURCE = "products";
export const API_PATH = "/api/product";
export const RESOURCE_META = { apiPath: API_PATH } as const;

export const ENTITY_LABEL = "Product";
export const PRIMARY_FIELD = "name";
export const PRIMARY_LABEL = "Name";
```

설명:
- `RESOURCE`: Refine 리소스 이름. `src/app/layout.tsx`의 `resources[].name`과 반드시 동일해야 함.
- `API_PATH`: 실제 백엔드 API 경로.
- `RESOURCE_META`: `data-provider`가 `meta.apiPath`를 읽어서 API 경로를 결정할 때 사용.
- `ENTITY_LABEL`, `PRIMARY_*`: 화면 표시/폼 바인딩용.

현재 도메인(`admins`, `roles`, `permissions`)은 아래처럼 동일한 구조를 사용합니다.

```ts
export const RESOURCE = "roles"; // resources[].name과 동일
export const API_PATH = "/api/role";
export const RESOURCE_META = { apiPath: API_PATH } as const;
```

주의:
- `RESOURCE`와 API 경로가 다를 수 있으므로(`roles` vs `/api/role`) 반드시 `RESOURCE_META`를 함께 유지합니다.

## 4. 페이지 훅에 meta 전달 확인
아래 훅에서 `resource`와 함께 `meta: RESOURCE_META`를 전달해야 `API_PATH`가 적용됩니다.

- `useDataGrid`
- `useModalForm` / `useForm`
- `useShow`

예:

```tsx
const { dataGridProps } = useDataGrid({
  resource: RESOURCE,
  meta: RESOURCE_META,
});
```

## 5. layout.tsx 리소스 등록
`src/app/layout.tsx`의 `resources`에 신규 리소스를 추가합니다.

```tsx
{
  name: "products",
  list: "/domain/products",
  create: "/domain/products/create",
  edit: "/domain/products/edit/:id",
  show: "/domain/products/show/:id",
  meta: {
    parent: "domain-user-group", // 필요 시
    label: "Products",
    canDelete: true,
  },
}
```

## 6. API 경로 규칙
현재 `src/providers/data-provider/index.ts` 규칙은 다음과 같습니다.

- `meta.apiPath`가 있으면 그 경로를 사용
- 없으면 기본값 `/api/${resource}` 사용

즉, 리소스명과 API 경로가 다를 때는 `constants.ts`에서 `API_PATH`와 `RESOURCE_META`를 함께 맞춰야 합니다.

## 7. 검증

```bash
cd frontend/ui-admin
npx tsc --noEmit
npm run lint
```

참고:
- `npm run lint`는 현재 `eslint .`를 사용합니다.

## 8. 빠른 체크리스트
- 템플릿 복사 완료 (`_dumy_domain` -> 신규 경로)
- `constants.ts`의 `RESOURCE`, `API_PATH`, `RESOURCE_META`, `PRIMARY_*` 수정
- 각 페이지 훅에 `meta: RESOURCE_META` 적용
- `src/app/layout.tsx` resources 등록
- `npx tsc --noEmit`, `npm run lint` 통과

## 9. `generate:grid-columns` 사용법
응답 JSON 모델로 `GridColDef[]` 초안을 생성할 수 있습니다.

### 9.1 기본 실행
```bash
cd frontend/ui-admin
cat model.json | npm run generate:grid-columns
```

### 9.2 파일 입력 + 상수명 지정
```bash
cd frontend/ui-admin
npm run generate:grid-columns -- --input model.json --name USER_LIST_COLUMNS
```

### 9.3 actions 컬럼 제외
```bash
cd frontend/ui-admin
npm run generate:grid-columns -- --input model.json --no-actions
```

### 9.4 입력 JSON 예시
```json
{
  "id": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "nickName": "string",
  "status": "ACTIVE",
  "permissions": ["string"],
  "failCount": 0,
  "enabled": true
}
```

### 9.5 생성 결과 적용 위치
- 생성된 코드를 도메인별 `constants.ts`에 붙여 넣고
- `page.tsx`에서 `columns={...}`에 연결합니다.
- `actions` 컬럼이 포함된 경우 `renderActions`를 페이지에서 주입해야 합니다.
