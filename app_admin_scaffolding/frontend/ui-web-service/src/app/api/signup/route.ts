import { NextResponse } from "next/server";

import { signup } from "@/lib/service-api";
import type { SignupRequest } from "@/lib/types";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as SignupRequest;
    const response = await signup(body);
    return NextResponse.json(response);
  } catch (caughtError) {
    return NextResponse.json(
      {
        success: false,
        error: {
          message:
            caughtError instanceof Error
              ? caughtError.message
              : "회원가입 요청에 실패했습니다.",
        },
      },
      { status: 400 },
    );
  }
}
