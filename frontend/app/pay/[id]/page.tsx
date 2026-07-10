"use client";

import { useParams, useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { OnlinePayment } from "@/lib/types";

/** Mock hosted-checkout page. With a real gateway the payer is redirected to the
 *  provider's own page instead, and confirmation arrives via webhook. */
export default function PayPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const qc = useQueryClient();

  const payment = useQuery({
    queryKey: ["payment", id],
    queryFn: () => api<OnlinePayment>(`/payments/${id}`),
  });

  const act = useMutation({
    mutationFn: (action: "confirm" | "cancel") =>
      api<OnlinePayment>(`/payments/${id}/${action}`, { method: "POST" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["payment", id] }),
  });

  const p = payment.data;

  return (
    <main className="grid min-h-screen place-items-center bg-slate-100 p-4">
      <div className="card w-full max-w-md space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-lg font-semibold">IMS Pay</h1>
          <span className="rounded bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700">
            {p?.provider ?? "…"} · demo gateway
          </span>
        </div>

        {payment.isLoading && <p className="text-sm text-slate-500">Loading payment…</p>}
        {payment.isError && (
          <p className="text-sm text-red-600">
            {payment.error instanceof Error ? payment.error.message : "Payment not found"}
          </p>
        )}

        {p && (
          <>
            <div className="rounded-md bg-slate-50 p-4">
              <div className="text-sm text-slate-500">{p.feeTitle}</div>
              <div className="text-3xl font-semibold">{p.amount}</div>
              <div className="mt-1 text-xs text-slate-400">Payment {p.id.slice(0, 8)}…</div>
            </div>

            {p.status === "PENDING" && (
              <div className="flex gap-2">
                <button className="btn flex-1" disabled={act.isPending} onClick={() => act.mutate("confirm")}>
                  Pay {p.amount}
                </button>
                <button className="btn-ghost" disabled={act.isPending} onClick={() => act.mutate("cancel")}>
                  Cancel
                </button>
              </div>
            )}

            {p.status === "SUCCESS" && (
              <div className="space-y-3 text-center">
                <p className="text-lg font-semibold text-green-700">✓ Payment successful</p>
                <p className="text-sm text-slate-500">Reference {p.reference}</p>
                <button className="btn w-full" onClick={() => router.replace("/dashboard")}>
                  Back to dashboard
                </button>
              </div>
            )}

            {(p.status === "CANCELLED" || p.status === "FAILED") && (
              <div className="space-y-3 text-center">
                <p className="text-lg font-semibold text-slate-600">Payment {p.status.toLowerCase()}</p>
                <button className="btn w-full" onClick={() => router.replace("/dashboard")}>
                  Back to dashboard
                </button>
              </div>
            )}

            {act.isError && (
              <p className="text-sm text-red-600">
                {act.error instanceof Error ? act.error.message : "Failed"}
              </p>
            )}
          </>
        )}
      </div>
    </main>
  );
}
