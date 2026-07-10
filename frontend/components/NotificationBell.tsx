"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AppNotification, Page } from "@/lib/types";

export function NotificationBell() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);

  const count = useQuery({
    queryKey: ["notifications-unread-count"],
    queryFn: () => api<{ count: number }>("/notifications/unread-count"),
    refetchInterval: 30000,
  });

  const list = useQuery({
    queryKey: ["notifications"],
    queryFn: () => api<Page<AppNotification>>("/notifications?size=15&sort=createdAt,desc"),
    enabled: open,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["notifications"] });
    qc.invalidateQueries({ queryKey: ["notifications-unread-count"] });
  };

  const markRead = useMutation({
    mutationFn: (id: string) => api(`/notifications/${id}/read`, { method: "PATCH" }),
    onSuccess: invalidate,
  });

  const markAll = useMutation({
    mutationFn: () => api("/notifications/read-all", { method: "POST" }),
    onSuccess: invalidate,
  });

  const unread = count.data?.count ?? 0;

  return (
    <div className="relative">
      <button
        className="relative rounded-md p-1.5 text-slate-500 hover:bg-slate-100 hover:text-slate-800"
        title="Notifications"
        onClick={() => setOpen((o) => !o)}
      >
        <span aria-hidden>🔔</span>
        {unread > 0 && (
          <span className="absolute -right-1 -top-1 rounded-full bg-red-600 px-1.5 text-[10px] font-bold leading-4 text-white">
            {unread > 99 ? "99+" : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute left-0 z-20 mt-1 w-72 rounded-md border border-slate-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-slate-100 px-3 py-2">
            <span className="text-sm font-semibold text-slate-700">Notifications</span>
            {unread > 0 && (
              <button
                className="text-xs text-indigo-600 hover:underline"
                onClick={() => markAll.mutate()}
                disabled={markAll.isPending}
              >
                Mark all read
              </button>
            )}
          </div>
          <div className="max-h-80 overflow-y-auto">
            {list.data?.content.map((n) => (
              <button
                key={n.id}
                className={
                  "block w-full border-b border-slate-50 px-3 py-2 text-left hover:bg-slate-50 " +
                  (n.readAt ? "opacity-60" : "")
                }
                onClick={() => {
                  if (!n.readAt) markRead.mutate(n.id);
                }}
              >
                <div className="text-sm font-medium text-slate-800">
                  {!n.readAt && <span className="mr-1 inline-block h-2 w-2 rounded-full bg-indigo-600" />}
                  {n.title}
                </div>
                {n.body && <div className="line-clamp-2 text-xs text-slate-500">{n.body}</div>}
                <div className="mt-0.5 text-[10px] uppercase tracking-wide text-slate-400">
                  {n.type}
                  {n.createdAt ? ` · ${new Date(n.createdAt).toLocaleString()}` : ""}
                </div>
              </button>
            ))}
            {list.data && list.data.content.length === 0 && (
              <div className="px-3 py-4 text-center text-sm text-slate-400">No notifications.</div>
            )}
            {list.isLoading && <div className="px-3 py-4 text-center text-sm text-slate-400">Loading…</div>}
          </div>
        </div>
      )}
    </div>
  );
}
