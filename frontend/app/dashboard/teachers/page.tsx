"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Page, Teacher } from "@/lib/types";
import { DESIGNATIONS } from "@/lib/types";
import { DetailModal } from "@/components/DetailModal";

export default function TeachersPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ fullName: "", designation: "SUBJECT", phone: "" });
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Teacher | null>(null);

  const list = useQuery({
    queryKey: ["teachers"],
    queryFn: () => api<Page<Teacher>>("/teachers?size=50"),
  });

  const create = useMutation({
    mutationFn: () => api<Teacher>("/teachers", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ fullName: "", designation: "SUBJECT", phone: "" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["teachers"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Teachers</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div>
          <label className="label">Full name</label>
          <input className="input" required value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </div>
        <div>
          <label className="label">Designation</label>
          <select className="input" value={form.designation}
            onChange={(e) => setForm({ ...form, designation: e.target.value })}>
            {DESIGNATIONS.map((d) => <option key={d}>{d}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Phone</label>
          <input className="input" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Add teacher</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Name</th><th className="th">Designation</th><th className="th">Phone</th><th className="th">Status</th><th className="th">Actions</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((t) => (
              <TeacherRow key={t.id} teacher={t} onView={() => setViewing(t)} onChange={() => qc.invalidateQueries({ queryKey: ["teachers"] })} />
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={5}>No teachers yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {viewing && (
        <DetailModal
          title={viewing.fullName}
          subtitle={`Teacher · ${viewing.designation} · ${viewing.status}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Full name", value: viewing.fullName },
            { label: "Designation", value: viewing.designation },
            { label: "Gender", value: viewing.gender },
            { label: "Date of birth", value: viewing.dob },
            { label: "Join date", value: viewing.joinDate },
            { label: "Phone", value: viewing.phone },
            { label: "Email", value: viewing.email },
            { label: "Address", value: viewing.address },
            { label: "Status", value: viewing.status },
          ]}
        />
      )}
    </div>
  );
}

function TeacherRow({ teacher, onView, onChange }: { teacher: Teacher; onView: () => void; onChange: () => void }) {
  const [edit, setEdit] = useState(false);
  const [f, setF] = useState({ fullName: teacher.fullName, designation: teacher.designation, phone: teacher.phone ?? "", status: teacher.status });
  const [err, setErr] = useState<string | null>(null);

  const save = useMutation({
    mutationFn: () => api(`/teachers/${teacher.id}`, { method: "PUT", body: JSON.stringify(f) }),
    onSuccess: () => { setEdit(false); setErr(null); onChange(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: () => api(`/teachers/${teacher.id}`, { method: "DELETE" }),
    onSuccess: onChange,
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  if (edit) {
    return (
      <tr>
        <td className="td"><input className="input" value={f.fullName} onChange={(e) => setF({ ...f, fullName: e.target.value })} /></td>
        <td className="td"><select className="input" value={f.designation} onChange={(e) => setF({ ...f, designation: e.target.value })}>{DESIGNATIONS.map((d) => <option key={d}>{d}</option>)}</select></td>
        <td className="td"><input className="input max-w-[8rem]" value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} /></td>
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
      <td className="td font-medium">{teacher.fullName}</td>
      <td className="td">{teacher.designation}</td>
      <td className="td">{teacher.phone ?? "—"}</td>
      <td className="td">{teacher.status}</td>
      <td className="td space-x-2 whitespace-nowrap">
        <button className="text-xs text-slate-600 hover:underline" onClick={onView}>View</button>
        <button className="text-xs text-indigo-600" onClick={() => setEdit(true)}>Edit</button>
        <button className="text-xs text-red-600" onClick={() => { if (confirm(`Delete ${teacher.fullName}?`)) del.mutate(); }}>Delete</button>
        {err && <span className="text-xs text-red-600">{err}</span>}
      </td>
    </tr>
  );
}
