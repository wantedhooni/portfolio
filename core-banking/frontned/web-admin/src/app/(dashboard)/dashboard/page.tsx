import Link from 'next/link';

const SHORTCUTS = [
  { href: '/dashboard/admin', label: '어드민 관리', desc: '어드민 계정을 등록하고 관리합니다.' },
  { href: '/dashboard/account', label: '계정 관리', desc: '사용자 계정을 조회하고 관리합니다.' },
  { href: '/dashboard/account-transaction', label: '거래 내역', desc: '거래 기록을 조회합니다.' },
];

export default function DashboardHomePage() {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-lg font-semibold text-gray-900">대시보드</h1>
        <p className="text-sm text-gray-500 mt-1">관리할 항목을 선택하세요.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {SHORTCUTS.map(({ href, label, desc }) => (
          <Link
            key={href}
            href={href}
            className="group bg-white border border-gray-200 rounded-xl p-5 hover:border-blue-300 hover:shadow-sm transition-all"
          >
            <p className="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">{label}</p>
            <p className="text-xs text-gray-400 mt-1">{desc}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
