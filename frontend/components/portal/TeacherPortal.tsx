"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Section, Subject, TeacherPortalData } from "@/lib/types";

export function TeacherPortal() {
  const portal = useQuery({
    queryKey: ["portal-teacher"],
    queryFn: () => api<TeacherPortalData>("/portal/teacher"),
  });
  const subjects = useQuery({
    queryKey: ["subjects-all"],
    queryFn: () => api<Subject[]>("/subjects"),
  });
  const sections = useQuery({
    queryKey: ["sections-all"],
    queryFn: () => api<Section[]>("/sections"),
  });

  if (portal.isLoading) return <p className="text-sm text-slate-500">Loading your portal…</p>;
  if (portal.isError)
    return (
      <p className="text-sm text-red-600">
        {portal.error instanceof Error ? portal.error.message : "Failed to load portal"}
      </p>
    );

  const d = portal.data!;
  const subjectName = (id?: string) => subjects.data?.find((s) => s.id === id)?.name;
  const sectionName = (id?: string) => sections.data?.find((s) => s.id === id)?.name;

  return (
    <div className="space-y-4">
      <div className="card">
        <h1 className="text-xl font-semibold">{d.teacher.fullName}</h1>
        <p className="text-sm text-slate-500">
          {d.teacher.designation}
          {d.teacher.joinDate ? ` · joined ${d.teacher.joinDate}` : ""}
        </p>
      </div>

      <div className="card">
        <h2 className="mb-2 text-sm font-semibold">Class teacher of</h2>
        {d.classTeacherOf.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {d.classTeacherOf.map((s) => (
              <span key={s.id} className="rounded-full bg-indigo-50 px-3 py-1 text-sm text-indigo-700">
                {s.grade ? `${s.grade} · ` : ""}{s.name}
              </span>
            ))}
          </div>
        ) : (
          <p className="text-sm text-slate-400">Not assigned as class teacher of any section.</p>
        )}
      </div>

      <div className="card p-0">
        <h2 className="border-b border-slate-100 px-4 py-3 text-sm font-semibold">My teaching routine</h2>
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Day</th><th className="th">Time</th><th className="th">Section</th><th className="th">Subject</th><th className="th">Venue</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {d.routine.map((s) => (
              <tr key={s.id}>
                <td className="td">{s.dayOfWeek ?? s.slotDate ?? "—"}</td>
                <td className="td">{s.startTime.slice(0, 5)}–{s.endTime.slice(0, 5)}</td>
                <td className="td">{sectionName(s.sectionId) ?? "—"}</td>
                <td className="td font-medium">{subjectName(s.subjectId) ?? s.label ?? "—"}</td>
                <td className="td">{s.venue ?? "—"}</td>
              </tr>
            ))}
            {d.routine.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={5}>No routine slots assigned to you.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
