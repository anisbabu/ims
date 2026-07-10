"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Exam, Grade, Page, RoutineSlot, Section, Subject, Teacher } from "@/lib/types";
import { WEEKDAYS } from "@/lib/types";

export default function RoutinePage() {
  const [tab, setTab] = useState<"CLASS" | "EXAM">("CLASS");
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Routine</h1>
      <div className="flex gap-2">
        <button className={tab === "CLASS" ? "btn" : "btn-ghost"} onClick={() => setTab("CLASS")}>Class routine</button>
        <button className={tab === "EXAM" ? "btn" : "btn-ghost"} onClick={() => setTab("EXAM")}>Exam routine</button>
      </div>
      {tab === "CLASS" ? <ClassRoutine /> : <ExamRoutine />}
    </div>
  );
}

function useLookups() {
  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const sections = useQuery({ queryKey: ["sections"], queryFn: () => api<Section[]>("/sections") });
  const subjects = useQuery({ queryKey: ["subjects"], queryFn: () => api<Subject[]>("/subjects") });
  const teachers = useQuery({ queryKey: ["teachers-all"], queryFn: () => api<Page<Teacher>>("/teachers?size=100") });
  const exams = useQuery({ queryKey: ["exams"], queryFn: () => api<Page<Exam>>("/exams?size=50") });
  return { grades, sections, subjects, teachers, exams };
}

function ClassRoutine() {
  const qc = useQueryClient();
  const { grades, sections, subjects, teachers } = useLookups();
  const [sectionId, setSectionId] = useState("");
  const [form, setForm] = useState({ dayOfWeek: "MONDAY", startTime: "09:00", endTime: "09:45", subjectId: "", teacherId: "", venue: "" });
  const [err, setErr] = useState<string | null>(null);

  const list = useQuery({
    queryKey: ["routine-class", sectionId],
    queryFn: () => api<RoutineSlot[]>(`/routines?kind=CLASS&sectionId=${sectionId}`),
    enabled: !!sectionId,
  });

  const create = useMutation({
    mutationFn: () => api("/routines", {
      method: "POST",
      body: JSON.stringify({ kind: "CLASS", sectionId, ...form, subjectId: form.subjectId || null, teacherId: form.teacherId || null }),
    }),
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["routine-class"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: (id: string) => api(`/routines/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["routine-class"] }),
  });

  const gradeName = (id?: string) => grades.data?.find((g) => g.id === id)?.name ?? "";
  const subjName = (id?: string) => subjects.data?.find((s) => s.id === id)?.name ?? "";
  const teachName = (id?: string) => teachers.data?.content.find((t) => t.id === id)?.fullName ?? "";

  const byDay = useMemo(() => {
    const map: Record<string, RoutineSlot[]> = {};
    WEEKDAYS.forEach((d) => (map[d] = []));
    (list.data ?? []).forEach((s) => { if (s.dayOfWeek) map[s.dayOfWeek]?.push(s); });
    return map;
  }, [list.data]);

  return (
    <div className="space-y-4">
      <select className="input max-w-[18rem]" value={sectionId} onChange={(e) => setSectionId(e.target.value)}>
        <option value="">Select section…</option>
        {sections.data?.map((s) => <option key={s.id} value={s.id}>{gradeName(s.gradeId)} — {s.name}</option>)}
      </select>

      {sectionId && (
        <>
          <form className="card grid grid-cols-1 gap-3 md:grid-cols-6" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
            <select className="input" value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })}>
              {WEEKDAYS.map((d) => <option key={d}>{d}</option>)}
            </select>
            <input className="input" type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
            <input className="input" type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
            <select className="input" value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })}>
              <option value="">Subject…</option>
              {subjects.data?.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <select className="input" value={form.teacherId} onChange={(e) => setForm({ ...form, teacherId: e.target.value })}>
              <option value="">Teacher…</option>
              {teachers.data?.content.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
            </select>
            <button className="btn" disabled={create.isPending}>Add period</button>
            {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
          </form>

          <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
            {WEEKDAYS.map((d) => (
              <div key={d} className="card">
                <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">{d}</h3>
                <ul className="space-y-1">
                  {byDay[d].map((s) => (
                    <li key={s.id} className="flex items-center justify-between rounded border border-slate-200 px-2 py-1 text-sm">
                      <span>{s.startTime?.slice(0, 5)}–{s.endTime?.slice(0, 5)} · {subjName(s.subjectId) || s.label || "—"}<br /><span className="text-xs text-slate-500">{teachName(s.teacherId)} {s.venue ? `· ${s.venue}` : ""}</span></span>
                      <button className="text-xs text-red-600" onClick={() => del.mutate(s.id)}>✕</button>
                    </li>
                  ))}
                  {byDay[d].length === 0 && <li className="text-xs text-slate-400">—</li>}
                </ul>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

function ExamRoutine() {
  const qc = useQueryClient();
  const { subjects, exams } = useLookups();
  const [examId, setExamId] = useState("");
  const [form, setForm] = useState({ slotDate: "", startTime: "09:00", endTime: "11:00", subjectId: "", venue: "" });
  const [err, setErr] = useState<string | null>(null);

  const list = useQuery({
    queryKey: ["routine-exam", examId],
    queryFn: () => api<RoutineSlot[]>(`/routines?kind=EXAM&examId=${examId}`),
    enabled: !!examId,
  });

  const create = useMutation({
    mutationFn: () => api("/routines", {
      method: "POST",
      body: JSON.stringify({ kind: "EXAM", examId, ...form, subjectId: form.subjectId || null }),
    }),
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["routine-exam"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: (id: string) => api(`/routines/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["routine-exam"] }),
  });

  const subjName = (id?: string) => subjects.data?.find((s) => s.id === id)?.name ?? "";

  return (
    <div className="space-y-4">
      <select className="input max-w-[18rem]" value={examId} onChange={(e) => setExamId(e.target.value)}>
        <option value="">Select exam…</option>
        {exams.data?.content.map((x) => <option key={x.id} value={x.id}>{x.name}</option>)}
      </select>

      {examId && (
        <>
          <form className="card grid grid-cols-1 gap-3 md:grid-cols-6" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
            <input className="input" type="date" value={form.slotDate} onChange={(e) => setForm({ ...form, slotDate: e.target.value })} required />
            <input className="input" type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
            <input className="input" type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
            <select className="input" value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })}>
              <option value="">Subject…</option>
              {subjects.data?.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <input className="input" placeholder="Venue" value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} />
            <button className="btn" disabled={create.isPending}>Add sitting</button>
            {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
          </form>

          <div className="card overflow-x-auto p-0">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50"><tr><th className="th">Date</th><th className="th">Time</th><th className="th">Subject</th><th className="th">Venue</th><th className="th"></th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {list.data?.map((s) => (
                  <tr key={s.id}>
                    <td className="td">{s.slotDate}</td>
                    <td className="td">{s.startTime?.slice(0, 5)}–{s.endTime?.slice(0, 5)}</td>
                    <td className="td">{subjName(s.subjectId) || "—"}</td>
                    <td className="td">{s.venue ?? "—"}</td>
                    <td className="td"><button className="text-xs text-red-600" onClick={() => del.mutate(s.id)}>Delete</button></td>
                  </tr>
                ))}
                {list.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No sittings yet.</td></tr>}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
