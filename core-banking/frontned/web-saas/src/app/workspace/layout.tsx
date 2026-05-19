"use client";

import { startTransition, useEffect, useState, type ReactNode } from "react";
import { useRouter } from "next/navigation";

import { COOKIE_KEYS, getToken } from "@/shared/auth/tokenStore";
import { WorkspaceProvider } from "@/workspace/WorkspaceProvider";
import { WorkspaceTopNav } from "@/workspace/WorkspaceTopNav";
import { Toaster } from "@/components/ui/sonner";

export default function WorkspaceLayout({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!getToken(COOKIE_KEYS.accessToken)) {
      router.replace("/login");
    } else {
      startTransition(() => setReady(true));
    }
  }, [router]);

  if (!ready) return null;

  return (
    <WorkspaceProvider>
      <Toaster position="top-right" />
      <div className="saas-shell">
        <WorkspaceTopNav />
        <main className="saas-page">{children}</main>
      </div>
    </WorkspaceProvider>
  );
}
