import Link from 'next/link';
import { fetchDetailServer } from '@/services/crud.server';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AlertCircleIcon, ChevronLeftIcon, ChevronRightIcon } from 'lucide-react';
import type { DetailPageConfig } from '@/types';

interface DetailPageProps {
  config: DetailPageConfig;
  id: string;
}

export default async function DetailPage({ config, id }: DetailPageProps) {
  let data: Record<string, unknown> | null = null;
  let error = '';

  try {
    data = await fetchDetailServer<Record<string, unknown>>(config.endpoint, id);
  } catch {
    error = '데이터를 불러오는데 실패했습니다.';
  }

  const displayFields = config.fields
    ? config.fields.map(({ key, label }) => ({ label, value: data?.[key] }))
    : Object.entries(data ?? {}).map(([key, value]) => ({ label: key, value }));

  return (
    <div className="flex flex-col gap-5 max-w-2xl">
      <div className="flex items-center gap-1">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link href={config.listPath}>
            <ChevronLeftIcon data-icon="inline-start" />
            목록
          </Link>
        </Button>
        <ChevronRightIcon className="size-3.5 text-muted-foreground/40" />
        <h1 className="text-lg font-semibold">{config.title}</h1>
      </div>

      {error ? (
        <Alert variant="destructive">
          <AlertCircleIcon />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : (
        <Card className="overflow-hidden p-0">
          <dl className="divide-y divide-border">
            {displayFields.map(({ label, value }) => (
              <div key={label} className="grid grid-cols-[9rem_1fr] items-start px-6 py-3.5 gap-4">
                <dt className="pt-0.5 text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</dt>
                <dd className="text-sm break-all">
                  {value === null || value === undefined || value === ''
                    ? <span className="text-muted-foreground/40">—</span>
                    : String(value)}
                </dd>
              </div>
            ))}
          </dl>
        </Card>
      )}
    </div>
  );
}
