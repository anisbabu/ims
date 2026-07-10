"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Hostel, HostelAllocation, Page, Room, Student } from "@/lib/types";
import { HOSTEL_TYPES } from "@/lib/types";
import { DetailModal } from "@/components/DetailModal";

export default function HostelPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Hostel</h1>
      <Hostels />
      <RoomsAndAllocations />
    </div>
  );
}

function Hostels() {
  const qc = useQueryClient();
  const list = useQuery({ queryKey: ["hostels"], queryFn: () => api<Hostel[]>("/hostel/hostels") });
  const [form, setForm] = useState({ name: "", type: "BOYS", address: "" });
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Hostel | null>(null);
  const create = useMutation({
    mutationFn: () => api("/hostel/hostels", { method: "POST", body: JSON.stringify(form) }),
    onSuccess: () => { setForm({ name: "", type: "BOYS", address: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["hostels"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const del = useMutation({ mutationFn: (id: string) => api(`/hostel/hostels/${id}`, { method: "DELETE" }), onSuccess: () => qc.invalidateQueries({ queryKey: ["hostels"] }) });

  return (
    <section className="card space-y-3">
      <h2 className="font-semibold">Hostels</h2>
      <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <input className="input max-w-[14rem]" placeholder="Name" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <select className="input max-w-[8rem]" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>{HOSTEL_TYPES.map((t) => <option key={t}>{t}</option>)}</select>
        <input className="input max-w-[16rem]" placeholder="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
        <button className="btn" disabled={create.isPending}>Add hostel</button>
        {err && <span className="text-sm text-red-600">{err}</span>}
      </form>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">Name</th><th className="th">Type</th><th className="th">Address</th><th className="th"></th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {list.data?.map((h) => (
              <tr key={h.id}>
                <td className="td font-medium">{h.name}</td><td className="td">{h.type}</td><td className="td">{h.address ?? "—"}</td>
                <td className="td space-x-2 whitespace-nowrap">
                  <button className="text-xs text-slate-600 hover:underline" onClick={() => setViewing(h)}>View</button>
                  <button className="text-xs text-red-600" onClick={() => { if (confirm("Delete hostel?")) del.mutate(h.id); }}>Delete</button>
                </td>
              </tr>
            ))}
            {list.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={4}>None yet.</td></tr>}
          </tbody>
        </table>
      </div>

      {viewing && (
        <DetailModal
          title={viewing.name}
          subtitle={`Hostel · ${viewing.type}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Name", value: viewing.name },
            { label: "Type", value: viewing.type },
            { label: "Address", value: viewing.address },
            { label: "Warden ID", value: viewing.wardenId },
          ]}
        />
      )}
    </section>
  );
}

function RoomsAndAllocations() {
  const qc = useQueryClient();
  const hostels = useQuery({ queryKey: ["hostels"], queryFn: () => api<Hostel[]>("/hostel/hostels") });
  const students = useQuery({ queryKey: ["students-all"], queryFn: () => api<Page<Student>>("/students?size=200") });
  const [hostelId, setHostelId] = useState("");
  const rooms = useQuery({ queryKey: ["rooms", hostelId], queryFn: () => api<Room[]>(`/hostel/rooms?hostelId=${hostelId}`), enabled: !!hostelId });
  const allocs = useQuery({ queryKey: ["allocations", hostelId], queryFn: () => api<Page<HostelAllocation>>(`/hostel/allocations?hostelId=${hostelId}&size=100`), enabled: !!hostelId });

  const [roomForm, setRoomForm] = useState({ roomNo: "", capacity: "2" });
  const [alloc, setAlloc] = useState({ studentId: "", roomId: "" });
  const [err, setErr] = useState<string | null>(null);
  const [viewing, setViewing] = useState<HostelAllocation | null>(null);

  const addRoom = useMutation({
    mutationFn: () => api("/hostel/rooms", { method: "POST", body: JSON.stringify({ hostelId, roomNo: roomForm.roomNo, capacity: Number(roomForm.capacity) }) }),
    onSuccess: () => { setRoomForm({ roomNo: "", capacity: "2" }); setErr(null); qc.invalidateQueries({ queryKey: ["rooms"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const allocate = useMutation({
    mutationFn: () => api("/hostel/allocations", { method: "POST", body: JSON.stringify(alloc) }),
    onSuccess: () => { setAlloc({ studentId: "", roomId: "" }); setErr(null); qc.invalidateQueries({ queryKey: ["allocations"] }); qc.invalidateQueries({ queryKey: ["rooms"] }); },
    onError: (e) => setErr(e instanceof Error ? e.message : "Failed"),
  });
  const vacate = useMutation({
    mutationFn: (id: string) => api(`/hostel/allocations/${id}/vacate`, { method: "PATCH", body: JSON.stringify({}) }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["allocations"] }); qc.invalidateQueries({ queryKey: ["rooms"] }); },
  });

  const stuName = (id: string) => students.data?.content.find((s) => s.id === id)?.fullName ?? id;
  const roomNo = (id: string) => rooms.data?.find((r) => r.id === id)?.roomNo ?? id;

  return (
    <section className="card space-y-4">
      <h2 className="font-semibold">Rooms &amp; allocations</h2>
      <select className="input max-w-[16rem]" value={hostelId} onChange={(e) => setHostelId(e.target.value)}>
        <option value="">Select hostel…</option>
        {hostels.data?.map((h) => <option key={h.id} value={h.id}>{h.name}</option>)}
      </select>

      {hostelId && (
        <>
          <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); addRoom.mutate(); }}>
            <input className="input max-w-[8rem]" placeholder="Room no" required value={roomForm.roomNo} onChange={(e) => setRoomForm({ ...roomForm, roomNo: e.target.value })} />
            <input className="input max-w-[6rem]" type="number" min={1} value={roomForm.capacity} onChange={(e) => setRoomForm({ ...roomForm, capacity: e.target.value })} />
            <button className="btn" disabled={addRoom.isPending}>Add room</button>
          </form>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead><tr><th className="th">Room</th><th className="th">Occupancy</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {rooms.data?.map((r) => <tr key={r.id}><td className="td font-medium">{r.roomNo}</td><td className="td">{r.occupied}/{r.capacity}</td></tr>)}
                {rooms.data?.length === 0 && <tr><td className="td text-slate-400" colSpan={2}>No rooms.</td></tr>}
              </tbody>
            </table>
          </div>

          <form className="flex flex-wrap gap-2" onSubmit={(e) => { e.preventDefault(); allocate.mutate(); }}>
            <select className="input max-w-[14rem]" required value={alloc.studentId} onChange={(e) => setAlloc({ ...alloc, studentId: e.target.value })}>
              <option value="">Student…</option>
              {students.data?.content.map((s) => <option key={s.id} value={s.id}>{s.fullName}</option>)}
            </select>
            <select className="input max-w-[10rem]" required value={alloc.roomId} onChange={(e) => setAlloc({ ...alloc, roomId: e.target.value })}>
              <option value="">Room…</option>
              {rooms.data?.filter((r) => r.occupied < r.capacity).map((r) => <option key={r.id} value={r.id}>{r.roomNo}</option>)}
            </select>
            <button className="btn" disabled={allocate.isPending}>Allocate</button>
            {err && <span className="text-sm text-red-600">{err}</span>}
          </form>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead><tr><th className="th">Student</th><th className="th">Room</th><th className="th">Since</th><th className="th">Status</th><th className="th"></th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {allocs.data?.content.map((a) => (
                  <tr key={a.id}>
                    <td className="td font-medium">{stuName(a.studentId)}</td><td className="td">{roomNo(a.roomId)}</td><td className="td">{a.allocatedDate}</td>
                    <td className={"td font-medium " + (a.status === "ALLOCATED" ? "text-emerald-600" : "text-slate-500")}>{a.status}</td>
                    <td className="td space-x-2 whitespace-nowrap">
                      <button className="text-xs text-slate-600 hover:underline" onClick={() => setViewing(a)}>View</button>
                      {a.status === "ALLOCATED" && <button className="text-xs text-indigo-600" onClick={() => vacate.mutate(a.id)}>Vacate</button>}
                    </td>
                  </tr>
                ))}
                {allocs.data?.content.length === 0 && <tr><td className="td text-slate-400" colSpan={5}>No allocations.</td></tr>}
              </tbody>
            </table>
          </div>
        </>
      )}

      {viewing && (
        <DetailModal
          title={stuName(viewing.studentId)}
          subtitle={`Allocation · ${viewing.status}`}
          onClose={() => setViewing(null)}
          fields={[
            { label: "Student", value: stuName(viewing.studentId) },
            { label: "Room", value: roomNo(viewing.roomId) },
            { label: "Allocated date", value: viewing.allocatedDate },
            { label: "Vacated date", value: viewing.vacatedDate },
            { label: "Status", value: viewing.status },
          ]}
        />
      )}
    </section>
  );
}
