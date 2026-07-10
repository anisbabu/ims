"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Grade, Notice, Page, Section } from "@/lib/types";
import { NOTICE_AUDIENCES } from "@/lib/types";
import { DetailModal } from "@/components/DetailModal";

const EMPTY = {
  title: "",
  body: "",
  audience: "ALL",
  gradeId: "",
  sectionId: "",
  publishDate: "",
  expiresOn: "",
  pinned: false,
};

export default function NoticesPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const list = useQuery({
    queryKey: ["notices", q],
    queryFn: () =>
      api<Page<Notice>>(
        `/notices?size=50&sort=pinned,desc&sort=publishDate,desc${q ? `&q=${encodeURIComponent(q)}` : ""}`
      ),
  });
  const grades = useQuery({ queryKey: ["grades-all"], queryFn: () => api<Grade[]>("/grades") });
  const sections = useQuery({ queryKey: ["sections-all"], queryFn: () => api<Section[]>("/sections") });

  const [form, setForm] = useState({ ...EMPTY });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Notice | null>(null);

  const payload = () => ({
    title: form.title,
    body: form.body || null,
    audience: form.audience,
    gradeId: form.gradeId || null,
    sectionId: form.sectionId || null,
    publishDate: form.publishDate || null,
    expiresOn: form.expiresOn || null,
    pinned: form.pinned,
  });

  const save = useMutation({
    mutationFn: () =>
      editingId
        ? api(`/notices/${editingId}`, { method: "PUT", body: JSON.stringify(payload()) })
        : api("/notices", { method: "POST", body: JSON.stringify(payload()) }),
    onSuccess: () => {
      setForm({ ...EMPTY });
      setEditingId(null);
      setErr(null);
      qc.invalidateQueries({ queryKey: ["notices"] });
    },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  const remove = useMutation({
    mutationFn: (id: string) => api(`/notices/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["notices"] }),
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });

  function startEdit(n: Notice) {
    setEditingId(n.id);
    setForm({
      title: n.title,
      body: n.body ?? "",
      audience: n.audience,
      gradeId: n.gradeId ?? "",
      sectionId: n.sectionId ?? "",
      publishDate: n.publishDate ?? "",
      expiresOn: n.expiresOn ?? "",
      pinned: n.pinned,
    });
  }

  const gradeName = (id?: string) => grades.data?.find((g) => g.id === id)?.name;
  const sectionName = (id?: string) => sections.data?.find((s) => s.id === id)?.name;
  const target = (n: Notice) =>
    [gradeName(n.gradeId), sectionName(n.sectionId)].filter(Boolean).join(" / ") || "—";

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">Notices</h1>

      <form
        className="card grid grid-cols-1 gap-3 md:grid-cols-4"
        onSubmit={(e) => {
          e.preventDefault();
          save.mutate();
        }}
      >
        <div className="md:col-span-2">
          <label className="label">Title</label>
          <input className="input" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div>
          <label className="label">Audience</label>
          <select className="input" value={form.audience} onChange={(e) => setForm({ ...form, audience: e.target.value })}>
            {NOTICE_AUDIENCES.map((a) => (
              <option key={a}>{a}</option>
            ))}
          </select>
        </div>
        <div className="flex items-end gap-2 pb-2">
          <input
            id="pinned"
            type="checkbox"
            checked={form.pinned}
            onChange={(e) => setForm({ ...form, pinned: e.target.checked })}
          />
          <label htmlFor="pinned" className="text-sm text-slate-600">
            Pin to top
          </label>
        </div>
        <div>
          <label className="label">Grade (optional)</label>
          <select
            className="input"
            value={form.gradeId}
            onChange={(e) => setForm({ ...form, gradeId: e.target.value, sectionId: "" })}
          >
            <option value="">All grades</option>
            {grades.data?.map((g) => (
              <option key={g.id} value={g.id}>
                {g.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Section (optional)</label>
          <select
            className="input"
            value={form.sectionId}
            onChange={(e) => setForm({ ...form, sectionId: e.target.value })}
          >
            <option value="">All sections</option>
            {sections.data
              ?.filter((s) => !form.gradeId || s.gradeId === form.gradeId)
              .map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
          </select>
        </div>
        <div>
          <label className="label">Publish date</label>
          <input
            type="date"
            className="input"
            value={form.publishDate}
            onChange={(e) => setForm({ ...form, publishDate: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Expires on</label>
          <input
            type="date"
            className="input"
            value={form.expiresOn}
            onChange={(e) => setForm({ ...form, expiresOn: e.target.value })}
          />
        </div>
        <div className="md:col-span-4">
          <label className="label">Body</label>
          <textarea className="input" rows={3} value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })} />
        </div>
        <div className="flex items-end gap-2">
          <button className="btn" disabled={save.isPending}>
            {editingId ? "Update notice" : "Publish notice"}
          </button>
          {editingId && (
            <button
              type="button"
              className="btn-ghost"
              onClick={() => {
                setEditingId(null);
                setForm({ ...EMPTY });
              }}
            >
              Cancel
            </button>
          )}
        </div>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>

      <input className="input max-w-xs" placeholder="Search title…" value={q} onChange={(e) => setQ(e.target.value)} />

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="th">Title</th>
              <th className="th">Audience</th>
              <th className="th">Target</th>
              <th className="th">Published</th>
              <th className="th">Expires</th>
              <th className="th"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((n) => (
              <tr key={n.id}>
                <td className="td font-medium">
                  {n.pinned && <span title="Pinned">📌 </span>}
                  {n.title}
                </td>
                <td className="td">{n.audience}</td>
                <td className="td">{target(n)}</td>
                <td className="td">{n.publishDate}</td>
                <td className="td">{n.expiresOn ?? "—"}</td>
                <td className="td space-x-2 whitespace-nowrap">
                  <button className="text-xs text-slate-600 hover:underline" onClick={() => setViewing(n)}>
                    View
                  </button>
                  <button className="text-xs font-medium text-indigo-600 hover:underline" onClick={() => startEdit(n)}>
                    Edit
                  </button>
                  <button
                    className="text-xs font-medium text-red-600 hover:underline"
                    onClick={() => {
                      if (confirm("Delete this notice?")) remove.mutate(n.id);
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {list.data && list.data.content.length === 0 && (
              <tr>
                <td className="td text-slate-400" colSpan={6}>
                  No notices yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {viewing && (
        <DetailModal
          title={viewing.title}
          subtitle={`Notice · ${viewing.audience}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Audience", value: viewing.audience },
            { label: "Grade", value: gradeName(viewing.gradeId) },
            { label: "Section", value: sectionName(viewing.sectionId) },
            { label: "Publish date", value: viewing.publishDate },
            { label: "Expires on", value: viewing.expiresOn },
            { label: "Pinned", value: viewing.pinned ? "Yes" : "No" },
          ]}
        >
          <div>
            <h3 className="mb-1 text-sm font-semibold text-slate-700">Body</h3>
            <p className="whitespace-pre-wrap text-sm text-slate-700">{viewing.body || "—"}</p>
          </div>
        </DetailModal>
      )}
    </div>
  );
}
