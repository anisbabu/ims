"use client";

export type Field = { label: string; value?: string | number | null };

export function DetailModal({
  title,
  subtitle,
  fields,
  children,
  onClose,
}: {
  title: string;
  subtitle?: string;
  fields: Field[];
  children?: React.ReactNode;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/40 p-4" onClick={onClose}>
      <div
        className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-lg bg-white shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h2 className="text-lg font-semibold text-slate-800">{title}</h2>
            {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
          </div>
          <button className="btn-ghost" onClick={onClose}>Close</button>
        </div>

        <div className="grid grid-cols-1 gap-x-8 gap-y-4 px-6 py-5 sm:grid-cols-2">
          {fields.map((f) => (
            <div key={f.label} className="border-b border-slate-100 pb-2">
              <div className="text-xs font-medium uppercase tracking-wide text-slate-400">{f.label}</div>
              <div className="mt-0.5 text-sm text-slate-800">
                {f.value === null || f.value === undefined || f.value === "" ? (
                  <span className="text-slate-300">—</span>
                ) : (
                  String(f.value)
                )}
              </div>
            </div>
          ))}
        </div>

        {children && <div className="border-t border-slate-200 px-6 py-5">{children}</div>}
      </div>
    </div>
  );
}
