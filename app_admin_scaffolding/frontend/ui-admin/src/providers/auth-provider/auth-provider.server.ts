import type { AuthProvider } from "@refinedev/core";
import { cookies } from "next/headers";

import {
  ACCESS_TOKEN_COOKIE_KEY,
  REFRESH_TOKEN_COOKIE_KEY,
} from "@providers/auth-provider/token-keys";

export const authProviderServer: Pick<AuthProvider, "check"> = {
  check: async () => {
    const cookieStore = await cookies();
    const accessToken = cookieStore.get(ACCESS_TOKEN_COOKIE_KEY);
    const refreshToken = cookieStore.get(REFRESH_TOKEN_COOKIE_KEY);

    if (accessToken || refreshToken) {
      return {
        authenticated: true,
      };
    }

    return {
      authenticated: false,
      logout: true,
      redirectTo: "/login",
    };
  },
};
