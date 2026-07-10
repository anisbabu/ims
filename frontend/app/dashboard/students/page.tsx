"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Guardian, Page, Student } from "@/lib/types";
import { RELATIONS } from "@/lib/types";
import { DetailModal } from "@/components/DetailModal";

export default function StudentsPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [form, setForm] = useState({ fullName: "", regNo: "", gender: "MALE" });
  const [err, setErr] = useState<string | null>(null);
  const [selected, setSelected] = useState<Student | null>(null);
  const [viewing, setViewing] = useState<Student | null>(null);

  const list = useQuery({
    queryKey: ["students", q],
    queryFn: () => api<Page<Student>>(`/students?size=50${q ? `&q=${encodeURIComponent(q)}` : ""}`),
  });

  const create = useMutation({
    mutationFn: () => api<Student>("/students", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ fullName: "", regNo: "", gender: "MALE" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["students"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Students</h1>

      <form
        className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}
      >
        <div>
          <label className="label">Full name</label>
          <input className="input" required value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </div>
        <div>
          <label className="label">Reg no</label>
          <input className="input" value={form.regNo}
            onChange={(e) => setForm({ ...form, regNo: e.target.value })} />
        </div>
        <div>
          <label className="label">Gender</label>
          <select className="input" value={form.gender}
            onChange={(e) => setForm({ ...form, gender: e.target.value })}>
            <option>MALE</option><option>FEMALE</option><option>OTHER</option>
          </select>
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Add student</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="flex items-center gap-2">
        <input className="input max-w-xs" placeholder="Search name…" value={q}
          onChange={(e) => setQ(e.target.value)} />
        <span className="text-sm text-slate-500">{list.data?.totalElements ?? 0} total</span>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="th">Name</th><th className="th">Reg</th><th className="th">Roll</th>
              <th className="th">Gender</th><th className="th">Status</th><th className="th">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((s) => (
              <StudentRow key={s.id} student={s}
                onView={() => setViewing(s)}
                onGuardians={() => setSelected(s)}
                onChange={() => qc.invalidateQueries({ queryKey: ["students"] })} />
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={6}>No students yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && <GuardianLinker student={selected} onClose={() => setSelected(null)} />}
      {viewing && <StudentDetail student={viewing} onClose={() => setViewing(null)} />}
    </div>
  );
}

function StudentDetail({ student, onClose }: { student: Student; onClose: () => void }) {
  const guardians = useQuery({
    queryKey: ["guardians-all"],
    queryFn: () => api<Page<Guardian>>("/guardians?size=100"),
  });
  const links = useQuery({
    queryKey: ["student-guardians", student.id],
    queryFn: () => api<{ id: string; guardianId: string; relation: string; primary: boolean }[]>(
      `/students/${student.id}/guardians`
    ),
  });
  const gName = (id: string) => guardians.data?.content.find((x) => x.id === id)?.fullName ?? id;

  return (
    <DetailModal
      title={student.fullName}
      subtitle={`Student · ${student.status}`}
      onClose={onClose}
      fields={[
        { label: "Full name", value: student.fullName },
        { label: "Registration no", value: student.regNo },
        { label: "Roll no", value: student.rollNo },
        { label: "Gender", value: student.gender },
        { label: "Date of birth", value: student.dob },
        { label: "Phone", value: student.phone },
        { label: "Email", value: student.email },
        { label: "Address", value: student.address },
        { label: "Status", value: student.status },
      ]}
    >
      <div>
        <h3 className="mb-2 text-sm font-semibold text-slate-700">Guardians</h3>
        <ul className="space-y-1 text-sm">
          {links.data?.map((l) => (
            <li key={l.id} className="flex justify-between rounded border border-slate-200 px-3 py-1.5">
              <span>{gName(l.guardianId)}{l.primary && <span className="ml-2 text-xs text-indigo-600">primary</span>}</span>
              <span className="text-slate-500">{l.relation}</span>
            </li>
          ))}
          {links.data?.length === 0 && <li className="text-slate-400">No guardians linked.</li>}
        </ul>
      </div>
    </DetailModal>
  );
}

function StudentRow({ student, onView, onGuardians, onChange }: { student: Student; onView: () => void; onGuardians: () => void; onChange: () => void }) {
  const [edit, setEdit] = useState(false);
  const [f, setF] = useState({ fullName: student.fullName, rollNo: student.rollNo ?? "", gender: student.gender ?? "MALE", status: student.status });
  const [err, setErr] = useState<string | null>(null);

  const save = useMutation({
    mutationFn: () => api(`/students/${student.id}`, { method: "PUT", body: JSON.stringify(f) }),
    onSuccess: () => { setEdit(false); setErr(null); onChange(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: () => api(`/students/${student.id}`, { method: "DELETE" }),
    onSuccess: onChange,
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  if (edit) {
    return (
      <tr>
        <td className="td"><input className="input" value={f.fullName} onChange={(e) => setF({ ...f, fullName: e.target.value })} /></td>
        <td className="td">{student.regNo ?? "—"}</td>
        <td className="td"><input className="input max-w-[5rem]" value={f.rollNo} onChange={(e) => setF({ ...f, rollNo: e.target.value })} /></td>
        <td className="td"><select className="input" value={f.gender} onChange={(e) => setF({ ...f, gender: e.target.value })}><option>MALE</option><option>FEMALE</option><option>OTHER</option></select></td>
        <td className="td"><select className="input" value={f.status} onChange={(e) => setF({ ...f, status: e.target.value })}><option>ACTIVE</option><option>INACTIVE</option></select></td>
        <td className="td space-x-2 whitespace-nowrap">
          <button className="text-xs text-indigo-600" onClick={() => save.mutate()}>Save</button>
          <button className="text-xs text-slate-500" onClick={() => setEdit(false)}>Cancel</button>
          {err && <span className="text-xs text-red-600">{err}</span>}
        </td>
      </tr>
    );
  }

  return (
    <tr>
      <td className="td font-medium">{student.fullName}</td>
      <td className="td">{student.regNo ?? "—"}</td>
      <td className="td">{student.rollNo ?? "—"}</td>
      <td className="td">{student.gender ?? "—"}</td>
      <td className="td">{student.status}</td>
      <td className="td space-x-2 whitespace-nowrap">
        <button className="text-xs text-slate-600 hover:underline" onClick={onView}>View</button>
        <button className="text-xs text-indigo-600" onClick={() => setEdit(true)}>Edit</button>
        <button className="text-xs text-red-600" onClick={() => { if (confirm(`Delete ${student.fullName}?`)) del.mutate(); }}>Delete</button>
        <button className="text-xs text-slate-500 hover:underline" onClick={onGuardians}>Guardians</button>
        {err && <span className="text-xs text-red-600">{err}</span>}
      </td>
    </tr>
  );
}

function GuardianLinker({ student, onClose }: { student: Student; onClose: () => void }) {
  const qc = useQueryClient();
  const [guardianId, setGuardianId] = useState("");
  const [relation, setRelation] = useState("FATHER");
  const [err, setErr] = useState<string | null>(null);

  const guardians = useQuery({
    queryKey: ["guardians-all"],
    queryFn: () => api<Page<Guardian>>("/guardians?size=100"),
  });
  const links = useQuery({
    queryKey: ["student-guardians", student.id],
    queryFn: () => api<{ id: string; guardianId: string; relation: string; primary: boolean }[]>(
      `/students/${student.id}/guardians`
    ),
  });

  const link = useMutation({
    mutationFn: () =>
      api(`/students/${student.id}/guardians`, {
        method: "POST",
        body: JSON.stringify({ guardianId, relation, primary: false }),
      }),
    onSuccess: () => {
      setErr(null);
      qc.invalidateQueries({ queryKey: ["student-guardians", student.id] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const unlink = useMutation({
    mutationFn: (linkId: string) => api(`/student-guardians/${linkId}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["student-guardians", student.id] }),
  });

  return (
    <div className="fixed inset-0 grid place-items-center bg-black/30 p-4" onClick={onClose}>
      <div className="card w-full max-w-md space-y-3" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="font-semibold">Guardians of {student.fullName}</h2>
          <button className="btn-ghost" onClick={onClose}>Close</button>
        </div>
        <ul className="space-y-1 text-sm">
          {links.data?.map((l) => {
            const g = guardians.data?.content.find((x) => x.id === l.guardianId);
            return (
              <li key={l.id} className="flex items-center justify-between rounded border border-slate-200 px-2 py-1">
                <span>{g?.fullName ?? l.guardianId}</span>
                <span className="flex items-center gap-2"><span className="text-slate-500">{l.relation}</span>
                  <button className="text-xs text-red-600" onClick={() => unlink.mutate(l.id)}>✕</button></span>
              </li>
            );
          })}
          {links.data?.length === 0 && <li className="text-slate-400">None linked.</li>}
        </ul>
        <div className="flex gap-2">
          <select className="input" value={guardianId} onChange={(e) => setGuardianId(e.target.value)}>
            <option value="">Pick guardian…</option>
            {guardians.data?.content.map((g) => (
              <option key={g.id} value={g.id}>{g.fullName}</option>
            ))}
          </select>
          <select className="input max-w-[8rem]" value={relation} onChange={(e) => setRelation(e.target.value)}>
            {RELATIONS.map((r) => <option key={r}>{r}</option>)}
          </select>
          <button className="btn" disabled={!guardianId || link.isPending} onClick={() => link.mutate()}>
            Link
          </button>
        </div>
        {err && <p className="text-sm text-red-600">{err}</p>}
      </div>
    </div>
  );
}
