"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { StudentPortalData, Subject } from "@/lib/types";

function Stat({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="card">
      <div className="text-2xl font-semibold">{value}</div>
      <div className="text-sm text-slate-500">{label}</div>
    </div>
  );
}

export function StudentPortal() {
  const portal = useQuery({
    queryKey: ["portal-student"],
    queryFn: () => api<StudentPortalData>("/portal/student"),
  });
  const subjects = useQuery({
    queryKey: ["subjects-all"],
    queryFn: () => api<Subject[]>("/subjects"),
  });

  if (portal.isLoading) return <p className="text-sm text-slate-500">Loading your portal…</p>;
  if (portal.isError)
    return (
      <p className="text-sm text-red-600">
        {portal.error instanceof Error ? portal.error.message : "Failed to load portal"}
      </p>
    );

  const d = portal.data!;
  const subjectName = (id?: string) => subjects.data?.find((s) => s.id === id)?.name ?? "—";
  const enrollment = d.admission
    ? [d.admission.grade, d.admission.section, d.admission.academicYear].filter(Boolean).join(" · ")
    : "Not enrolled";

  return (
    <div className="space-y-4">
      <div className="card flex items-center gap-4">
        <div>
          <h1 className="text-xl font-semibold">{d.student.fullName}</h1>
          <p className="text-sm text-slate-500">
            {enrollment}
            {d.student.rollNo ? ` · Roll ${d.student.rollNo}` : ""}
            {d.student.regNo ? ` · Reg ${d.student.regNo}` : ""}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <Stat label="Attendance (last 12 months)" value={`${d.attendance.presentPercent}%`} />
        <Stat label="Days recorded" value={d.attendance.totalDays} />
        <Stat label="Fees due" value={d.fees.totalDue} />
        <Stat label="Exams taken" value={d.marks.rows.length} />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="card p-0">
          <h2 className="border-b border-slate-100 px-4 py-3 text-sm font-semibold">My results</h2>
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr><th className="th">Exam</th><th className="th">Marks</th><th className="th">%</th><th className="th">Grade</th><th className="th">Result</th></tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {d.marks.rows.map((r) => (
                <tr key={r.examId}>
                  <td className="td font-medium">{r.examName}</td>
                  <td className="td">{r.totalObtained}/{r.totalMax}</td>
                  <td className="td">{r.percent}%</td>
                  <td className="td">{r.letter}</td>
                  <td className="td">{r.pass ? "Pass" : <span className="text-red-600">Fail</span>}</td>
                </tr>
              ))}
              {d.marks.rows.length === 0 && (
                <tr><td className="td text-slate-400" colSpan={5}>No results published yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="card p-0">
          <h2 className="border-b border-slate-100 px-4 py-3 text-sm font-semibold">My fees</h2>
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr><th className="th">Fee</th><th className="th">Amount</th><th className="th">Due</th><th className="th">Status</th></tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {d.fees.fees.map((f) => (
                <tr key={f.id}>
                  <td className="td font-medium">{f.title}</td>
                  <td className="td">{f.amount}</td>
                  <td className="td">{f.dueAmount}</td>
                  <td className="td">{f.status}</td>
                </tr>
              ))}
              {d.fees.fees.length === 0 && (
                <tr><td className="td text-slate-400" colSpan={4}>No fees billed.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card p-0">
        <h2 className="border-b border-slate-100 px-4 py-3 text-sm font-semibold">Class routine</h2>
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Day</th><th className="th">Time</th><th className="th">Subject</th><th className="th">Venue</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {d.routine.map((s) => (
              <tr key={s.id}>
                <td className="td">{s.dayOfWeek ?? "—"}</td>
                <td className="td">{s.startTime.slice(0, 5)}–{s.endTime.slice(0, 5)}</td>
                <td className="td font-medium">{subjectName(s.subjectId) || s.label || "—"}</td>
                <td className="td">{s.venue ?? "—"}</td>
              </tr>
            ))}
            {d.routine.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={4}>No routine published for your section.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
