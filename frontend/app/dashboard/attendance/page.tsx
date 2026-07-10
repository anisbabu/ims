"use client";

import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AttendanceRecord, Grade, Page, Section, Student } from "@/lib/types";
import { ATTENDANCE_STATUSES } from "@/lib/types";

export default function AttendancePage() {
  const qc = useQueryClient();
  const today = new Date().toISOString().slice(0, 10);
  const [date, setDate] = useState(today);
  const [sectionId, setSectionId] = useState("");
  const [marks, setMarks] = useState<Record<string, string>>({});
  const [err, setErr] = useState<string | null>(null);

  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const sections = useQuery({ queryKey: ["sections"], queryFn: () => api<Section[]>("/sections") });
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=200") });
  const existing = useQuery({
    queryKey: ["attendance", date, sectionId],
    queryFn: () => api<AttendanceRecord[]>(`/attendance?date=${date}${sectionId ? `&sectionId=${sectionId}` : ""}`),
  });

  // seed marks from existing records for the chosen date
  useEffect(() => {
    if (existing.data) {
      const seed: Record<string, string> = {};
      existing.data.forEach((r) => (seed[r.studentId] = r.status));
      setMarks(seed);
    }
  }, [existing.data]);

  const gradeName = (id: string) => grades.data?.find((g) => g.id === id)?.name ?? "";
  const sectionLabel = (s: Section) => `${gradeName(s.gradeId)} — ${s.name}`;

  const save = useMutation({
    mutationFn: () => {
      const entries = Object.entries(marks)
        .filter(([, status]) => !!status)
        .map(([studentId, status]) => ({ studentId, status }));
      return api("/attendance", { method: "POST", body: JSON.stringify({ date, sectionId: sectionId || null, entries }) });
    },
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["attendance"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const roster = students.data?.content ?? [];

  function setAll(status: string) {
    const next: Record<string, string> = {};
    roster.forEach((s) => (next[s.id] = status));
    setMarks(next);
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Attendance</h1>

      <div className="card flex flex-wrap items-end gap-3">
        <div>
          <label className="label">Date</label>
          <input className="input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
        <div>
          <label className="label">Section (tag only)</label>
          <select className="input" value={sectionId} onChange={(e) => setSectionId(e.target.value)}>
            <option value="">—</option>
            {sections.data?.map((s) => <option key={s.id} value={s.id}>{sectionLabel(s)}</option>)}
          </select>
        </div>
        <button className="btn-ghost" onClick={() => setAll("PRESENT")}>All present</button>
        <button className="btn-ghost" onClick={() => setAll("ABSENT")}>All absent</button>
        <button className="btn" disabled={save.isPending} onClick={() => save.mutate()}>Save attendance</button>
        {err && <span className="text-sm text-red-600">{err}</span>}
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Student</th><th className="th">Status</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {roster.map((s) => (
              <tr key={s.id}>
                <td className="td font-medium">{s.fullName}</td>
                <td className="td">
                  <select className="input max-w-[10rem]" value={marks[s.id] ?? ""}
                    onChange={(e) => setMarks({ ...marks, [s.id]: e.target.value })}>
                    <option value="">—</option>
                    {ATTENDANCE_STATUSES.map((st) => <option key={st}>{st}</option>)}
                  </select>
                </td>
              </tr>
            ))}
            {roster.length === 0 && <tr><td className="td text-slate-400" colSpan={2}>No students.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
