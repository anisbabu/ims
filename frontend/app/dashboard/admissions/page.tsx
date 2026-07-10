"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AcademicYear, Admission, Grade, Page, Section, Student } from "@/lib/types";
import { ADMISSION_STATUSES } from "@/lib/types";

export default function AdmissionsPage() {
  const qc = useQueryClient();
  const [err, setErr] = useState<string | null>(null);
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=100") });
  const years = useQuery({ queryKey: ["years"], queryFn: () => api<AcademicYear[]>("/academic-years") });
  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const sections = useQuery({ queryKey: ["sections"], queryFn: () => api<Section[]>("/sections") });
  const list = useQuery({ queryKey: ["admissions"], queryFn: () => api<Page<Admission>>("/admissions?size=50") });

  const [form, setForm] = useState({ studentId: "", academicYearId: "", gradeId: "", sectionId: "", admissionNo: "" });

  const create = useMutation({
    mutationFn: () => api("/admissions", {
      method: "POST",
      body: JSON.stringify({ ...form, sectionId: form.sectionId || null }),
    }),
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["admissions"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const setStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api(`/admissions/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admissions"] }),
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const studentName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Admissions</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-5"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div>
          <label className="label">Student</label>
          <select className="input" required value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })}>
            <option value="">Select…</option>
            {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Year</label>
          <select className="input" required value={form.academicYearId} onChange={(e) => setForm({ ...form, academicYearId: e.target.value })}>
            <option value="">Select…</option>
            {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Grade</label>
          <select className="input" required value={form.gradeId} onChange={(e) => setForm({ ...form, gradeId: e.target.value })}>
            <option value="">Select…</option>
            {grades.data?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Section</label>
          <select className="input" value={form.sectionId} onChange={(e) => setForm({ ...form, sectionId: e.target.value })}>
            <option value="">—</option>
            {sections.data?.filter((s) => !form.gradeId || s.gradeId === form.gradeId).map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Admit</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Student</th><th className="th">Adm No</th><th className="th">Status</th><th className="th">Change</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((a) => (
              <tr key={a.id}>
                <td className="td font-medium">{studentName(a.studentId)}</td>
                <td className="td">{a.admissionNo ?? "—"}</td>
                <td className="td">{a.status}</td>
                <td className="td">
                  <select className="input max-w-[10rem]" value={a.status}
                    onChange={(e) => setStatus.mutate({ id: a.id, status: e.target.value })}>
                    {ADMISSION_STATUSES.map((s) => <option key={s}>{s}</option>)}
                  </select>
                </td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={4}>No admissions yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
