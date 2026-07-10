"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Page } from "@/lib/types";
import { useMe } from "@/lib/hooks";

function Stat({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="card">
      <div className="text-2xl font-semibold">{value}</div>
      <div className="text-sm text-slate-500">{label}</div>
    </div>
  );
}

function useCount(path: string) {
  return useQuery({
    queryKey: ["count", path],
    queryFn: () => api<Page<unknown>>(path),
    select: (d) => d.totalElements,
  });
}

export default function DashboardHome() {
  const { data: me } = useMe();
  const students = useCount("/students?size=1");
  const teachers = useCount("/teachers?size=1");
  const guardians = useCount("/guardians?size=1");
  const admissions = useCount("/admissions?size=1");

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Dashboard</h1>
      <p className="text-sm text-slate-500">
        Welcome{me ? `, ${me.fullName}` : ""}. Tenant-scoped overview.
      </p>
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <Stat label="Students" value={students.data ?? "…"} />
        <Stat label="Teachers" value={teachers.data ?? "…"} />
        <Stat label="Guardians" value={guardians.data ?? "…"} />
        <Stat label="Admissions" value={admissions.data ?? "…"} />
      </div>
    </div>
  );
}
