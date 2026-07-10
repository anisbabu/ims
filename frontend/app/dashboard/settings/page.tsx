"use client";

import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useMe } from "@/lib/hooks";

export default function SettingsPage() {
  const { data: me } = useMe();
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirm, setConfirm] = useState("");
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const change = useMutation({
    mutationFn: () => api("/auth/change-password", { method: "POST", body: JSON.stringify({ currentPassword: current, newPassword: next }) }),
    onSuccess: () => { setMsg("Password changed."); setErr(null); setCurrent(""); setNext(""); setConfirm(""); },
    onError: (e) => { setMsg(null); setErr(e instanceof Error ? e.message : "Failed"); },
  });

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">My account</h1>

      <div className="card space-y-1 text-sm">
        <div><span className="text-slate-500">Name:</span> <b>{me?.fullName}</b></div>
        <div><span className="text-slate-500">Email:</span> {me?.email}</div>
        <div><span className="text-slate-500">Role:</span> {me?.role}</div>
      </div>

      <form className="card max-w-md space-y-3" onSubmit={(e) => {
        e.preventDefault();
        if (next !== confirm) { setErr("New passwords do not match"); setMsg(null); return; }
        change.mutate();
      }}>
        <h2 className="font-semibold">Change password</h2>
        <div><label className="label">Current password</label><input className="input" type="password" required value={current} onChange={(e) => setCurrent(e.target.value)} /></div>
        <div><label className="label">New password (min 8)</label><input className="input" type="password" required minLength={8} value={next} onChange={(e) => setNext(e.target.value)} /></div>
        <div><label className="label">Confirm new password</label><input className="input" type="password" required value={confirm} onChange={(e) => setConfirm(e.target.value)} /></div>
        {err && <p className="text-sm text-red-600">{err}</p>}
        {msg && <p className="text-sm text-emerald-600">{msg}</p>}
        <button className="btn" disabled={change.isPending}>Update password</button>
      </form>
    </div>
  );
}
