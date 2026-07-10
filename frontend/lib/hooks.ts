"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "./api";
import type { Me } from "./types";

export function useMe() {
  return useQuery({
    queryKey: ["me"],
    queryFn: () => api<Me>("/auth/me"),
  });
}
