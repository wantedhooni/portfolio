import { NextResponse } from "next/server";

import { signin } from "@/lib/service-api";
import type { SigninRequest } from "@/lib/types";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as SigninRequest;
    const response = await signin(body);
    return NextResponse.json(response);
  } catch (caughtError) {
    return NextResponse.json(
      {
        success: false,
        error: {
          message:
            caughtError instanceof Error
              ? caughtError.message
              : "로그인 요청에 실패했습니다.",
        },
      },
      { status: 400 },
    );
  }
}
