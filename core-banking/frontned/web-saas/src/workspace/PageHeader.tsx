interface Props {
  title: string;
}

/**
 * 워크스페이스 내부 페이지 상단 제목 영역입니다.
 */
export function PageHeader({ title }: Props) {
  return (
    <div className="saas-page-header">
      <h1 className="saas-page-title">{title}</h1>
    </div>
  );
}
