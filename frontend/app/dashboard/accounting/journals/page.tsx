"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { FinancialYear, Journal, LedgerAccount, Page } from "@/lib/types";

type Line = { accountId: string; debit: string; credit: string; memo: string };
const emptyLine = (): Line => ({ accountId: "", debit: "", credit: "", memo: "" });

export default function JournalsPage() {
  const qc = useQueryClient();
  const years = useQuery({ queryKey: ["fin-years"], queryFn: () => api<FinancialYear[]>("/accounting/financial-years") });
  const accounts = useQuery({ queryKey: ["accounts"], queryFn: () => api<LedgerAccount[]>("/accounting/accounts") });
  const [fyId, setFyId] = useState("");
  const currentFy = years.data?.find((y) => y.current);
  const activeFy = fyId || currentFy?.id || "";

  const [entryDate, setEntryDate] = useState(new Date().toISOString().slice(0, 10));
  const [reference, setReference] = useState("");
  const [narration, setNarration] = useState("");
  const [lines, setLines] = useState<Line[]>([emptyLine(), emptyLine()]);
  const [post, setPost] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const list = useQuery({
    queryKey: ["journals", activeFy],
    queryFn: () => api<Page<Journal>>(`/accounting/journals?size=50${activeFy ? `&financialYearId=${activeFy}` : ""}`),
    enabled: !!activeFy,
  });

  const totals = useMemo(() => {
    let d = 0, c = 0;
    lines.forEach((l) => { d += Number(l.debit || 0); c += Number(l.credit || 0); });
    return { d, c, balanced: d === c && d > 0 };
  }, [lines]);

  const create = useMutation({
    mutationFn: () => api("/accounting/journals", {
      method: "POST",
      body: JSON.stringify({
        financialYearId: activeFy, entryDate, reference, narration, post,
        lines: lines.filter((l) => l.accountId).map((l) => ({
          accountId: l.accountId, debit: Number(l.debit || 0), credit: Number(l.credit || 0), memo: l.memo || null,
        })),
      }),
    }),
    onSuccess: () => {
      setLines([emptyLine(), emptyLine()]); setReference(""); setNarration(""); setErr(null);
      qc.invalidateQueries({ queryKey: ["journals"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const postMut = useMutation({
    mutationFn: (id: string) => api(`/accounting/journals/${id}/post`, { method: "PATCH" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["journals"] }),
  });

  function setLine(i: number, patch: Partial<Line>) {
    setLines(lines.map((l, idx) => (idx === i ? { ...l, ...patch } : l)));
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Journals</h1>
        <select className="input max-w-[12rem]" value={activeFy} onChange={(e) => setFyId(e.target.value)}>
          {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}{y.current ? " (current)" : ""}</option>)}
        </select>
      </div>

      <form className="card space-y-3" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <div className="flex flex-wrap gap-2">
          <input className="input max-w-[10rem]" type="date" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} />
          <input className="input max-w-[10rem]" placeholder="Reference" value={reference} onChange={(e) => setReference(e.target.value)} />
          <input className="input flex-1" placeholder="Narration" value={narration} onChange={(e) => setNarration(e.target.value)} />
        </div>
        <table className="min-w-full">
          <thead><tr><th className="th">Account</th><th className="th">Debit</th><th className="th">Credit</th><th className="th">Memo</th><th className="th"></th></tr></thead>
          <tbody>
            {lines.map((l, i) => (
              <tr key={i}>
                <td className="td">
                  <select className="input" value={l.accountId} onChange={(e) => setLine(i, { accountId: e.target.value })}>
                    <option value="">Account…</option>
                    {accounts.data?.map((a) => <option key={a.id} value={a.id}>{a.code} · {a.name}</option>)}
                  </select>
                </td>
                <td className="td"><input className="input max-w-[8rem]" type="number" value={l.debit} onChange={(e) => setLine(i, { debit: e.target.value, credit: "" })} /></td>
                <td className="td"><input className="input max-w-[8rem]" type="number" value={l.credit} onChange={(e) => setLine(i, { credit: e.target.value, debit: "" })} /></td>
                <td className="td"><input className="input" value={l.memo} onChange={(e) => setLine(i, { memo: e.target.value })} /></td>
                <td className="td">{lines.length > 2 && <button type="button" className="text-red-600" onClick={() => setLines(lines.filter((_, idx) => idx !== i))}>✕</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="flex flex-wrap items-center gap-3">
          <button type="button" className="btn-ghost" onClick={() => setLines([...lines, emptyLine()])}>+ Add line</button>
          <span className={"text-sm " + (totals.balanced ? "text-emerald-600" : "text-red-600")}>
            Debit {totals.d} · Credit {totals.c} · {totals.balanced ? "balanced" : "not balanced"}
          </span>
          <label className="flex items-center gap-1 text-sm text-slate-600"><input type="checkbox" checked={post} onChange={(e) => setPost(e.target.checked)} /> post immediately</label>
          <button className="btn" disabled={!activeFy || !totals.balanced || create.isPending}>Save journal</button>
          {err && <span className="text-sm text-red-600">{err}</span>}
        </div>
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50"><tr><th className="th">Date</th><th className="th">Ref</th><th className="th">Narration</th><th className="th">Debit</th><th className="th">Src</th><th className="th">Status</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((j) => (
              <tr key={j.id}>
                <td className="td">{j.entryDate}</td>
                <td className="td">{j.reference ?? "—"}</td>
                <td className="td">{j.narration ?? "—"}</td>
                <td className="td">{j.totalDebit}</td>
                <td className="td text-xs">{j.source}</td>
                <td className={"td font-medium " + (j.posted ? "text-emerald-600" : "text-amber-600")}>{j.posted ? "Posted" : "Draft"}</td>
                <td className="td">{!j.posted && <button className="text-xs text-indigo-600" onClick={() => postMut.mutate(j.id)}>Post</button>}</td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && <tr><td className="td text-slate-400" colSpan={7}>No journals yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
