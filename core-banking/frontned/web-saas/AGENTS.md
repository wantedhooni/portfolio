<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

## 작업 규칙
- 이 프로젝트는 사용자 SaaS용 코어뱅킹 프론트엔드이며, 작업 범위는 `web-saas` 내부로 제한한다.
- `web-admin`, `backend`, 다른 템플릿 프로젝트를 참고하거나 침범하지 않는다.
- API 연동은 `saas-api-docs.json`의 OpenAPI 계약을 우선 기준으로 삼는다.
- class, service, service method에는 한글 doc 주석을 작성한다.
- 작업 내용은 `TASK.md`에 한글로 기록하고, 계획은 `PLANS.md`에 한글로 관리한다.
- 작업 후 `README.md`와 `scripts/all-start.sh`, `scripts/all-stop.sh`, `scripts/all-restart.sh`를 최신 상태로 유지한다.
