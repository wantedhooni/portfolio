// PM2 process manager 설정 — web-admin / web-saas (Next.js `next start`).
//
// 배경
//   `next start` 를 하루 이상 띄우면 Node 힙이 서서히 차올라 결국
//   V8 한계에서 프로세스가 죽거나(heap OOM) GC 스래싱으로 이벤트 루프가 멈춥니다.
//   기존 `npm run start &` 는 감시자가 없어 한 번 멈추면 영구히 죽은 채로 남았습니다.
//
// 이 설정이 해결하는 것
//   1) autorestart        — 크래시 시 자동 재기동 (영구 다운 방지)
//   2) max_memory_restart — 메모리 한도 초과 시 누수가 터지기 전에 선제 재시작
//   3) node_args 힙 상한   — V8 한계를 host RAM보다 낮게 고정 → 호스트 OOM 전파 차단
//   4) 로그 파일          — heap OOM 여부 등 원인 추적 (pm2 logs / ~/.pm2/logs)
//
// 메모리 수치는 보수적 기본값입니다. 호스트 RAM이 넉넉하면 512/600 → 1024/1200 등으로 상향하세요.

const common = {
    script: 'node_modules/next/dist/bin/next',
    args: 'start',
    interpreter: 'node',
    exec_mode: 'fork',
    instances: 1,
    autorestart: true,
    node_args: '--max-old-space-size=512', // 힙 상한 512MB
    max_memory_restart: '600M',            // RSS 600MB 초과 시 재시작
    max_restarts: 20,
    restart_delay: 3000,
    time: true,        // 로그에 타임스탬프
    merge_logs: true,
    env: { NODE_ENV: 'production' },
};

module.exports = {
    apps: [
        { ...common, name: 'web-admin', cwd: './web-admin', args: 'start -p 18081' },
        { ...common, name: 'web-saas', cwd: './web-saas', args: 'start -p 18091' },
    ],
};
