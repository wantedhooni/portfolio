import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: "list",
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://127.0.0.1:3100",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: {
    command:
      "cross-env PORT=3100 NEXT_PUBLIC_DISABLE_DEVTOOLS=true NODE_OPTIONS=--max_old_space_size=4096 npx refine dev --devtools=false",
    url: "http://127.0.0.1:3100/login",
    reuseExistingServer: true,
    timeout: 180_000,
  },
});
