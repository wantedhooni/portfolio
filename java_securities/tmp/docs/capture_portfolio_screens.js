const { chromium, devices } = require('playwright');
const path = require('path');

const root = '/Users/revy/workspace_revy/securities_monolithic';
const out = path.join(root, 'output', 'playwright');
const baseUrl = 'http://localhost:5173';
const email = 'user1@example.com';
const password = 'Password!';

async function login(page) {
  await page.goto(`${baseUrl}/login`, { waitUntil: 'networkidle' });
  await page.fill('input[type="email"]', email);
  await page.fill('input[type="password"]', password);
  await Promise.all([
    page.waitForURL(url => !url.toString().includes('/login'), { timeout: 15000 }),
    page.click('button[type="submit"], .primary-button'),
  ]);
  await page.waitForLoadState('networkidle');
}

async function captureDesktop() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1080 }, deviceScaleFactor: 1 });
  const page = await context.newPage();

  await page.goto(baseUrl, { waitUntil: 'networkidle' });
  await page.screenshot({ path: path.join(out, 'portfolio-home-desktop.png'), fullPage: true });

  await login(page);

  const routes = [
    ['/chart', 'portfolio-chart-desktop.png'],
    ['/account', 'portfolio-account-desktop.png'],
    ['/exchange', 'portfolio-exchange-desktop.png'],
    ['/trade', 'portfolio-trade-desktop.png'],
    ['/orders', 'portfolio-orders-desktop.png'],
  ];

  for (const [route, file] of routes) {
    await page.goto(`${baseUrl}${route}`, { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(out, file), fullPage: true });
  }

  await browser.close();
}

async function captureMobile() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ ...devices['iPhone 13'] });
  const page = await context.newPage();

  await page.goto(baseUrl, { waitUntil: 'networkidle' });
  await page.screenshot({ path: path.join(out, 'portfolio-home-mobile.png'), fullPage: true });

  await login(page);

  const routes = [
    ['/trade', 'portfolio-trade-mobile.png'],
    ['/orders', 'portfolio-orders-mobile.png'],
  ];

  for (const [route, file] of routes) {
    await page.goto(`${baseUrl}${route}`, { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(out, file), fullPage: true });
  }

  await browser.close();
}

(async () => {
  await captureDesktop();
  await captureMobile();
})();
