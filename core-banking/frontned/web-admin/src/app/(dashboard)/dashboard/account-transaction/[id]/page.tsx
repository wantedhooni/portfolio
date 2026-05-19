import DetailPage from '@/components/data-grid/DetailPage';
import { transactionDetailConfig } from '@/features/account-transaction/config';

export default async function TransactionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <DetailPage config={transactionDetailConfig} id={id} />;
}
