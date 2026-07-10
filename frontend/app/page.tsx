"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAccess } from "@/lib/api";

export default function Home() {
  const router = useRouter();
  useEffect(() => {
    router.replace(getAccess() ? "/dashboard" : "/login");
  }, [router]);
  return (
    <main className="grid min-h-screen place-items-center text-slate-500">
      Loading…
    </main>
  );
}
