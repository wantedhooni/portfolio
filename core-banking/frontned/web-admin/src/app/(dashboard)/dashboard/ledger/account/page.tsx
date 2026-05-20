'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { ledgerAccountConfig } from '@/features/ledger/config';

export default function LedgerAccountPage() {
  return <CrudPage config={ledgerAccountConfig} />;
}
