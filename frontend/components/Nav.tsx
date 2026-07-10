"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useMe } from "@/lib/hooks";
import type { Role } from "@/lib/types";

type Item = { href: string; label: string; roles?: Role[] };
type Group = { title: string; items: Item[]; roles?: Role[] };

const ADMINS: Role[] = ["SUPER_ADMIN", "INSTITUTE_ADMIN"];
const STAFF: Role[] = ["SUPER_ADMIN", "INSTITUTE_ADMIN", "TEACHER"];

// No `roles` = visible to every logged-in role.
const GROUPS: Group[] = [
  {
    title: "Dashboard",
    items: [{ href: "/dashboard", label: "Overview" }],
  },
  {
    title: "Communication",
    items: [{ href: "/dashboard/notices", label: "Notices" }],
  },
  {
    title: "Academic",
    roles: STAFF,
    items: [
      { href: "/dashboard/students", label: "Students" },
      { href: "/dashboard/teachers", label: "Teachers" },
      { href: "/dashboard/guardians", label: "Guardians" },
      { href: "/dashboard/academic", label: "Academic setup", roles: ADMINS },
      { href: "/dashboard/admissions", label: "Admissions", roles: ADMINS },
      { href: "/dashboard/attendance", label: "Attendance" },
      { href: "/dashboard/routine", label: "Routine" },
    ],
  },
  {
    title: "Accounts",
    roles: ADMINS,
    items: [
      { href: "/dashboard/fees", label: "Fees" },
      { href: "/dashboard/fee-structures", label: "Fee structures" },
      { href: "/dashboard/accounting/accounts", label: "Chart of accounts" },
      { href: "/dashboard/accounting/journals", label: "Journals" },
      { href: "/dashboard/accounting/reports", label: "Reports" },
    ],
  },
  {
    title: "Exams",
    roles: STAFF,
    items: [
      { href: "/dashboard/exams", label: "Exams & Results" },
      { href: "/dashboard/reports", label: "Reports" },
      { href: "/dashboard/certificates", label: "Certificates" },
    ],
  },
  {
    title: "Facilities",
    roles: STAFF,
    items: [
      { href: "/dashboard/library", label: "Library" },
      { href: "/dashboard/hostel", label: "Hostel" },
      { href: "/dashboard/transport", label: "Transport" },
    ],
  },
  {
    title: "Administration",
    items: [
      { href: "/dashboard/users", label: "Users & roles", roles: ADMINS },
      { href: "/dashboard/settings", label: "My account" },
    ],
  },
  {
    title: "Platform",
    roles: ["SUPER_ADMIN"],
    items: [{ href: "/dashboard/institutes", label: "Institutes" }],
  },
];

export function Nav() {
  const path = usePathname();
  const { data: me } = useMe();

  const allowed = (roles?: Role[]) => !roles || (me ? roles.includes(me.role) : false);

  return (
    <nav className="space-y-4">
      {GROUPS.filter((g) => allowed(g.roles)).map((group) => {
        const items = group.items.filter((i) => allowed(i.roles ?? group.roles));
        if (items.length === 0) return null;
        return (
          <div key={group.title}>
            <div className="px-3 pb-1 text-xs font-bold uppercase tracking-wider text-indigo-600">
              {group.title}
            </div>
            <div className="space-y-0.5">
              {items.map((l) => {
                const active = path === l.href;
                return (
                  <Link
                    key={l.href}
                    href={l.href}
                    className={
                      "block rounded-md px-3 py-1.5 text-sm " +
                      (active
                        ? "bg-indigo-600 font-medium text-white"
                        : "font-light text-slate-500 hover:bg-slate-100 hover:text-slate-800")
                    }
                  >
                    {l.label}
                  </Link>
                );
              })}
            </div>
          </div>
        );
      })}
    </nav>
  );
}
