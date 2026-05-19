@AGENTS.md

당신은 별도의 백엔드 API 서버와 통신하는 **Next.js 14+ (App Router) 기반의 어드민 패널**을 구축하는 시니어 프론트엔드 개발자입니다. 아래 요구사항과 기술 스택을 바탕으로 아키텍처 설계와 코드를 작성해 주세요.
> 
> 1. 기술 스택 및 환경
> 
> - **Framework**: Next.js 14+ (App Router, `'use client'` 기반 클라이언트 사이드 데이터 패칭 우선)
> - **Language / Styling**: TypeScript, Tailwind CSS
> - **Data Table**: ag-Grid (`ag-grid-react`, `ag-grid-community`)
> - **UI Component**: Tailwind CSS 기반의 모달(Modal) 창
> - **HTTP Client**: Axios (또는 Fetch API 래퍼)
> 
> 2. 인증 및 API 통신 요구사항 (JWT)
> 
> - **Client-Side Request**: 모든 데이터 패칭 및 인증 API 요청은 Next.js 서버를 거치지 않고 **브라우저(Client)에서 백엔드 서버로 직접 요청**합니다.
> - **토큰 관리**: Access Token은 클라이언트 메모리(또는 Zustand 등)에 저장하고, Refresh Token은 브라우저 쿠키(또는 백엔드 HttpOnly Cookie)로 관리합니다.
> - **Token Refresh (Axios Interceptor)**: API 요청 중 Access Token이 만료되어 `401 Unauthorized` 에러가 발생하면, 자동으로 백엔드의 `/api/auth/refresh` 엔드포인트를 호출하여 토큰을 재발급받고, 실패했던 기존 API 요청을 자동으로 재시도(Retry)하는 interceptor 로직을 작성해 주세요. 토큰 갱신에 완전히 실패하면 `/login`으로 리다이렉트합니다.
> 
> 3. 기능 및 UI 요구사항
> 
> - **ag-Grid 데이터 테이블**:
>     - 백엔드 API와 연동된 서버 사이드 페이징(Server-side Pagination) 또는 클라이언트 사이드 페이징이 원활하게 동작하는 ag-Grid 설정을 보여주세요.
>     - 정렬(Sorting), 필터링(Filtering), 컬럼 크기 조절 기능이 포함되어야 합니다.
> - **모달(Modal) 생성 폼**:
>     - 어드민 페이지 내에 '등록/생성' 버튼을 누르면 화면 위에 모달 창이 뜹니다.
>     - 모달 내부에는 유효성 검증(Validation)이 포함된 생성 폼이 들어가며, 제출(Submit) 시 백엔드 API로 생성 요청을 보낸 후 성공하면 ag-Grid 테이블이 새로고침(Refresh)되어야 합니다.
> 
> ---
> 
> [출력 요구사항]
> 
> 위 구조를 구현하기 위해 필요한 다음 핵심 코드들을 TypeScript로 작성해 주세요.
> 
> 1. **`src/lib/api.ts`**: 401 에러 시 Silent Refresh(토큰 자동 갱신 및 재시도)가 구현된 Axios 인스턴스 설정 코드
> 2. **`src/components/admin/AdminModal.tsx`**: 데이터 생성을 위한 재사용 가능하거나 독립적인 모달 폼 컴포넌트
> 3. **`src/app/admin/users/page.tsx`** (예시 페이지): ag-Grid 테이블이 탑재되어 있고, '생성 모달'을 열 수 있는 버튼이 포함된 클라이언트 컴포넌트 메인 페이지 코드