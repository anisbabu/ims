const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api";

const ACCESS_KEY = "ims_access";
const REFRESH_KEY = "ims_refresh";

export function setTokens(access: string, refresh: string) {
  localStorage.setItem(ACCESS_KEY, access);
  localStorage.setItem(REFRESH_KEY, refresh);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
}

export function getAccess(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function tryRefresh(): Promise<boolean> {
  const refresh = localStorage.getItem(REFRESH_KEY);
  if (!refresh) return false;
  const res = await fetch(`${API_BASE}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: refresh }),
  });
  if (!res.ok) return false;
  const data = await res.json();
  setTokens(data.accessToken, data.refreshToken);
  return true;
}

export async function api<T = unknown>(
  path: string,
  options: RequestInit = {},
  retry = true
): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");
  const token = getAccess();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401 && retry && (await tryRefresh())) {
    return api<T>(path, options, false);
  }
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const body = await res.json();
      msg = body.message ?? msg;
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, msg);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const API_BASE_URL = API_BASE;

/** Origin without the trailing /api, for building absolute file URLs. */
const ORIGIN = API_BASE.replace(/\/api$/, "");

/** Resolve a stored file path (e.g. "/api/files/{id}") to an absolute URL. */
export function fileUrl(path?: string | null): string | undefined {
  if (!path) return undefined;
  return path.startsWith("http") ? path : ORIGIN + path;
}

/** Upload an image, returns its relative url ("/api/files/{id}"). */
export async function uploadImage(file: File): Promise<string> {
  const form = new FormData();
  form.append("file", file);
  const headers = new Headers();
  const token = getAccess();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let res = await fetch(`${API_BASE}/files`, { method: "POST", headers, body: form });
  if (res.status === 401 && (await tryRefresh())) {
    const h = new Headers();
    const t = getAccess();
    if (t) h.set("Authorization", `Bearer ${t}`);
    res = await fetch(`${API_BASE}/files`, { method: "POST", headers: h, body: form });
  }
  if (!res.ok) {
    let msg = res.statusText;
    try { msg = (await res.json()).message ?? msg; } catch { /* ignore */ }
    throw new ApiError(res.status, msg);
  }
  const data = await res.json();
  return data.url as string;
}

/** Fetches a binary (e.g. PDF) with auth + one refresh retry, and triggers a browser download. */
export async function downloadFile(path: string, fallbackName: string): Promise<void> {
  const doFetch = async () => {
    const headers = new Headers();
    const token = getAccess();
    if (token) headers.set("Authorization", `Bearer ${token}`);
    return fetch(`${API_BASE}${path}`, { headers });
  };

  let res = await doFetch();
  if (res.status === 401 && (await tryRefresh())) {
    res = await doFetch();
  }
  if (!res.ok) throw new ApiError(res.status, `Download failed (${res.status})`);

  const disposition = res.headers.get("Content-Disposition") ?? "";
  const match = /filename="?([^"]+)"?/.exec(disposition);
  const name = match ? match[1] : fallbackName;

  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
