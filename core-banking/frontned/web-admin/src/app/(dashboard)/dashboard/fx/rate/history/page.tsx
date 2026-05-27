'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { rateHistoryConfig } from '@/features/fx/config';

export default function FxRateHistoryPage() {
  return <CrudPage config={rateHistoryConfig} />;
}
