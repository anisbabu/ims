"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AcademicYear, Grade, Section, Page, Teacher } from "@/lib/types";

export default function AcademicPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Academic setup</h1>
      <Years />
      <Grades />
      <Sections />
    </div>
  );
}

function Years() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [current, setCurrent] = useState(true);
  const list = useQuery({ queryKey: ["years"], queryFn: () => api<AcademicYear[]>("/academic-years") });
  const create = useMutation({
    mutationFn: () => api("/academic-years", { method: "POST", body: JSON.stringify({ name, current }) }),
    onSuccess: () => { setName(""); qc.invalidateQueries({ queryKey: ["years"] }); },
  });
  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Academic years</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[10rem]" placeholder="e.g. 2026" required value={name}
          onChange={(e) => setName(e.target.value)} />
        <label className="flex items-center gap-1 text-sm text-slate-600">
          <input type="checkbox" checked={current} onChange={(e) => setCurrent(e.target.checked)} /> current
        </label>
        <button className="btn" disabled={create.isPending}>Add</button>
      </form>
      <div className="flex flex-wrap gap-2">
        {list.data?.map((y) => (
          <span key={y.id} className={"rounded-full px-3 py-1 text-sm " + (y.current ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-700")}>
            {y.name}{y.current ? " ★" : ""}
          </span>
        ))}
        {list.data?.length === 0 && <span className="text-sm text-slate-400">None yet.</span>}
      </div>
    </section>
  );
}

function Grades() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [orderNo, setOrderNo] = useState(0);
  const list = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const create = useMutation({
    mutationFn: () => api("/grades", { method: "POST", body: JSON.stringify({ name, orderNo: Number(orderNo) }) }),
    onSuccess: () => { setName(""); setOrderNo(0); qc.invalidateQueries({ queryKey: ["grades"] }); },
  });
  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Grades / classes</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[12rem]" placeholder="e.g. Class 5" required value={name}
          onChange={(e) => setName(e.target.value)} />
        <input className="input max-w-[6rem]" type="number" placeholder="order" value={orderNo}
          onChange={(e) => setOrderNo(Number(e.target.value))} />
        <button className="btn" disabled={create.isPending}>Add</button>
      </form>
      <div className="flex flex-wrap gap-2">
        {list.data?.map((g) => (
          <span key={g.id} className="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-700">{g.name}</span>
        ))}
        {list.data?.length === 0 && <span className="text-sm text-slate-400">None yet.</span>}
      </div>
    </section>
  );
}

function Sections() {
  const qc = useQueryClient();
  const grades = useQuery({ queryKey: ["grades"], queryFn: () => api<Grade[]>("/grades") });
  const teachers = useQuery({ queryKey: ["teachers-all"], queryFn: () => api<Page<Teacher>>("/teachers?size=100") });
  const sections = useQuery({ queryKey: ["sections"], queryFn: () => api<Section[]>("/sections") });
  const [gradeId, setGradeId] = useState("");
  const [name, setName] = useState("");
  const [classTeacherId, setClassTeacherId] = useState("");
  const [capacity, setCapacity] = useState(40);
  const create = useMutation({
    mutationFn: () => api("/sections", {
      method: "POST",
      body: JSON.stringify({ gradeId, name, classTeacherId: classTeacherId || null, capacity: Number(capacity) }),
    }),
    onSuccess: () => { setName(""); qc.invalidateQueries({ queryKey: ["sections"] }); },
  });
  const gradeName = (id: string) => grades.data?.find((g) => g.id === id)?.name ?? id;
  const teacherName = (id?: string) => teachers.data?.content.find((t) => t.id === id)?.fullName ?? "—";
  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Sections</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <select className="input max-w-[12rem]" required value={gradeId} onChange={(e) => setGradeId(e.target.value)}>
          <option value="">Grade…</option>
          {grades.data?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
        </select>
        <input className="input max-w-[6rem]" placeholder="A" required value={name} onChange={(e) => setName(e.target.value)} />
        <select className="input max-w-[12rem]" value={classTeacherId} onChange={(e) => setClassTeacherId(e.target.value)}>
          <option value="">Class teacher…</option>
          {teachers.data?.content.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
        </select>
        <input className="input max-w-[6rem]" type="number" value={capacity} onChange={(e) => setCapacity(Number(e.target.value))} />
        <button className="btn" disabled={!gradeId || create.isPending}>Add</button>
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Grade</th><th className="th">Section</th><th className="th">Class teacher</th><th className="th">Capacity</th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {sections.data?.map((s) => (
              <tr key={s.id}>
                <td className="td">{gradeName(s.gradeId)}</td>
                <td className="td font-medium">{s.name}</td>
                <td className="td">{teacherName(s.classTeacherId)}</td>
                <td className="td">{s.capacity}</td>
              </tr>
            ))}
            {sections.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={4}>None yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}
