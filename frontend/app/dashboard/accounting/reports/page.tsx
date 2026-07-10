"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AccountLedger,
  BalanceSheet,
  FinancialYear,
  LedgerAccount,
  ProfitAndLoss,
  TrialBalance,
} from "@/lib/types";

type Tab = "trial" | "pl" | "bs" | "ledger";

export default function ReportsPage() {
  const years = useQuery({ queryKey: ["fin-years"], queryFn: () => api<FinancialYear[]>("/accounting/financial-years") });
  const currentFy = years.data?.find((y) => y.current);
  const [fyId, setFyId] = useState("");
  const activeFy = fyId || currentFy?.id || "";
  const [tab, setTab] = useState<Tab>("trial");

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Financial reports</h1>
        <select className="input max-w-[12rem]" value={activeFy} onChange={(e) => setFyId(e.target.value)}>
          {years.data?.map((y) => <option key={y.id} value={y.id}>{y.name}{y.current ? " (current)" : ""}</option>)}
        </select>
      </div>

      <div className="flex flex-wrap gap-2">
        {([["trial", "Trial balance"], ["pl", "Profit & Loss"], ["bs", "Balance sheet"], ["ledger", "Ledger"]] as [Tab, string][]).map(([k, label]) => (
          <button key={k} className={tab === k ? "btn" : "btn-ghost"} onClick={() => setTab(k)}>{label}</button>
        ))}
      </div>

      {activeFy && tab === "trial" && <TrialBalanceView fyId={activeFy} />}
      {activeFy && tab === "pl" && <ProfitLossView fyId={activeFy} />}
      {activeFy && tab === "bs" && <BalanceSheetView fyId={activeFy} />}
      {activeFy && tab === "ledger" && <LedgerView fyId={activeFy} />}
    </div>
  );
}

const money = (n: number) => n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function TrialBalanceView({ fyId }: { fyId: string }) {
  const q = useQuery({ queryKey: ["trial", fyId], queryFn: () => api<TrialBalance>(`/accounting/reports/trial-balance?financialYearId=${fyId}`) });
  if (!q.data) return <p className="text-sm text-slate-500">Loading…</p>;
  return (
    <div className="card overflow-x-auto p-0">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50"><tr><th className="th">Code</th><th className="th">Account</th><th className="th">Type</th><th className="th text-right">Debit</th><th className="th text-right">Credit</th></tr></thead>
        <tbody className="divide-y divide-slate-100">
          {q.data.rows.map((r) => (
            <tr key={r.accountId}>
              <td className="td font-mono">{r.code}</td><td className="td">{r.name}</td><td className="td text-xs">{r.type}</td>
              <td className="td text-right">{r.debit ? money(r.debit) : ""}</td>
              <td className="td text-right">{r.credit ? money(r.credit) : ""}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="border-t-2 border-slate-300 font-semibold">
            <td className="td" colSpan={3}>Total {q.data.balanced ? "✓ balanced" : "✗ NOT balanced"}</td>
            <td className="td text-right">{money(q.data.totalDebit)}</td>
            <td className="td text-right">{money(q.data.totalCredit)}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}

function ProfitLossView({ fyId }: { fyId: string }) {
  const q = useQuery({ queryKey: ["pl", fyId], queryFn: () => api<ProfitAndLoss>(`/accounting/reports/profit-and-loss?financialYearId=${fyId}`) });
  if (!q.data) return <p className="text-sm text-slate-500">Loading…</p>;
  const d = q.data;
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      <Section title="Income" lines={d.income} total={d.totalIncome} />
      <Section title="Expense" lines={d.expense} total={d.totalExpense} />
      <div className="card md:col-span-2 flex items-center justify-between text-lg font-semibold">
        <span>Net profit</span>
        <span className={d.netProfit >= 0 ? "text-emerald-600" : "text-red-600"}>{money(d.netProfit)}</span>
      </div>
    </div>
  );
}

function Section({ title, lines, total }: { title: string; lines: { accountId: string; code: string; name: string; amount: number }[]; total: number }) {
  return (
    <div className="card">
      <h3 className="mb-2 font-semibold">{title}</h3>
      <table className="min-w-full">
        <tbody className="divide-y divide-slate-100">
          {lines.map((l) => (
            <tr key={l.accountId}><td className="td">{l.name}</td><td className="td text-right">{money(l.amount)}</td></tr>
          ))}
          {lines.length === 0 && <tr><td className="td text-slate-400">None</td></tr>}
        </tbody>
        <tfoot><tr className="border-t-2 border-slate-300 font-semibold"><td className="td">Total {title}</td><td className="td text-right">{money(total)}</td></tr></tfoot>
      </table>
    </div>
  );
}

function BalanceSheetView({ fyId }: { fyId: string }) {
  const q = useQuery({ queryKey: ["bs", fyId], queryFn: () => api<BalanceSheet>(`/accounting/reports/balance-sheet?financialYearId=${fyId}`) });
  if (!q.data) return <p className="text-sm text-slate-500">Loading…</p>;
  const d = q.data;
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      <Section title="Assets" lines={d.assets} total={d.totalAssets} />
      <div className="space-y-4">
        <Section title="Liabilities" lines={d.liabilities} total={d.totalLiabilities} />
        <Section title="Equity" lines={d.equity} total={d.totalEquity} />
        <div className="card flex items-center justify-between text-sm"><span>Net profit (unclosed)</span><span>{money(d.netProfit)}</span></div>
      </div>
      <div className="card md:col-span-2 flex items-center justify-between font-semibold">
        <span>Assets {money(d.totalAssets)} vs Liab+Equity+Profit {money(d.totalLiabilitiesAndEquity)}</span>
        <span className={d.balanced ? "text-emerald-600" : "text-red-600"}>{d.balanced ? "✓ balanced" : "✗ not balanced"}</span>
      </div>
    </div>
  );
}

function LedgerView({ fyId }: { fyId: string }) {
  const accounts = useQuery({ queryKey: ["accounts"], queryFn: () => api<LedgerAccount[]>("/accounting/accounts") });
  const [accountId, setAccountId] = useState("");
  const q = useQuery({
    queryKey: ["ledger", fyId, accountId],
    queryFn: () => api<AccountLedger>(`/accounting/reports/ledger?financialYearId=${fyId}&accountId=${accountId}`),
    enabled: !!accountId,
  });
  return (
    <div className="space-y-3">
      <select className="input max-w-[20rem]" value={accountId} onChange={(e) => setAccountId(e.target.value)}>
        <option value="">Select account…</option>
        {accounts.data?.map((a) => <option key={a.id} value={a.id}>{a.code} · {a.name}</option>)}
      </select>
      {q.data && (
        <div className="card overflow-x-auto p-0">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50"><tr><th className="th">Date</th><th className="th">Ref</th><th className="th">Narration</th><th className="th text-right">Debit</th><th className="th text-right">Credit</th><th className="th text-right">Balance</th></tr></thead>
            <tbody className="divide-y divide-slate-100">
              {q.data.rows.map((r, i) => (
                <tr key={i}>
                  <td className="td">{r.date}</td><td className="td">{r.reference ?? "—"}</td><td className="td">{r.narration ?? "—"}</td>
                  <td className="td text-right">{r.debit ? money(r.debit) : ""}</td>
                  <td className="td text-right">{r.credit ? money(r.credit) : ""}</td>
                  <td className="td text-right font-medium">{money(r.balance)}</td>
                </tr>
              ))}
              {q.data.rows.length === 0 && <tr><td className="td text-slate-400" colSpan={6}>No postings.</td></tr>}
            </tbody>
            <tfoot><tr className="border-t-2 border-slate-300 font-semibold"><td className="td" colSpan={5}>Closing balance</td><td className="td text-right">{money(q.data.closingBalance)}</td></tr></tfoot>
          </table>
        </div>
      )}
    </div>
  );
}
