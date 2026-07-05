import { useEffect, useState } from "react";
import { Check, Loader2 } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

const STAGES = [
  {
    label: "Fetching the page",
    detail: "Loading content server-side, rendering JavaScript when needed.",
  },
  {
    label: "Extracting content",
    detail: "Cleaning the page down to what actually signals potential.",
  },
  {
    label: "Assessing with Claude",
    detail: "Scoring eight market-potential dimensions with cited reasoning.",
  },
];

/**
 * Friendly loading state shown while an analysis is in flight. The backend
 * returns a single response, so stage progress is an optimistic simulation:
 * fetch/extract advance on a short timer, then it holds on the (longest)
 * assessment stage until the real result replaces this card.
 */
export function AnalyzingCard({ url }: { url: string }) {
  const [stage, setStage] = useState(0);
  const [progress, setProgress] = useState(8);

  useEffect(() => {
    const toExtract = setTimeout(() => setStage(1), 4500);
    const toAssess = setTimeout(() => setStage(2), 5000);
    return () => {
      clearTimeout(toExtract);
      clearTimeout(toAssess);
    };
  }, []);

  useEffect(() => {
    // Ease toward ~92% so the bar keeps moving but never completes early.
    const id = setInterval(() => {
      setProgress((p) => (p >= 92 ? 92 : p + Math.max(0.5, (92 - p) * 0.06)));
    }, 300);
    return () => clearInterval(id);
  }, []);

  return (
    <Card className="mt-10 p-6">
      <div className="flex items-center gap-3">
        <Loader2 className="h-5 w-5 shrink-0 animate-spin text-primary" />
        <div className="min-w-0">
          <p className="font-medium">Analyzing…</p>
          <p className="truncate text-sm text-muted-foreground">{url}</p>
        </div>
      </div>

      <Progress className="mt-5" value={progress} indicatorClassName="bg-primary" />

      <ul className="mt-6 space-y-4">
        {STAGES.map((item, index) => {
          const done = index < stage;
          const active = index === stage;
          return (
            <li key={item.label} className="flex items-start gap-3">
              <span
                className={cn(
                  "mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full ring-1",
                  done && "bg-primary/15 text-primary ring-primary/30",
                  active && "text-primary ring-primary/40",
                  !done && !active && "text-muted-foreground ring-border",
                )}
              >
                {done ? (
                  <Check className="h-3 w-3" />
                ) : active ? (
                  <Loader2 className="h-3 w-3 animate-spin" />
                ) : (
                  <span className="h-1.5 w-1.5 rounded-full bg-current" />
                )}
              </span>
              <div>
                <p
                  className={cn(
                    "text-sm font-medium",
                    !done && !active && "text-muted-foreground",
                  )}
                >
                  {item.label}
                </p>
                <p className="text-sm text-muted-foreground">{item.detail}</p>
              </div>
            </li>
          );
        })}
      </ul>

      <p className="mt-6 text-xs text-muted-foreground">
        This can take up to a minute for JavaScript-heavy pages.
      </p>
    </Card>
  );
}
