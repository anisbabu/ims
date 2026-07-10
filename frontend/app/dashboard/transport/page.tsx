"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Page, Student, TransportAssignment, TransportRoute, Vehicle } from "@/lib/types";

export default function TransportPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Transport</h1>
      <Vehicles />
      <Routes />
      <Assignments />
    </div>
  );
}

function Vehicles() {
  const qc = useQueryClient();
  const list = useQuery({ queryKey: ["vehicles"], queryFn: () => api<Vehicle[]>("/transport/vehicles") });
  const [form, setForm] = useState({ regNo: "", model: "", capacity: "40", driverName: "", driverPhone: "" });
  const [err, setErr] = useState<string | null>(null);
  const create = useMutation({
    mutationFn: () => api("/transport/vehicles", { method: "POST", body: JSON.stringify({ ...form, capacity: Number(form.capacity) }) }),
    onSuccess: () => { setForm({ regNo: "", model: "", capacity: "40", driverName: "", driverPhone: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["vehicles"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({ mutationFn: (id: string) => api(`/transport/vehicles/${id}`, { method: "DELETE" }), onSuccess: () => qc.invalidateQueries({ queryKey: ["vehicles"] }) });

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Vehicles</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-6" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input" placeholder="Reg no" required value={form.regNo} onChange={(e) => setForm({ ...form, regNo: e.target.value })} />
        <input className="input" placeholder="Model" value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} />
        <input className="input" type="number" placeholder="Capacity" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} />
        <input className="input" placeholder="Driver" value={form.driverName} onChange={(e) => setForm({ ...form, driverName: e.target.value })} />
        <input className="input" placeholder="Driver phone" value={form.driverPhone} onChange={(e) => setForm({ ...form, driverPhone: e.target.value })} />
        <button className="btn" disabled={create.isPending}>Add vehicle</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Reg no</th><th className="th">Model</th><th className="th">Capacity</th><th className="th">Driver</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((v) => (
              <tr key={v.id}>
                <td className="td font-medium">{v.regNo}</td><td className="td">{v.model ?? "—"}</td><td className="td">{v.capacity}</td><td className="td">{v.driverName ?? "—"}{v.driverPhone ? ` · ${v.driverPhone}` : ""}</td>
                <td className="td"><button className="text-xs text-red-600" onClick={() => { if (confirm("Delete vehicle?")) del.mutate(v.id); }}>Delete</button></td>
              </tr>
            ))}
            {list.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>None yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Routes() {
  const qc = useQueryClient();
  const vehicles = useQuery({ queryKey: ["vehicles"], queryFn: () => api<Vehicle[]>("/transport/vehicles") });
  const list = useQuery({ queryKey: ["routes"], queryFn: () => api<TransportRoute[]>("/transport/routes") });
  const [form, setForm] = useState({ name: "", stops: "", fare: "", vehicleId: "" });
  const [err, setErr] = useState<string | null>(null);
  const create = useMutation({
    mutationFn: () => api("/transport/routes", { method: "POST", body: JSON.stringify({ name: form.name, stops: form.stops, fare: form.fare ? Number(form.fare) : null, vehicleId: form.vehicleId || null }) }),
    onSuccess: () => { setForm({ name: "", stops: "", fare: "", vehicleId: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["routes"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({ mutationFn: (id: string) => api(`/transport/routes/${id}`, { method: "DELETE" }), onSuccess: () => qc.invalidateQueries({ queryKey: ["routes"] }) });
  const vehReg = (id?: string) => vehicles.data?.find((v) => v.id === id)?.regNo ?? "—";

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Routes</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-5" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input" placeholder="Name" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input className="input md:col-span-2" placeholder="Stops (comma separated)" value={form.stops} onChange={(e) => setForm({ ...form, stops: e.target.value })} />
        <input className="input" type="number" placeholder="Fare" value={form.fare} onChange={(e) => setForm({ ...form, fare: e.target.value })} />
        <select className="input" value={form.vehicleId} onChange={(e) => setForm({ ...form, vehicleId: e.target.value })}>
          <option value="">Vehicle…</option>
          {vehicles.data?.map((v) => <option key={v.id} value={v.id}>{v.regNo}</option>)}
        </select>
        <button className="btn md:col-span-5" disabled={create.isPending}>Add route</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Name</th><th className="th">Stops</th><th className="th">Fare</th><th className="th">Vehicle</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((r) => (
              <tr key={r.id}>
                <td className="td font-medium">{r.name}</td><td className="td">{r.stops ?? "—"}</td><td className="td">{r.fare ?? "—"}</td><td className="td">{vehReg(r.vehicleId)}</td>
                <td className="td"><button className="text-xs text-red-600" onClick={() => { if (confirm("Delete route?")) del.mutate(r.id); }}>Delete</button></td>
              </tr>
            ))}
            {list.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>None yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Assignments() {
  const qc = useQueryClient();
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=200") });
  const routes = useQuery({ queryKey: ["routes"], queryFn: () => api<TransportRoute[]>("/transport/routes") });
  const list = useQuery({ queryKey: ["assignments"], queryFn: () => api<Page<TransportAssignment>>("/transport/assignments?size=50") });
  const [form, setForm] = useState({ studentId: "", routeId: "", stopName: "" });
  const [err, setErr] = useState<string | null>(null);

  const assign = useMutation({
    mutationFn: () => api("/transport/assignments", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => { setForm({ studentId: "", routeId: "", stopName: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["assignments"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const end = useMutation({
    mutationFn: (id: string) => api(`/transport/assignments/${id}/end`, { method: "PATCH", body: JSON.stringify({}) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["assignments"] }),
  });

  const stuName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;
  const routeName = (id: string) => routes.data?.find((r) => r.id === id)?.name ?? id;

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Student assignments</h2>
      <form className="grid grid-cols-1 gap-3 md:grid-cols-4" onSubmit={(e) => { e.preventDefault(); assign.mutate(); }}>
        <select className="input" required value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })}>
          <option value="">Student…</option>
          {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
        </select>
        <select className="input" required value={form.routeId} onChange={(e) => setForm({ ...form, routeId: e.target.value })}>
          <option value="">Route…</option>
          {routes.data?.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
        <input className="input" placeholder="Stop" value={form.stopName} onChange={(e) => setForm({ ...form, stopName: e.target.value })} />
        <button className="btn" disabled={assign.isPending}>Assign</button>
        {err && <p className="col-span-full text-sm text-red-600">{err}</p>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Student</th><th className="th">Route</th><th className="th">Stop</th><th className="th">Status</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.content.map((a) => (
              <tr key={a.id}>
                <td className="td font-medium">{stuName(a.studentId)}</td><td className="td">{routeName(a.routeId)}</td><td className="td">{a.stopName ?? "—"}</td>
                <td className={"td font-medium " + (a.status === "ACTIVE" ? "text-emerald-600" : "text-slate-500")}>{a.status}</td>
                <td className="td">{a.status === "ACTIVE" && <button className="text-xs text-indigo-600" onClick={() => end.mutate(a.id)}>End</button>}</td>
              </tr>
            ))}
            {list.data?.content.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No assignments.</td></tr>}
          </tbody>
        </table>
      </div>
    </section>
  );
}
