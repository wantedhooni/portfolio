import { expect, test } from "@playwright/test";

test.describe("Auth flow", () => {
  test("login page renders", async ({ page }) => {
    await page.goto("/login");

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole("button", { name: /sign in|login/i })).toBeVisible();
  });

  test("successful login redirects to protected page", async ({ page }) => {
    await page.goto("/login");

    await page.getByLabel(/e-?mail/i).fill("sysadmin@system.dev");
    await page.getByLabel(/password/i).fill("qwer1234!");
    await page.getByRole("button", { name: /sign in|login/i }).click();

    await page.waitForURL(/\/domain\/[a-z-]+(?:\?.*)?$/);
    await expect(page).not.toHaveURL(/\/login(?:\?.*)?$/);
    await expect(page.getByRole("button", { name: /search/i }).first()).toBeVisible();
  });
});
