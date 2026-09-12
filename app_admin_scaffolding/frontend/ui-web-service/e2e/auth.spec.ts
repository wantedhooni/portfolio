import { expect, test } from "@playwright/test";

test("signup page submits and routes to signin", async ({ page }) => {
  await page.route("**/api/signup", async (route) => {
    const payload = route.request().postDataJSON() as {
      email: string;
      password: string;
      nickName: string;
    };

    expect(payload.nickName).toBe("revy");

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: {
          message: "회원가입이 완료되었습니다.",
        },
      }),
    });
  });

  await page.goto("/signup");
  await page.getByLabel("Email").fill("user@example.com");
  await page.getByLabel("Nickname").fill("revy");
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: "Signup" }).click();

  await page.waitForURL("**/signin");
  await expect(page).toHaveURL(/\/signin$/);
});

test("signin page submits and returns home", async ({ page }) => {
  await page.route("**/api/signin", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: {
          tokenType: "Bearer",
          accessToken: "access-token-for-test",
          refreshToken: "refresh-token-for-test",
        },
      }),
    });
  });

  await page.goto("/signin");
  await page.getByLabel("Email").fill("user@example.com");
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: "Signin" }).click();

  await page.waitForURL("**/");
  await expect(page).toHaveURL(/\/$/);
});
