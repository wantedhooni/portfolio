'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { productConfig } from '@/features/insurance/config';

export default function InsuranceProductPage() {
  return <CrudPage config={productConfig} />;
}
