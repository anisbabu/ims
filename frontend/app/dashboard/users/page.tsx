"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AppUser, Page, Role } from "@/lib/types";
import { useMe } from "@/lib/hooks";

export default function UsersPage() {
  const { data: me } = useMe();
  const qc = useQueryClient();
  const isSuper = me?.role === "SUPER_ADMIN";
  const canManage = isSuper || me?.role === "INSTITUTE_ADMIN";

  const roles = useQuery({ queryKey: ["assignable-roles"], queryFn: () => api<Role[]>("/auth/roles"), enabled: canManage });
  const list = useQuery({ queryKey: ["users"], queryFn: () => api<Page<AppUser>>("/users?size=100"), enabled: canManage });

  const [form, setForm] = useState({ email: "", password: "", fullName: "", role: "TEACHER", instituteId: "" });
  const [err, setErr] = useState<string | null>(null);

  const create = useMutation({
    mutationFn: () => api("/users", {
      method: "POST",
      body: JSON.stringify({ ...form, instituteId: isSuper && form.instituteId ? form.instituteId : undefined }),
    }),
    onSuccess: () => { setForm({ email: "", password: "", fullName: "", role: "TEACHER", instituteId: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["users"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  if (!canManage) {
    return <div className="card"><h1 className="text-xl font-semibold">Users</h1><p className="mt-2 text-sm text-slate-500">Only administrators can manage users.</p></div>;
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Users &amp; roles</h1>

      <form className="card grid grid-cols-1 gap-3 md:grid-cols-3"
        onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div><label className="label">Full name</label><input className="input" required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
        <div><label className="label">Email</label><input className="input" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
        <div><label className="label">Password (min 8)</label><input className="input" type="password" required minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
        <div>
          <label className="label">Role</label>
          <select className="input" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            {roles.data?.map((r) => <option key={r}>{r}</option>)}
          </select>
        </div>
        {isSuper && form.role !== "SUPER_ADMIN" && (
          <div><label className="label">Institute ID</label><input className="input" placeholder="uuid (tenant)" value={form.instituteId} onChange={(e) => setForm({ ...form, instituteId: e.target.value })} /></div>
        )}
        <div className="flex items-end"><button className="btn w-full" disabled={create.isPending}>Create user</button></div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Name</th><th className="th">Email</th><th className="th">Role</th><th className="th">Status</th><th className="th">Actions</th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((u) => (
              <UserRow key={u.id} user={u} roles={roles.data ?? []} selfId={me?.id} onChange={() => qc.invalidateQueries({ queryKey: ["users"] })} />
            ))}
            {list.data && list.data.content.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No users.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function UserRow({ user, roles, selfId, onChange }: { user: AppUser; roles: Role[]; selfId?: string; onChange: () => void }) {
  const [pwOpen, setPwOpen] = useState(false);
  const [pw, setPw] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const isSelf = user.id === selfId;

  const setRole = useMutation({
    mutationFn: (role: string) => api(`/users/${user.id}`, { method: "PUT", body: JSON.stringify({ role }) }),
    onSuccess: onChange, onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const toggle = useMutation({
    mutationFn: () => api(`/users/${user.id}`, { method: "PUT", body: JSON.stringify({ status: user.status === "ACTIVE" ? "DISABLED" : "ACTIVE" }) }),
    onSuccess: onChange, onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const reset = useMutation({
    mutationFn: () => api(`/users/${user.id}/password`, { method: "PATCH", body: JSON.stringify({ newPassword: pw }) }),
    onSuccess: () => { setPwOpen(false); setPw(""); setErr(null); }, onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: () => api(`/users/${user.id}`, { method: "DELETE" }),
    onSuccess: onChange, onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  return (
    <>
      <tr>
        <td className="td font-medium">{user.fullName}{isSelf && <span className="ml-1 text-xs text-slate-400">(you)</span>}</td>
        <td className="td">{user.email}</td>
        <td className="td">
          <select className="input max-w-[10rem]" value={user.role} disabled={isSelf} onChange={(e) => setRole.mutate(e.target.value)}>
            {roles.map((r) => <option key={r}>{r}</option>)}
            {!roles.includes(user.role) && <option>{user.role}</option>}
          </select>
        </td>
        <td className={"td font-medium " + (user.status === "ACTIVE" ? "text-emerald-600" : "text-red-600")}>{user.status}</td>
        <td className="td space-x-2 whitespace-nowrap">
          <button className="text-xs text-indigo-600" onClick={() => setPwOpen(!pwOpen)}>Reset pw</button>
          {!isSelf && <button className="text-xs text-amber-600" onClick={() => toggle.mutate()}>{user.status === "ACTIVE" ? "Disable" : "Enable"}</button>}
          {!isSelf && <button className="text-xs text-red-600" onClick={() => { if (confirm("Delete user?")) del.mutate(); }}>Delete</button>}
        </td>
      </tr>
      {pwOpen && (
        <tr><td className="td" colSpan={5}>
          <div className="flex items-center gap-2">
            <input className="input max-w-[14rem]" type="password" placeholder="New password (min 8)" value={pw} onChange={(e) => setPw(e.target.value)} />
            <button className="btn" disabled={pw.length < 8 || reset.isPending} onClick={() => reset.mutate()}>Set password</button>
            {err && <span className="text-sm text-red-600">{err}</span>}
          </div>
        </td></tr>
      )}
      {err && !pwOpen && <tr><td className="td text-sm text-red-600" colSpan={5}>{err}</td></tr>}
    </>
  );
}
