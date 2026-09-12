# Frontend AI 작업 방식

이 문서는 `frontend/ui-admin`에서 AI가 작업할 때의 기준을 정리한다.

## 1. 기본 원칙

- `refine.dev` 표준 패턴을 우선한다.
- 동작 변경보다 일관성/안정성 있는 리팩토링을 우선한다.
- 하드코딩 문자열보다 도메인 `constants.ts` 상수를 우선 사용한다.
- 신규 도메인/기능은 기존 `admins` 패턴을 기준으로 맞춘다.

## 2. 도메인 페이지 표준 구조

도메인별 `constants.ts`는 아래 3개를 기본으로 가진다.

```ts
export const RESOURCE = "roles";
export const API_PATH = "/api/role";
export const RESOURCE_META = { apiPath: API_PATH } as const;
```

그리고 `page.tsx`, `create`, `edit`, `show`에서 아래 훅 사용 시 모두 `meta: RESOURCE_META`를 전달한다.

- `useDataGrid`
- `useModalForm`
- `useForm`
- `useShow`

## 3. 리스트/폼 구현 기준

- List 페이지:
  - 검색은 `useDataGrid({ onSearch })` + `search()` 사용
  - 툴바는 `DomainListToolbar` 사용
  - 저장 버튼은 `SaveButton + saveButtonProps` 사용
- Form 페이지:
  - `register` 필드는 `constants.ts`의 `*_FIELD`, `*_LABEL` 상수 사용
  - 가능하면 `useForm`/`useModalForm` 제네릭으로 폼 타입을 명시
- `as any` 사용은 최소화하고, 가능하면 타입 안전하게 접근한다.

## 4. Devtools / Next 설정 기준

- `npm run dev`는 Devtools 활성화 상태(`NEXT_PUBLIC_ENABLE_DEVTOOLS=true`)를 기본으로 사용한다.
- Next 개발 origin은 `next.config.mjs`의 `allowedDevOrigins`를 사용한다.
- 추가 origin이 필요하면 `ALLOWED_DEV_ORIGINS` 환경변수(CSV)로 확장한다.

## 5. 검증 순서

변경 후 아래를 기본 검증으로 수행한다.

```bash
cd frontend/ui-admin
npm run lint
npm run test:e2e
```

참고:
- `npx tsc --noEmit`은 현재 프로젝트의 외부 의존성 타입 충돌 상태에 따라 실패할 수 있으므로, 실패 시 변경 범위 영향인지 분리해서 판단한다.

## 6. 문서 갱신 규칙

아래 항목이 바뀌면 문서를 함께 갱신한다.

- 실행 명령(`package.json` scripts)
- 도메인 템플릿/상수 규칙
- Next/Refine 설정 방식
- E2E/검증 기준
