"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AcademicYear, FeeStructure, Grade } from "@/lib/types";

const EMPTY = { academicYearId: "", gradeId: "", title: "", amount: "", dueDate: "" };

export default function FeeStructuresPage() {
  const qc = useQueryClient();
  const years = useQuery({ queryKey: ["years-all"], queryFn: () => api<AcademicYear[]>("/academic-years") });
  const grades = useQuery({ queryKey: ["grades-all"], queryFn: () => api<Grade[]>("/grades") });
  const list = useQuery({ queryKey: ["fee-structures"], queryFn: () => api<FeeStructure[]>("/fee-structures") });

  const [form, setForm] = useState({ ...EMPTY });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [gen, setGen] = useState({ academicYearId: "", gradeId: "" });
  const [genResult, setGenResult] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const yearName = (id: string) => years.data?.find((y) => y.id === id)?.name ?? id;
  const gradeName = (id: string) => grades.data?.find((g) => g.id === id)?.name ?? id;

  const save = useMutation({
    mutationFn: () => {
      const body = JSON.stringify({
        academicYearId: form.academicYearId,
        gradeId: form.gradeId,
        title: form.title,
        amount: Number(form.amount),
        dueDate: form.dueDate || null,
      });
      return editingId
        ? api(`/fee-structures/${editingId}`, { method: "PUT", body })
        : api("/fee-structures", { method: "POST", body });
    },
    onSuccess: () => {
      setForm({ ...EMPTY });
      setEditingId(null);
      setErr(null);
      qc.invalidateQueries({ queryKey: ["fee-structures"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const remove = useMutation({
    mutationFn: (id: string) => api(`/fee-structures/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["fee-structures"] }),
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const generate = useMutation({
    mutationFn: () =>
      api<{ students: number; created: number; skipped: number }>("/fee-structures/generate", {
        method: "POST",
        body: JSON.stringify(gen),
      }),
    onSuccess: (r) => {
      setGenResult(`Billed ${r.students} enrolled student(s): ${r.created} fees created, ${r.skipped} already billed.`);
      setErr(null);
      qc.invalidateQueries({ queryKey: ["fees"] });
    },
    onError: (e) => {
      setGenResult(null);
      setErr(e instanceof Error ? e.message : "Failed");
    },
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Fee structures</h1>

      <form
        className="card grid grid-cols-1 gap-3 md:grid-cols-5"
        onSubmit={(e) => {
          e.preventDefault();
          save.mutate();
        }}
      >
        <div>
          <label className="label">Academic year</label>
          <select className="input" required value={form.academicYearId}
            onChange={(e) => setForm({ ...form, academicYearId: e.target.value })}>
            <option value="">Select…</option>
            {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Grade</label>
          <select className="input" required value={form.gradeId}
            onChange={(e) => setForm({ ...form, gradeId: e.target.value })}>
            <option value="">Select…</option>
            {grades.data?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Fee head</label>
          <input className="input" required placeholder="e.g. Tuition (July)" value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div>
          <label className="label">Amount</label>
          <input className="input" required type="number" min="0.01" step="0.01" value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })} />
        </div>
        <div>
          <label className="label">Due date</label>
          <input className="input" type="date" value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
        </div>
        <div className="flex items-end gap-2 md:col-span-5">
          <button className="btn" disabled={save.isPending}>{editingId ? "Update head" : "Add fee head"}</button>
          {editingId && (
            <button type="button" className="btn-ghost"
              onClick={() => { setEditingId(null); setForm({ ...EMPTY }); }}>
              Cancel
            </button>
          )}
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card space-y-3">
        <h2 className="text-sm font-semibold">Generate fees for enrolled students</h2>
        <p className="text-sm text-slate-500">
          Creates one fee per head for every ENROLLED student of the year + grade. Students already
          billed for a head are skipped, so it is safe to re-run.
        </p>
        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="label">Academic year</label>
            <select className="input" value={gen.academicYearId}
              onChange={(e) => setGen({ ...gen, academicYearId: e.target.value })}>
              <option value="">Select…</option>
              {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Grade</label>
            <select className="input" value={gen.gradeId}
              onChange={(e) => setGen({ ...gen, gradeId: e.target.value })}>
              <option value="">Select…</option>
              {grades.data?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
            </select>
          </div>
          <button className="btn" disabled={!gen.academicYearId || !gen.gradeId || generate.isPending}
            onClick={() => generate.mutate()} type="button">
            Generate fees
          </button>
        </div>
        {genResult && <p className="text-sm text-green-700">{genResult}</p>}
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="th">Year</th><th className="th">Grade</th><th className="th">Fee head</th>
              <th className="th">Amount</th><th className="th">Due date</th><th className="th"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((s) => (
              <tr key={s.id}>
                <td className="td">{yearName(s.academicYearId)}</td>
                <td className="td">{gradeName(s.gradeId)}</td>
                <td className="td font-medium">{s.title}</td>
                <td className="td">{s.amount}</td>
                <td className="td">{s.dueDate ?? "—"}</td>
                <td className="td space-x-2 whitespace-nowrap">
                  <button className="text-xs font-medium text-indigo-600 hover:underline"
                    onClick={() => {
                      setEditingId(s.id);
                      setForm({
                        academicYearId: s.academicYearId, gradeId: s.gradeId, title: s.title,
                        amount: String(s.amount), dueDate: s.dueDate ?? "",
                      });
                    }}>
                    Edit
                  </button>
                  <button className="text-xs font-medium text-red-600 hover:underline"
                    onClick={() => { if (confirm("Delete this fee head?")) remove.mutate(s.id); }}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {list.data && list.data.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={6}>No fee heads defined yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
