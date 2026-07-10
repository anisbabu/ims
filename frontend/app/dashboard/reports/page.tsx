"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AttendanceReport,
  Exam,
  ExamResultSheet,
  Grade,
  Page,
  Section,
  Student,
  StudentMarksReport,
} from "@/lib/types";

type Tab = "attendance" | "result" | "student";

export default function ReportsPage() {
  const [tab, setTab] = useState<Tab>("attendance");
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Attendance &amp; marks reports</h1>
      <div className="flex flex-wrap gap-2">
        {([["attendance", "Attendance"], ["result", "Exam result sheet"], ["student", "Student marks"]] as [Tab, string][]).map(([k, label]) => (
          <button key={k} className={tab === k ? "btn" : "btn-ghost"} onClick={() => setTab(k)}>{label}</button>
        ))}
      </div>
      {tab === "attendance" && <AttendanceReportView />}
      {tab === "result" && <ExamResultView />}
      {tab === "student" && <StudentMarksView />}
    </div>
  );
}

function AttendanceReportView() {
  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const sections = useQuery({ queryKey: ["sections"], queryFn: () => api<Section[]>("/sections") });
  const today = new Date().toISOString().slice(0, 10);
  const first = today.slice(0, 8) + "01";
  const [from, setFrom] = useState(first);
  const [to, setTo] = useState(today);
  const [sectionId, setSectionId] = useState("");
  const [params, setParams] = useState<{ from: string; to: string; sectionId: string } | null>(null);

  const report = useQuery({
    queryKey: ["att-report", params],
    queryFn: () => api<AttendanceReport>(`/reports/attendance?from=${params!.from}&to=${params!.to}${params!.sectionId ? `&sectionId=${params!.sectionId}` : ""}`),
    enabled: !!params,
  });
  const gradeName = (id: string) => grades.data?.find((g) => g.id === id)?.name ?? "";

  return (
    <div className="space-y-3">
      <div className="card flex flex-wrap items-end gap-2">
        <div><label className="label">From</label><input className="input" type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></div>
        <div><label className="label">To</label><input className="input" type="date" value={to} onChange={(e) => setTo(e.target.value)} /></div>
        <div>
          <label className="label">Section</label>
          <select className="input" value={sectionId} onChange={(e) => setSectionId(e.target.value)}>
            <option value="">All</option>
            {sections.data?.map((s) => <option key={s.id} value={s.id}>{gradeName(s.gradeId)} — {s.name}</option>)}
          </select>
        </div>
        <button className="btn" onClick={() => setParams({ from, to, sectionId })}>Run report</button>
      </div>
      {report.data && (
        <div className="card overflow-x-auto p-0">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50"><tr><th className="th">Student</th><th className="th">Present</th><th className="th">Absent</th><th className="th">Late</th><th className="th">Excused</th><th className="th">Days</th><th className="th text-right">Present %</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {report.data.rows.map((r) => (
                <tr key={r.studentId}>
                  <td className="td font-medium">{r.studentName}</td>
                  <td className="td">{r.present}</td><td className="td">{r.absent}</td><td className="td">{r.late}</td><td className="td">{r.excused}</td><td className="td">{r.totalDays}</td>
                  <td className={"td text-right font-medium " + (r.presentPercent >= 75 ? "text-emerald-600" : r.presentPercent >= 50 ? "text-amber-600" : "text-red-600")}>{r.presentPercent}%</td>
                </tr>
              ))}
              {report.data.rows.length === 0 && <tr><td className="td text-slate-400" colSpan={7}>No attendance in range.</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function ExamResultView() {
  const exams = useQuery({ queryKey: ["exams"], queryFn: () => api<Page<Exam>>("/exams?size=50") });
  const [examId, setExamId] = useState("");
  const sheet = useQuery({
    queryKey: ["result-sheet", examId],
    queryFn: () => api<ExamResultSheet>(`/reports/exam/${examId}/result-sheet`),
    enabled: !!examId,
  });

  return (
    <div className="space-y-3">
      <select className="input max-w-[20rem]" value={examId} onChange={(e) => setExamId(e.target.value)}>
        <option value="">Select exam…</option>
        {exams.data?.content.map((x) => <option key={x.id} value={x.id}>{x.name}</option>)}
      </select>
      {sheet.data && (
        <div className="card overflow-x-auto p-0">
          <div className="border-b border-slate-200 px-4 py-2 font-semibold">{sheet.data.examName} — class result</div>
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50"><tr><th className="th">Rank</th><th className="th">Student</th><th className="th">Subjects</th><th className="th text-right">Marks</th><th className="th text-right">%</th><th className="th">GPA</th><th className="th">Grade</th><th className="th">Result</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {sheet.data.rows.map((r) => (
                <tr key={r.studentId}>
                  <td className="td font-semibold">{r.position}</td>
                  <td className="td font-medium">{r.studentName}</td>
                  <td className="td">{r.subjects}</td>
                  <td className="td text-right">{r.totalObtained}/{r.totalMax}</td>
                  <td className="td text-right">{r.percent}</td>
                  <td className="td">{r.gpa}</td>
                  <td className="td font-medium">{r.letter}</td>
                  <td className={"td font-medium " + (r.pass ? "text-emerald-600" : "text-red-600")}>{r.pass ? "PASS" : "FAIL"}</td>
                </tr>
              ))}
              {sheet.data.rows.length === 0 && <tr><td className="td text-slate-400" colSpan={8}>No marks recorded.</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function StudentMarksView() {
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=200") });
  const [studentId, setStudentId] = useState("");
  const report = useQuery({
    queryKey: ["student-marks", studentId],
    queryFn: () => api<StudentMarksReport>(`/reports/student/${studentId}/marks`),
    enabled: !!studentId,
  });

  return (
    <div className="space-y-3">
      <select className="input max-w-[20rem]" value={studentId} onChange={(e) => setStudentId(e.target.value)}>
        <option value="">Select student…</option>
        {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
      </select>
      {report.data && (
        <div className="card overflow-x-auto p-0">
          <div className="border-b border-slate-200 px-4 py-2 font-semibold">{report.data.studentName} — marks history</div>
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50"><tr><th className="th">Exam</th><th className="th text-right">Marks</th><th className="th text-right">%</th><th className="th">Grade</th><th className="th">Result</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {report.data.rows.map((r) => (
                <tr key={r.examId}>
                  <td className="td font-medium">{r.examName}</td>
                  <td className="td text-right">{r.totalObtained}/{r.totalMax}</td>
                  <td className="td text-right">{r.percent}</td>
                  <td className="td font-medium">{r.letter}</td>
                  <td className={"td font-medium " + (r.pass ? "text-emerald-600" : "text-red-600")}>{r.pass ? "PASS" : "FAIL"}</td>
                </tr>
              ))}
              {report.data.rows.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No marks yet.</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
