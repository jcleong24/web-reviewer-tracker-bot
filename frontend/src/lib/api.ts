import type { Assessment } from "@/types/assessment";

/**
 * POST a URL to the backend analyze endpoint and return the assessment.
 * The request is proxied to the Spring backend (see vite.config.ts); the
 * browser never fetches the target domain directly.
 */
export async function analyzeUrl(url: string): Promise<Assessment> {
  const response = await fetch("/api/v1/analyze", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as {
      error?: string;
    } | null;
    throw new Error(body?.error ?? `Request failed (${response.status}).`);
  }

  return (await response.json()) as Assessment;
}
