"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, downloadFile } from "@/lib/api";
import type { Certificate, Page, Student } from "@/lib/types";
import { CERTIFICATE_TYPES } from "@/lib/types";

export default function CertificatesPage() {
  const qc = useQueryClient();
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=100") });
  const list = useQuery({ queryKey: ["certificates"], queryFn: () => api<Page<Certificate>>("/certificates?size=50") });
  const [form, setForm] = useState({ studentId: "", type: "MARKSHEET", title: "", serialNo: "", content: "" });
  const [err, setErr] = useState<string | null>(null);

  const issue = useMutation({
    mutationFn: () => api("/certificates", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ studentId: "", type: "MARKSHEET", title: "", serialNo: "", content: "" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["certificates"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const studentName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Certificates</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-3"
        onSubmit={(e) => { e.preventDefault(); issue.mutate(); }}>
        <div>
          <label className="label">Student</label>
          <select className="input" required value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })}>
            <option value="">Select…</option>
            {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Type</label>
          <select className="input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            {CERTIFICATE_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Serial no</label>
          <input className="input" value={form.serialNo} onChange={(e) => setForm({ ...form, serialNo: e.target.value })} />
        </div>
        <div className="md:col-span-2">
          <label className="label">Title</label>
          <input className="input" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div className="md:col-span-3">
          <label className="label">Content</label>
          <textarea className="input" rows={2} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={issue.isPending}>Issue certificate</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Serial</th><th className="th">Student</th><th className="th">Type</th><th className="th">Title</th><th className="th">Issued</th><th className="th"></th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((c) => (
              <tr key={c.id}>
                <td className="td">{c.serialNo ?? "—"}</td>
                <td className="td font-medium">{studentName(c.studentId)}</td>
                <td className="td">{c.type}</td>
                <td className="td">{c.title}</td>
                <td className="td">{c.issueDate ?? "—"}</td>
                <td className="td">
                  <button
                    className="text-xs font-medium text-indigo-600 hover:underline"
                    onClick={() => downloadFile(`/certificates/${c.id}/pdf`, `certificate-${c.serialNo ?? c.id}.pdf`)}
                  >
                    PDF ↓
                  </button>
                </td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={6}>No certificates yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
