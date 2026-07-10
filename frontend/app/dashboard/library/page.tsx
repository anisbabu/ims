"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Book, BookIssue, Page, Student } from "@/lib/types";

export default function LibraryPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Library</h1>
      <Books />
      <Issues />
    </div>
  );
}

function Books() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [form, setForm] = useState({ title: "", author: "", isbn: "", category: "", totalCopies: "1" });
  const [err, setErr] = useState<string | null>(null);
  const list = useQuery({ queryKey: ["books", q], queryFn: () => api<Page<Book>>(`/library/books?size=50${q ? `&q=${encodeURIComponent(q)}` : ""}`) });

  const create = useMutation({
    mutationFn: () => api("/library/books", { method: "POST", body: JSON.stringify({ ...form, totalCopies: Number(form.totalCopies) }) }),
    onSuccess: () => { setForm({ title: "", author: "", isbn: "", category: "", totalCopies: "1" }); setErr(null); qc.invalidateQueries({ queryKey: ["books"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({
    mutationFn: (id: string) => api(`/library/books/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["books"] }),
  });

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Books</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-6" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input md:col-span-2" placeholder="Title" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        <input className="input" placeholder="Author" value={form.author} onChange={(e) => setForm({ ...form, author: e.target.value })} />
        <input className="input" placeholder="ISBN" value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} />
        <input className="input" type="number" min={0} placeholder="Copies" value={form.totalCopies} onChange={(e) => setForm({ ...form, totalCopies: e.target.value })} />
        <button className="btn" disabled={create.isPending}>Add book</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <input className="input max-w-xs" placeholder="Search title…" value={q} onChange={(e) => setQ(e.target.value)} />
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Title</th><th className="th">Author</th><th className="th">ISBN</th><th className="th">Available</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((b) => (
              <tr key={b.id}>
                <td className="td font-medium">{b.title}</td>
                <td className="td">{b.author ?? "—"}</td>
                <td className="td">{b.isbn ?? "—"}</td>
                <td className="td">{b.availableCopies}/{b.totalCopies}</td>
                <td className="td"><button className="text-xs text-red-600" onClick={() => { if (confirm("Delete book?")) del.mutate(b.id); }}>Delete</button></td>
              </tr>
            ))}
            {list.data?.content.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No books.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Issues() {
  const qc = useQueryClient();
  const books = useQuery({ queryKey: ["books-all"], queryFn: () => api<Page<Book>>("/library/books?size=200") });
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=200") });
  const list = useQuery({ queryKey: ["issues"], queryFn: () => api<Page<BookIssue>>("/library/issues?size=50") });
  const [form, setForm] = useState({ bookId: "", studentId: "", dueDate: "" });
  const [err, setErr] = useState<string | null>(null);

  const issue = useMutation({
    mutationFn: () => api("/library/issues", { method: "POST", body: JSON.stringify({ ...form, dueDate: form.dueDate || null }) }),
    onSuccess: () => { setErr(null); qc.invalidateQueries({ queryKey: ["issues"] }); qc.invalidateQueries({ queryKey: ["books"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const ret = useMutation({
    mutationFn: (id: string) => api(`/library/issues/${id}/return`, { method: "PATCH", body: JSON.stringify({}) }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["issues"] }); qc.invalidateQueries({ queryKey: ["books"] }); },
  });

  const bookTitle = (id: string) => books.data?.content.find((b) => b.id === id)?.title ?? id;
  const stuName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Issue / return</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-4" onSubmit={(e) => { e.preventDefault(); issue.mutate(); }}>
        <select className="input" required value={form.bookId} onChange={(e) => setForm({ ...form, bookId: e.target.value })}>
          <option value="">Book…</option>
          {books.data?.content.filter((b) => b.availableCopies > 0).map((b) => <option key={b.id} value={b.id}>{b.title} ({b.availableCopies})</option>)}
        </select>
        <select className="input" required value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })}>
          <option value="">Student…</option>
          {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
        </select>
        <input className="input" type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
        <button className="btn" disabled={issue.isPending}>Issue book</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Book</th><th className="th">Student</th><th className="th">Issued</th><th className="th">Due</th><th className="th">Status</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((i) => (
              <tr key={i.id}>
                <td className="td font-medium">{bookTitle(i.bookId)}</td>
                <td className="td">{stuName(i.studentId)}</td>
                <td className="td">{i.issueDate}</td>
                <td className="td">{i.dueDate ?? "—"}</td>
                <td className={"td font-medium " + (i.status === "RETURNED" ? "text-emerald-600" : "text-amber-600")}>{i.status}{i.fine > 0 && ` · fine ${i.fine}`}</td>
                <td className="td">{i.status === "ISSUED" && <button className="text-xs text-indigo-600" onClick={() => ret.mutate(i.id)}>Return</button>}</td>
              </tr>
            ))}
            {list.data?.content.length === 0 && <tr><td className="td text-slate-400" colSpan={6}>No issues.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}
