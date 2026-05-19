'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { accountConfig } from '@/features/account/config';

export default function AccountPage() {
  return <CrudPage config={accountConfig} />;
}
