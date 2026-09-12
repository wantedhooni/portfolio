import { expect, test } from "@playwright/test";

test("admins search reflects filtered results in grid", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel(/e-?mail/i).fill("sysadmin@system.dev");
  await page.getByLabel(/password/i).fill("qwer1234!");
  await page.getByRole("button", { name: /sign in|login/i }).click();
  await page.waitForURL(/\/domain\/[a-z-]+(?:\?.*)?$/);
  await expect(page).not.toHaveURL(/\/login(?:\?.*)?$/);

  await page.goto("/domain/admins", { waitUntil: "domcontentloaded" });
  await page.waitForURL(/\/domain\/admins(?:\?.*)?$/);
  await expect(page.getByText("sysadmin@system.dev")).toBeVisible();

  await page.getByPlaceholder("Keyword").fill("no-user-should-match-this-keyword");
  await page.getByRole("button", { name: "Search" }).click();
  await expect(page.getByText("No results found.")).toBeVisible();

  await page.getByPlaceholder("Keyword").fill("sysadmin");
  await page.getByRole("button", { name: "Search" }).click();
  await expect(page.getByText("sysadmin@system.dev")).toBeVisible();
});
