import { NextResponse } from "next/server";

import { reissue } from "@/lib/service-api";
import type { ReissueRequest } from "@/lib/types";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as ReissueRequest;
    const response = await reissue(body);
    return NextResponse.json(response);
  } catch (caughtError) {
    return NextResponse.json(
      {
        success: false,
        error: {
          message:
            caughtError instanceof Error
              ? caughtError.message
              : "토큰 재발급 요청에 실패했습니다.",
        },
      },
      { status: 400 },
    );
  }
}
