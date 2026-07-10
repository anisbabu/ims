"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AcademicYear,
  Exam,
  ExamType,
  Grade,
  Marksheet,
  Page,
  Student,
  Subject,
} from "@/lib/types";
import { EXAM_STATUSES } from "@/lib/types";

export default function ExamsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Exams &amp; Results</h1>
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Subjects />
        <ExamTypes />
      </div>
      <Exams />
      <MarksAndSheet />
    </div>
  );
}

function Subjects() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const list = useQuery({ queryKey: ["subjects"], queryFn: () => api<Subject[]>("/subjects") });
  const create = useMutation({
    mutationFn: () => api("/subjects", { method: "POST", body: JSON.stringify({ name, code }) }),
    onSuccess: () => { setName(""); setCode(""); qc.invalidateQueries({ queryKey: ["subjects"] }); },
  });
  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Subjects</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[12rem]" placeholder="Mathematics" required value={name} onChange={(e) => setName(e.target.value)} />
        <input className="input max-w-[6rem]" placeholder="MATH" value={code} onChange={(e) => setCode(e.target.value)} />
        <button className="btn" disabled={create.isPending}>Add</button>
      </form>
      <div className="flex flex-wrap gap-2">
        {list.data?.map((s) => <span key={s.id} className="rounded-full bg-slate-100 px-3 py-1 text-sm">{s.name}</span>)}
        {list.data?.length === 0 && <span className="text-sm text-slate-400">None yet.</span>}
      </div>
    </section>
  );
}

function ExamTypes() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const list = useQuery({ queryKey: ["exam-types"], queryFn: () => api<ExamType[]>("/exam-types") });
  const create = useMutation({
    mutationFn: () => api("/exam-types", { method: "POST", body: JSON.stringify({ name }) }),
    onSuccess: () => { setName(""); qc.invalidateQueries({ queryKey: ["exam-types"] }); },
  });
  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Exam types</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[12rem]" placeholder="Midterm / Final / Class Test" required value={name} onChange={(e) => setName(e.target.value)} />
        <button className="btn" disabled={create.isPending}>Add</button>
      </form>
      <div className="flex flex-wrap gap-2">
        {list.data?.map((t) => <span key={t.id} className="rounded-full bg-slate-100 px-3 py-1 text-sm">{t.name}</span>)}
        {list.data?.length === 0 && <span className="text-sm text-slate-400">None yet.</span>}
      </div>
    </section>
  );
}

function Exams() {
  const qc = useQueryClient();
  const types = useQuery({ queryKey: ["exam-types"], queryFn: () => api<ExamType[]>("/exam-types") });
  const years = useQuery({ queryKey: ["years"], queryFn: () => api<AcademicYear[]>("/academic-years") });
  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const list = useQuery({ queryKey: ["exams"], queryFn: () => api<Page<Exam>>("/exams?size=50") });
  const [form, setForm] = useState({ name: "", examTypeId: "", academicYearId: "", gradeId: "" });
  const [err, setErr] = useState<string | null>(null);

  const create = useMutation({
    mutationFn: () => api("/exams", { method: "POST", body: JSON.stringify({ ...form, gradeId: form.gradeId || null }) }),
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["exams"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const setStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api(`/exams/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exams"] }),
  });

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Exams</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-5" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input" placeholder="Final Term 2026" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <select className="input" required value={form.examTypeId} onChange={(e) => setForm({ ...form, examTypeId: e.target.value })}>
          <option value="">Type…</option>
          {types.data?.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
        </select>
        <select className="input" required value={form.academicYearId} onChange={(e) => setForm({ ...form, academicYearId: e.target.value })}>
          <option value="">Year…</option>
          {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}</option>)}
        </select>
        <select className="input" value={form.gradeId} onChange={(e) => setForm({ ...form, gradeId: e.target.value })}>
          <option value="">Grade (any)</option>
          {grades.data?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
        </select>
        <button className="btn" disabled={create.isPending}>Create exam</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Exam</th><th className="th">Status</th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((x) => (
              <tr key={x.id}>
                <td className="td font-medium">{x.name}</td>
                <td className="td">
                  <select className="input max-w-[10rem]" value={x.status} onChange={(e) => setStatus.mutate({ id: x.id, status: e.target.value })}>
                    {EXAM_STATUSES.map((s) => <option key={s}>{s}</option>)}
                  </select>
                </td>
              </tr>
            ))}
            {list.data?.content.length === 0 && <tr><td className="td text-slate-400" colSpan={2}>No exams yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function MarksAndSheet() {
  const exams = useQuery({ queryKey: ["exams"], queryFn: () => api<Page<Exam>>("/exams?size=50") });
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=100") });
  const subjects = useQuery({ queryKey: ["subjects"], queryFn: () => api<Subject[]>("/subjects") });

  const [examId, setExamId] = useState("");
  const [studentId, setStudentId] = useState("");
  const [entries, setEntries] = useState<Record<string, { max: string; obt: string }>>({});
  const [err, setErr] = useState<string | null>(null);
  const [sheet, setSheet] = useState<Marksheet | null>(null);

  const rows = useMemo(() => subjects.data ?? [], [subjects.data]);

  const save = useMutation({
    mutationFn: () => {
      const payload = {
        studentId,
        entries: rows
          .filter((s) => entries[s.id]?.obt !== undefined && entries[s.id]?.obt !== "")
          .map((s) => ({
            subjectId: s.id,
            maxMarks: Number(entries[s.id]?.max || 100),
            obtainedMarks: Number(entries[s.id]?.obt || 0),
          })),
      };
      return api(`/exams/${examId}/marks`, { method: "POST", body: JSON.stringify(payload) });
    },
    onSuccess: () => { setErr(null); loadSheet(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  async function loadSheet() {
    setErr(null);
    try {
      const ms = await api<Marksheet>(`/exams/${examId}/students/${studentId}/marksheet`);
      setSheet(ms);
    } catch (e) {
      setSheet(null);
      setErr(e instanceof Error ? e.message : "No marksheet");
    }
  }

  return (
    <section className="card space-y-4">
      <h2 className="font-semibold">Enter marks &amp; view marksheet</h2>
      <div className="flex flex-wrap gap-2">
        <select className="input max-w-[16rem]" value={examId} onChange={(e) => { setExamId(e.target.value); setSheet(null); }}>
          <option value="">Select exam…</option>
          {exams.data?.content.map((x) => <option key={x.id} value={x.id}>{x.name}</option>)}
        </select>
        <select className="input max-w-[16rem]" value={studentId} onChange={(e) => { setStudentId(e.target.value); setSheet(null); }}>
          <option value="">Select student…</option>
          {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
        </select>
      </div>

      {examId && studentId && (
        <>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead><tr><th className="th">Subject</th><th className="th">Max</th><th className="th">Obtained</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {rows.map((s) => (
                  <tr key={s.id}>
                    <td className="td font-medium">{s.name}</td>
                    <td className="td">
                      <input className="input max-w-[6rem]" type="number" placeholder="100"
                        value={entries[s.id]?.max ?? ""}
                        onChange={(e) => setEntries({ ...entries, [s.id]: { max: e.target.value, obt: entries[s.id]?.obt ?? "" } })} />
                    </td>
                    <td className="td">
                      <input className="input max-w-[6rem]" type="number" placeholder="0"
                        value={entries[s.id]?.obt ?? ""}
                        onChange={(e) => setEntries({ ...entries, [s.id]: { max: entries[s.id]?.max ?? "100", obt: e.target.value } })} />
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && <tr><td className="td text-slate-400" colSpan={3}>Add subjects first.</td></tr>}
              </tbody>
            </table>
          </div>
          <div className="flex gap-2">
            <button className="btn" disabled={save.isPending} onClick={() => save.mutate()}>Save marks</button>
            <button className="btn-ghost" onClick={loadSheet}>View marksheet</button>
          </div>
        </>
      )}
      {err && <p className="text-sm text-red-600">{err}</p>}

      {sheet && (
        <div className="rounded-lg border border-slate-300 p-4">
          <div className="flex items-baseline justify-between">
            <div>
              <div className="text-lg font-semibold">{sheet.studentName}</div>
              <div className="text-sm text-slate-500">{sheet.examName}</div>
            </div>
            <div className="text-right text-sm">
              <div>Position: <b>{sheet.position ?? "—"}</b></div>
              <div>GPA: <b>{sheet.gpa}</b></div>
            </div>
          </div>
          <table className="mt-3 min-w-full divide-y divide-slate-200">
            <thead><tr><th className="th">Subject</th><th className="th">Marks</th><th className="th">%</th><th className="th">Grade</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {sheet.lines.map((l) => (
                <tr key={l.subjectId}>
                  <td className="td">{l.subjectName}</td>
                  <td className="td">{l.obtainedMarks}/{l.maxMarks}</td>
                  <td className="td">{l.percent}</td>
                  <td className={"td font-medium " + (l.pass ? "text-emerald-600" : "text-red-600")}>{l.letter}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-3 flex justify-between text-sm">
            <span>Total: <b>{sheet.totalObtained}/{sheet.totalMax}</b> ({sheet.percent}%)</span>
            <span className={sheet.pass ? "text-emerald-600" : "text-red-600"}>
              Overall: <b>{sheet.letter}</b> · {sheet.pass ? "PASS" : "FAIL"}
            </span>
          </div>
        </div>
      )}
    </section>
  );
}
