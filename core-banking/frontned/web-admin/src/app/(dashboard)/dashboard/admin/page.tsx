'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { adminConfig } from '@/features/admin/config';

export default function AdminPage() {
  return <CrudPage config={adminConfig} />;
}
