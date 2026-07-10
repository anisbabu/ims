"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Guardian, Page } from "@/lib/types";

export default function GuardiansPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ fullName: "", phone: "", occupation: "" });
  const [err, setErr] = useState<string | null>(null);

  const list = useQuery({
    queryKey: ["guardians"],
    queryFn: () => api<Page<Guardian>>("/guardians?size=50"),
  });

  const create = useMutation({
    mutationFn: () => api<Guardian>("/guardians", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ fullName: "", phone: "", occupation: "" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["guardians"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Guardians</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div>
          <label className="label">Full name</label>
          <input className="input" required value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </div>
        <div>
          <label className="label">Phone</label>
          <input className="input" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div>
          <label className="label">Occupation</label>
          <input className="input" value={form.occupation}
            onChange={(e) => setForm({ ...form, occupation: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Add guardian</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Name</th><th className="th">Phone</th><th className="th">Occupation</th><th className="th">Actions</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((g) => (
              <GuardianRow key={g.id} guardian={g} onChange={() => qc.invalidateQueries({ queryKey: ["guardians"] })} />
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={4}>No guardians yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function GuardianRow({ guardian, onChange }: { guardian: Guardian; onChange: () => void }) {
  const [edit, setEdit] = useState(false);
  const [f, setF] = useState({ fullName: guardian.fullName, phone: guardian.phone ?? "", occupation: guardian.occupation ?? "" });
  const [err, setErr] = useState<string | null>(null);

  const save = useMutation({
    mutationFn: () => api(`/guardians/${guardian.id}`, { method: "PUT", body: JSON.stringify(f) }),
    onSuccess: () => { setEdit(false); setErr(null); onChange(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: () => api(`/guardians/${guardian.id}`, { method: "DELETE" }),
    onSuccess: onChange,
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  if (edit) {
    return (
      <tr>
        <td className="td"><input className="input" value={f.fullName} onChange={(e) => setF({ ...f, fullName: e.target.value })} /></td>
        <td className="td"><input className="input max-w-[8rem]" value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} /></td>
        <td className="td"><input className="input" value={f.occupation} onChange={(e) => setF({ ...f, occupation: e.target.value })} /></td>
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
      <td className="td font-medium">{guardian.fullName}</td>
      <td className="td">{guardian.phone ?? "—"}</td>
      <td className="td">{guardian.occupation ?? "—"}</td>
      <td className="td space-x-2 whitespace-nowrap">
        <button className="text-xs text-indigo-600" onClick={() => setEdit(true)}>Edit</button>
        <button className="text-xs text-red-600" onClick={() => { if (confirm(`Delete ${guardian.fullName}?`)) del.mutate(); }}>Delete</button>
        {err && <span className="text-xs text-red-600">{err}</span>}
      </td>
    </tr>
  );
}
