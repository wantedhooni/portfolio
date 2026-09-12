import { expect, test } from "@playwright/test";

test("home page exposes signup and signin entry points", async ({ page }) => {
  await page.goto("/");

  await expect(
    page.getByRole("heading", {
      name: "현재 열려 있는 Service API 인증 기능을 바로 테스트하는 고객 앱",
    }),
  ).toBeVisible();
  await expect(page.getByRole("link", { name: "회원가입 테스트" })).toBeVisible();
  await expect(page.getByRole("link", { name: "로그인 테스트" })).toBeVisible();
});
