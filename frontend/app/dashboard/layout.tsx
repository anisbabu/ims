"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { clearTokens, getAccess } from "@/lib/api";
import { useMe } from "@/lib/hooks";
import { Nav } from "@/components/Nav";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { data: me, isError } = useMe();

  useEffect(() => {
    if (!getAccess()) router.replace("/login");
  }, [router]);

  useEffect(() => {
    if (isError) {
      clearTokens();
      router.replace("/login");
    }
  }, [isError, router]);

  function logout() {
    clearTokens();
    router.replace("/login");
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-7xl gap-6 p-4">
      <aside className="w-56 shrink-0 space-y-4">
        <div className="px-3">
          <div className="text-lg font-bold text-indigo-700">IMS</div>
          <div className="text-xs text-slate-500">
            {me ? `${me.fullName} · ${me.role}` : "…"}
          </div>
        </div>
        <Nav />
        <button className="btn-ghost w-full" onClick={logout}>
          Log out
        </button>
      </aside>
      <main className="min-w-0 flex-1">{children}</main>
    </div>
  );
}
