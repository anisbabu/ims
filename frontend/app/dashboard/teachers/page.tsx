"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Page, Teacher } from "@/lib/types";
import { DESIGNATIONS } from "@/lib/types";

export default function TeachersPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ fullName: "", designation: "SUBJECT", phone: "" });
  const [err, setErr] = useState<string | null>(null);

  const list = useQuery({
    queryKey: ["teachers"],
    queryFn: () => api<Page<Teacher>>("/teachers?size=50"),
  });

  const create = useMutation({
    mutationFn: () => api<Teacher>("/teachers", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ fullName: "", designation: "SUBJECT", phone: "" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["teachers"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Teachers</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div>
          <label className="label">Full name</label>
          <input className="input" required value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </div>
        <div>
          <label className="label">Designation</label>
          <select className="input" value={form.designation}
            onChange={(e) => setForm({ ...form, designation: e.target.value })}>
            {DESIGNATIONS.map((d) => <option key={d}>{d}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Phone</label>
          <input className="input" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Add teacher</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="th">Name</th>
              <th className="th">Designation</th>
              <th className="th">Phone</th>
              <th className="th">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((t) => (
              <tr key={t.id}>
                <td className="td font-medium">{t.fullName}</td>
                <td className="td">{t.designation}</td>
                <td className="td">{t.phone ?? "—"}</td>
                <td className="td">{t.status}</td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={4}>No teachers yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
