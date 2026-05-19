"use client";

import { createContext, useContext, type ReactNode } from "react";

import { useWorkspace } from "./useWorkspace";

type WorkspaceContextValue = ReturnType<typeof useWorkspace>;

const WorkspaceContext = createContext<WorkspaceContextValue | null>(null);

/**
 * 워크스페이스 전체 데이터 상태를 하위 페이지에 공급하는 Context Provider입니다.
 *
 * layout.tsx에서 한 번만 마운트되며, 계좌 선택이 바뀌어도 페이지 전환 시 재요청하지 않습니다.
 */
export function WorkspaceProvider({ children }: { children: ReactNode }) {
  const workspace = useWorkspace();
  return <WorkspaceContext.Provider value={workspace}>{children}</WorkspaceContext.Provider>;
}

export function useWorkspaceContext(): WorkspaceContextValue {
  const ctx = useContext(WorkspaceContext);
  if (!ctx) throw new Error("useWorkspaceContext는 WorkspaceProvider 내부에서만 사용할 수 있습니다.");
  return ctx;
}
