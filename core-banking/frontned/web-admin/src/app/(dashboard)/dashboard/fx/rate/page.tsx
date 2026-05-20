'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { rateConfig, rateCreateFields } from '@/features/fx/config';

// Rate는 등록만 가능 (수정/삭제 없음)
// CrudPage가 crudFields를 받으면 액션 컬럼이 자동 표시되므로
// columnDefs를 정적으로 설정하고 crudFields는 별도로 주입하지 않음.
// 등록은 페이지 헤더의 + 버튼으로만 가능.
export default function FxRatePage() {
  return (
    <CrudPage
      config={{
        ...rateConfig,
        crudFields: rateCreateFields,
        // ColDef[] 형태 (함수 아님) → 액션 컬럼 미포함
      }}
    />
  );
}
