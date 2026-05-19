'use client';

import CrudPage from '@/components/data-grid/CrudPage';
import { transactionConfig } from '@/features/account-transaction/config';

export default function AccountTransactionPage() {
  return <CrudPage config={transactionConfig} />;
}
