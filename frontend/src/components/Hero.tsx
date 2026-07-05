import type { ReactNode } from "react";
import { Sparkles } from "lucide-react";

// Free Unsplash photo (abstract dark-blue tech), hotlinked via the Unsplash CDN.
// Used as low-opacity atmosphere behind a gradient overlay.
const HERO_IMAGE =
  "https://images.unsplash.com/photo-1644088379091-d574269d422f?auto=format&fit=crop&w=2400&q=80";

/**
 * Landing hero: headline, subcopy, and the analyzer call-to-action (passed as
 * children so form state stays in the parent).
 */
export function Hero({ children }: { children: ReactNode }) {
  return (
    <section className="relative isolate overflow-hidden border-b border-border">
      <div className="absolute inset-0 -z-10">
        <img
          src={HERO_IMAGE}
          alt=""
          aria-hidden="true"
          className="h-full w-full object-cover opacity-25"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-background/60 via-background/85 to-background" />
        <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-primary/40 to-transparent" />
      </div>

      <div className="mx-auto max-w-3xl px-6 py-24 text-center sm:py-32">
        <span className="inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
          <Sparkles className="h-3.5 w-3.5" />
          AI-powered market analysis
        </span>

        <h1 className="mt-6 text-balance text-4xl font-bold tracking-tight sm:text-5xl">
          Gauge any product&apos;s{" "}
          <span className="bg-gradient-to-r from-sky-400 to-primary bg-clip-text text-transparent">
            market potential
          </span>{" "}
          in seconds
        </h1>

        <p className="mx-auto mt-4 max-w-xl text-balance text-muted-foreground sm:text-lg">
          Paste a product URL and get a candid, evidence-based scorecard — demand,
          differentiation, willingness to pay, and the risks a scout should worry about.
        </p>

        <div className="mx-auto mt-8 max-w-xl">{children}</div>
      </div>
    </section>
  );
}
