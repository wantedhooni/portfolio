'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { stockConfig } from '@/features/stock/config';

export default function StockPage() {
  return <CrudPage config={stockConfig} />;
}
