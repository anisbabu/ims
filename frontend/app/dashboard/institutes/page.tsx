"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Page } from "@/lib/types";
import { useMe } from "@/lib/hooks";
import { DetailModal } from "@/components/DetailModal";

interface Institute {
  id: string;
  name: string;
  code: string;
  address?: string;
  phone?: string;
  email?: string;
  logoUrl?: string;
  settings?: string;
  status: string;
}

export default function InstitutesPage() {
  const { data: me } = useMe();
  const qc = useQueryClient();
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Institute | null>(null);
  const [form, setForm] = useState({
    name: "", code: "", adminEmail: "", adminPassword: "", adminFullName: "",
  });

  const isSuper = me?.role === "SUPER_ADMIN";

  const list = useQuery({
    queryKey: ["institutes"],
    queryFn: () => api<Page<Institute>>("/institutes?size=50"),
    enabled: isSuper,
  });

  const create = useMutation({
    mutationFn: () => api("/institutes", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => {
      setForm({ name: "", code: "", adminEmail: "", adminPassword: "", adminFullName: "" });
      setErr(null);
      qc.invalidateQueries({ queryKey: ["institutes"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  if (!isSuper) {
    return (
      <div className="card">
        <h1 className="text-xl font-semibold">Institutes</h1>
        <p className="mt-2 text-sm text-slate-500">
          Only the platform SUPER_ADMIN can create and list institutes.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Institutes (platform)</h1>
      <form className="card grid grid-cols-1 gap-3 md:grid-cols-3"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div>
          <label className="label">Institute name</label>
          <input className="input" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div>
          <label className="label">Code</label>
          <input className="input" required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
        </div>
        <div>
          <label className="label">Admin full name</label>
          <input className="input" required value={form.adminFullName} onChange={(e) => setForm({ ...form, adminFullName: e.target.value })} />
        </div>
        <div>
          <label className="label">Admin email</label>
          <input className="input" type="email" required value={form.adminEmail} onChange={(e) => setForm({ ...form, adminEmail: e.target.value })} />
        </div>
        <div>
          <label className="label">Admin password</label>
          <input className="input" type="password" required minLength={8} value={form.adminPassword} onChange={(e) => setForm({ ...form, adminPassword: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={create.isPending}>Create institute</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Name</th><th className="th">Code</th><th className="th">Status</th><th className="th"></th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((i) => (
              <tr key={i.id}>
                <td className="td font-medium">{i.name}</td>
                <td className="td">{i.code}</td>
                <td className="td">{i.status}</td>
                <td className="td"><button className="text-xs text-slate-600 hover:underline" onClick={() => setViewing(i)}>View</button></td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={4}>No institutes yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {viewing && (
        <DetailModal
          title={viewing.name}
          subtitle={`Institute · ${viewing.code} · ${viewing.status}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Name", value: viewing.name },
            { label: "Code", value: viewing.code },
            { label: "Status", value: viewing.status },
            { label: "Phone", value: viewing.phone },
            { label: "Email", value: viewing.email },
            { label: "Address", value: viewing.address },
            { label: "Logo URL", value: viewing.logoUrl },
            { label: "Settings", value: viewing.settings },
          ]}
        />
      )}
    </div>
  );
}
