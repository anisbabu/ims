"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

type Item = { href: string; label: string };
type Group = { title: string; items: Item[] };

const GROUPS: Group[] = [
  {
    title: "Dashboard",
    items: [{ href: "/dashboard", label: "Overview" }],
  },
  {
    title: "Academic",
    items: [
      { href: "/dashboard/students", label: "Students" },
      { href: "/dashboard/teachers", label: "Teachers" },
      { href: "/dashboard/guardians", label: "Guardians" },
      { href: "/dashboard/academic", label: "Academic setup" },
      { href: "/dashboard/admissions", label: "Admissions" },
      { href: "/dashboard/attendance", label: "Attendance" },
      { href: "/dashboard/routine", label: "Routine" },
    ],
  },
  {
    title: "Accounts",
    items: [
      { href: "/dashboard/fees", label: "Fees" },
      { href: "/dashboard/accounting/accounts", label: "Chart of accounts" },
      { href: "/dashboard/accounting/journals", label: "Journals" },
      { href: "/dashboard/accounting/reports", label: "Reports" },
    ],
  },
  {
    title: "Exams",
    items: [
      { href: "/dashboard/exams", label: "Exams & Results" },
      { href: "/dashboard/reports", label: "Reports" },
      { href: "/dashboard/certificates", label: "Certificates" },
    ],
  },
  {
    title: "Facilities",
    items: [
      { href: "/dashboard/library", label: "Library" },
      { href: "/dashboard/hostel", label: "Hostel" },
      { href: "/dashboard/transport", label: "Transport" },
    ],
  },
  {
    title: "Administration",
    items: [
      { href: "/dashboard/users", label: "Users & roles" },
      { href: "/dashboard/settings", label: "My account" },
    ],
  },
  {
    title: "Platform",
    items: [{ href: "/dashboard/institutes", label: "Institutes" }],
  },
];

export function Nav() {
  const path = usePathname();
  return (
    <nav className="space-y-4">
      {GROUPS.map((group) => (
        <div key={group.title}>
          <div className="px-3 pb-1 text-xs font-bold uppercase tracking-wider text-indigo-600">
            {group.title}
          </div>
          <div className="space-y-0.5">
            {group.items.map((l) => {
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
      ))}
    </nav>
  );
}
