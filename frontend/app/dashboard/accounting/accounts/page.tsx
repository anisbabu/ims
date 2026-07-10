"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { FinancialYear, LedgerAccount } from "@/lib/types";
import { ACCOUNT_TYPES } from "@/lib/types";
import { DetailModal } from "@/components/DetailModal";

export default function AccountsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Chart of accounts</h1>
      <FinancialYears />
      <ChartOfAccounts />
    </div>
  );
}

function FinancialYears() {
  const qc = useQueryClient();
  const list = useQuery({ queryKey: ["fin-years"], queryFn: () => api<FinancialYear[]>("/accounting/financial-years") });
  const [form, setForm] = useState({ name: "", startDate: "", endDate: "", current: true });
  const [err, setErr] = useState<string | null>(null);

  const create = useMutation({
    mutationFn: () => api("/accounting/financial-years", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => { setForm({ name: "", startDate: "", endDate: "", current: true }); setErr(null); qc.invalidateQueries({ queryKey: ["fin-years"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const setCurrent = useMutation({
    mutationFn: (id: string) => api(`/accounting/financial-years/${id}/current`, { method: "PATCH" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["fin-years"] }),
  });
  const close = useMutation({
    mutationFn: (id: string) => api(`/accounting/financial-years/${id}/close`, { method: "PATCH" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["fin-years"] }),
  });

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Financial years</h2>
      <form className="flex flex-wrap items-end gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[10rem]" placeholder="FY 2027" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input className="input max-w-[10rem]" type="date" required value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
        <input className="input max-w-[10rem]" type="date" required value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
        <label className="flex items-center gap-1 text-sm text-slate-600"><input type="checkbox" checked={form.current} onChange={(e) => setForm({ ...form, current: e.target.checked })} /> current</label>
        <button className="btn" disabled={create.isPending}>Add</button>
        {err && <span className="text-sm text-red-600">{err}</span>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Name</th><th className="th">Period</th><th className="th">Status</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((y) => (
              <tr key={y.id}>
                <td className="td font-medium">{y.name}{y.current && <span className="ml-2 rounded bg-indigo-600 px-1.5 text-xs text-white">current</span>}</td>
                <td className="td">{y.startDate} → {y.endDate}</td>
                <td className="td">{y.closed ? "Closed" : "Open"}</td>
                <td className="td space-x-2">
                  {!y.current && <button className="text-xs text-indigo-600" onClick={() => setCurrent.mutate(y.id)}>Set current</button>}
                  {!y.closed && <button className="text-xs text-red-600" onClick={() => close.mutate(y.id)}>Close</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function ChartOfAccounts() {
  const qc = useQueryClient();
  const list = useQuery({ queryKey: ["accounts"], queryFn: () => api<LedgerAccount[]>("/accounting/accounts") });
  const [form, setForm] = useState({ code: "", name: "", type: "ASSET" });
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<LedgerAccount | null>(null);

  const create = useMutation({
    mutationFn: () => api("/accounting/accounts", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => { setForm({ code: "", name: "", type: "ASSET" }); setErr(null); qc.invalidateQueries({ queryKey: ["accounts"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const typeColor: Record<string, string> = {
    ASSET: "text-blue-600", LIABILITY: "text-amber-600", EQUITY: "text-purple-600",
    INCOME: "text-emerald-600", EXPENSE: "text-red-600",
  };

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Accounts</h2>
      <form className="flex flex-wrap items-end gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[8rem]" placeholder="Code" required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
        <input className="input max-w-[16rem]" placeholder="Account name" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <select className="input max-w-[10rem]" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
          {ACCOUNT_TYPES.map((t) => <option key={t}>{t}</option>)}
        </select>
        <button className="btn" disabled={create.isPending}>Add account</button>
        {err && <span className="text-sm text-red-600">{err}</span>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Code</th><th className="th">Name</th><th className="th">Type</th><th className="th">System</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((a) => (
              <tr key={a.id}>
                <td className="td font-mono">{a.code}</td>
                <td className="td font-medium">{a.name}</td>
                <td className={"td font-medium " + (typeColor[a.type] ?? "")}>{a.type}</td>
                <td className="td text-xs text-slate-500">{a.systemKey ?? "—"}</td>
                <td className="td"><button className="text-xs text-slate-600 hover:underline" onClick={() => setViewing(a)}>View</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {viewing && (
        <DetailModal
          title={`${viewing.code} · ${viewing.name}`}
          subtitle={`Account · ${viewing.type}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Code", value: viewing.code },
            { label: "Name", value: viewing.name },
            { label: "Type", value: viewing.type },
            { label: "System key", value: viewing.systemKey },
            { label: "Active", value: viewing.active ? "Yes" : "No" },
          ]}
        />
      )}
    </section>
  );
}
