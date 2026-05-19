import DetailPage from '@/components/data-grid/DetailPage';
import { accountDetailConfig } from '@/features/account/config';

export default async function AccountDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <DetailPage config={accountDetailConfig} id={id} />;
}
