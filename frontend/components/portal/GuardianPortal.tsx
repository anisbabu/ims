"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { GuardianPortalData, OnlinePayment } from "@/lib/types";

export function GuardianPortal() {
  const router = useRouter();
  const checkout = useMutation({
    mutationFn: (feeId: string) =>
      api<OnlinePayment>("/payments/checkout", { method: "POST", body: JSON.stringify({ feeId }) }),
    onSuccess: (p) => {
      if (p.checkoutUrl) router.push(p.checkoutUrl);
    },
  });
  const portal = useQuery({
    queryKey: ["portal-guardian"],
    queryFn: () => api<GuardianPortalData>("/portal/guardian"),
  });

  if (portal.isLoading) return <p className="text-sm text-slate-500">Loading your portal…</p>;
  if (portal.isError)
    return (
      <p className="text-sm text-red-600">
        {portal.error instanceof Error ? portal.error.message : "Failed to load portal"}
      </p>
    );

  const d = portal.data!;

  return (
    <div className="space-y-4">
      <div className="card">
        <h1 className="text-xl font-semibold">{d.guardian.fullName}</h1>
        <p className="text-sm text-slate-500">
          Guardian portal · {d.children.length} {d.children.length === 1 ? "child" : "children"}
        </p>
      </div>

      {d.children.map((c) => (
        <div key={c.studentId} className="card space-y-3">
          <div className="flex items-baseline justify-between">
            <h2 className="text-lg font-semibold">{c.fullName}</h2>
            <span className="text-sm text-slate-500">
              {c.admission
                ? [c.admission.grade, c.admission.section, c.admission.academicYear].filter(Boolean).join(" · ")
                : "Not enrolled"}
            </span>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <div className="text-2xl font-semibold">{c.attendance.presentPercent}%</div>
              <div className="text-sm text-slate-500">Attendance (12 mo)</div>
            </div>
            <div>
              <div className="text-2xl font-semibold">{c.fees.totalPaid}</div>
              <div className="text-sm text-slate-500">Fees paid</div>
            </div>
            <div>
              <div className={"text-2xl font-semibold" + (Number(c.fees.totalDue) > 0 ? " text-red-600" : "")}>
                {c.fees.totalDue}
              </div>
              <div className="text-sm text-slate-500">Fees due</div>
            </div>
          </div>
          {c.fees.fees.length > 0 && (
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr><th className="th">Fee</th><th className="th">Amount</th><th className="th">Due</th><th className="th">Due date</th><th className="th">Status</th><th className="th"></th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {c.fees.fees.map((f) => (
                  <tr key={f.id}>
                    <td className="td font-medium">{f.title}</td>
                    <td className="td">{f.amount}</td>
                    <td className="td">{f.dueAmount}</td>
                    <td className="td">{f.dueDate ?? "—"}</td>
                    <td className="td">{f.status}</td>
                    <td className="td">
                      {Number(f.dueAmount) > 0 && f.status !== "WAIVED" && (
                        <button
                          className="text-xs font-medium text-indigo-600 hover:underline"
                          disabled={checkout.isPending}
                          onClick={() => checkout.mutate(f.id)}
                        >
                          Pay now
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ))}

      {d.children.length === 0 && (
        <p className="card text-sm text-slate-400">
          No students are linked to your guardian profile yet. Ask the institute office to link your children.
        </p>
      )}
    </div>
  );
}
