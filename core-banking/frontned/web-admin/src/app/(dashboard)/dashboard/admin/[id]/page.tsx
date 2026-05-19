import DetailPage from '@/components/data-grid/DetailPage';
import { adminDetailConfig } from '@/features/admin/config';

export default async function AdminDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <DetailPage config={adminDetailConfig} id={id} />;
}
