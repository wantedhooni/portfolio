import { expect, test } from "@playwright/test";

test("admins list edit/show links navigate to detail routes", async ({ page }) => {
  await page.goto("/login");

  await page.getByLabel(/e-?mail/i).fill("sysadmin@system.dev");
  await page.getByLabel(/password/i).fill("qwer1234!");
  await page.getByRole("button", { name: /sign in|login/i }).click();

  await page.waitForURL(/\/domain\/[a-z-]+(?:\?.*)?$/);
  await page.goto("/domain/admins", { waitUntil: "domcontentloaded" });
  await page.waitForURL(/\/domain\/admins(?:\?.*)?$/);

  const firstEditLink = page.locator('a[href*="/domain/admins/edit/"]').first();
  await expect(firstEditLink).toBeVisible();
  await firstEditLink.click();
  await expect(page).toHaveURL(/\/domain\/admins\/edit\/[^/?#]+(?:\?.*)?$/);

  await page.goto("/domain/admins", { waitUntil: "domcontentloaded" });
  await page.waitForURL(/\/domain\/admins(?:\?.*)?$/);

  const firstShowLink = page.locator('a[href*="/domain/admins/show/"]').first();
  await expect(firstShowLink).toBeVisible();
  await firstShowLink.click();
  await expect(page).toHaveURL(/\/domain\/admins\/show\/[^/?#]+(?:\?.*)?$/);
});
