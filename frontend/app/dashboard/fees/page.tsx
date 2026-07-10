"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Fee, FeeSummary, Page, Student } from "@/lib/types";
import { PAYMENT_METHODS } from "@/lib/types";

export default function FeesPage() {
  const qc = useQueryClient();
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=100") });
  const [studentId, setStudentId] = useState("");
  const [form, setForm] = useState({ title: "", amount: "", dueDate: "" });
  const [err, setErr] = useState<string | null>(null);

  const fees = useQuery({
    queryKey: ["fees", studentId],
    queryFn: () => api<Page<Fee>>(`/fees?size=50${studentId ? `&studentId=${studentId}` : ""}`),
  });
  const summary = useQuery({
    queryKey: ["fee-summary", studentId],
    queryFn: () => api<FeeSummary>(`/fees/summary?studentId=${studentId}`),
    enabled: !!studentId,
  });

  const createFee = useMutation({
    mutationFn: () => api("/fees", {
      method: "POST",
      body: JSON.stringify({ studentId, title: form.title, amount: Number(form.amount), dueDate: form.dueDate || null }),
    }),
    onSuccess: () => { setForm({ title: "", amount: "", dueDate: "" }); setErr(null); refresh(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  function refresh() {
    qc.invalidateQueries({ queryKey: ["fees"] });
    qc.invalidateQueries({ queryKey: ["fee-summary"] });
  }

  const studentName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Fees</h1>

      <div className="flex flex-wrap items-center gap-2">
        <select className="input max-w-[16rem]" value={studentId} onChange={(e) => setStudentId(e.target.value)}>
          <option value="">All students</option>
          {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
        </select>
        {summary.data && (
          <div className="flex gap-4 text-sm">
            <span>Billed <b>{summary.data.totalBilled}</b></span>
            <span className="text-emerald-600">Paid <b>{summary.data.totalPaid}</b></span>
            <span className="text-red-600">Due <b>{summary.data.totalDue}</b></span>
          </div>
        )}
      </div>

      <form className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => { e.preventDefault(); if (!studentId) { setErr("Pick a student first"); return; } createFee.mutate(); }}>
        <div className="md:col-span-2">
          <label className="label">Title (student picked above)</label>
          <input className="input" required placeholder="Tuition — Term 1" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div>
          <label className="label">Amount</label>
          <input className="input" type="number" required value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
        </div>
        <div>
          <label className="label">Due date</label>
          <input className="input" type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
        </div>
        <div className="flex items-end">
          <button className="btn w-full" disabled={createFee.isPending}>Add fee</button>
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr><th className="th">Student</th><th className="th">Title</th><th className="th">Amount</th><th className="th">Paid</th><th className="th">Due</th><th className="th">Status</th><th className="th"></th></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {fees.data?.content.map((f) => (
              <FeeRow key={f.id} fee={f} name={studentName(f.studentId)} onChange={refresh} />
            ))}
            {fees.data && fees.data.content.length === 0 && (
              <tr><td className="td text-slate-400" colSpan={7}>No fees.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function FeeRow({ fee, name, onChange }: { fee: Fee; name: string; onChange: () => void }) {
  const [open, setOpen] = useState(false);
  const [amount, setAmount] = useState("");
  const [method, setMethod] = useState("CASH");
  const [err, setErr] = useState<string | null>(null);

  const pay = useMutation({
    mutationFn: () => api(`/fees/${fee.id}/payments`, { method: "POST", body: JSON.stringify({ amount: Number(amount), method }) }),
    onSuccess: () => { setOpen(false); setAmount(""); setErr(null); onChange(); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const color =
    fee.status === "PAID" ? "text-emerald-600" :
    fee.status === "PARTIAL" ? "text-amber-600" :
    fee.status === "OVERDUE" ? "text-red-600" : "text-slate-600";

  return (
    <>
      <tr>
        <td className="td font-medium">{name}</td>
        <td className="td">{fee.title}</td>
        <td className="td">{fee.amount}</td>
        <td className="td">{fee.paidAmount}</td>
        <td className="td">{fee.dueAmount}</td>
        <td className={"td font-medium " + color}>{fee.status}</td>
        <td className="td">
          {fee.status !== "PAID" && fee.status !== "WAIVED" && (
            <button className="btn-ghost" onClick={() => setOpen(!open)}>Pay</button>
          )}
        </td>
      </tr>
      {open && (
        <tr>
          <td className="td" colSpan={7}>
            <div className="flex flex-wrap items-end gap-2">
              <input className="input max-w-[8rem]" type="number" placeholder={`≤ ${fee.dueAmount}`} value={amount} onChange={(e) => setAmount(e.target.value)} />
              <select className="input max-w-[8rem]" value={method} onChange={(e) => setMethod(e.target.value)}>
                {PAYMENT_METHODS.map((m) => <option key={m}>{m}</option>)}
              </select>
              <button className="btn" disabled={!amount || pay.isPending} onClick={() => pay.mutate()}>Record payment</button>
              {err && <span className="text-sm text-red-600">{err}</span>}
            </div>
          </td>
        </tr>
      )}
    </>
  );
}
