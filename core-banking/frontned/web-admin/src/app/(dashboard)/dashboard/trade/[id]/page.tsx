import DetailPage from '@/components/data-grid/DetailPage';
import { tradeDetailConfig } from '@/features/trade/config';

export default async function TradeDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <DetailPage config={tradeDetailConfig} id={id} />;
}
