import DetailPage from '@/components/data-grid/DetailPage';
import { stockDetailConfig } from '@/features/stock/config';

export default async function StockDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <DetailPage config={stockDetailConfig} id={id} />;
}
