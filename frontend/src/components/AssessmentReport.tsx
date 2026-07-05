import type { ReactNode } from "react";
import { AlertTriangle, CheckCircle2, HelpCircle } from "lucide-react";
import type { Assessment, Rating } from "@/types/assessment";
import { cn } from "@/lib/utils";

const RATING_STYLES: Record<Rating, string> = {
  Strong: "bg-emerald-100 text-emerald-800",
  Promising: "bg-sky-100 text-sky-800",
  Mixed: "bg-amber-100 text-amber-800",
  Weak: "bg-red-100 text-red-800",
};

/** Bar fill color banded by score: strong / mixed / weak. */
function scoreColor(score: number): string {
  if (score >= 70) return "bg-emerald-500";
  if (score >= 40) return "bg-amber-500";
  return "bg-red-500";
}

function RatingBadge({ rating }: { rating: Rating }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium",
        RATING_STYLES[rating] ?? "bg-muted text-muted-foreground",
      )}
    >
      {rating}
    </span>
  );
}

function ScoreBar({ score }: { score: number }) {
  const clamped = Math.max(0, Math.min(100, score));
  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-muted">
      <div
        className={cn("h-full rounded-full transition-all", scoreColor(clamped))}
        style={{ width: `${clamped}%` }}
      />
    </div>
  );
}

function ListSection({
  title,
  items,
  icon,
  tone,
}: {
  title: string;
  items: string[];
  icon: ReactNode;
  tone: string;
}) {
  if (items.length === 0) return null;
  return (
    <div className="rounded-lg border border-border p-6">
      <h3 className="flex items-center gap-2 font-semibold">
        <span className={tone}>{icon}</span>
        {title}
      </h3>
      <ul className="mt-3 space-y-2 text-sm">
        {items.map((item, index) => (
          <li key={index} className="flex gap-2">
            <span className="text-muted-foreground">•</span>
            <span>{item}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

/**
 * Renders a full market-potential assessment: overall score, per-dimension
 * breakdown, and the strengths / red-flags / diligence-question lists.
 */
export function AssessmentReport({ result }: { result: Assessment }) {
  return (
    <section className="mt-10 space-y-6">
      <div className="rounded-lg border border-border p-6">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="truncate text-sm text-muted-foreground">{result.url}</div>
            <h2 className="mt-1 text-lg font-semibold">{result.pageTitle}</h2>
          </div>
          <RatingBadge rating={result.rating} />
        </div>

        <div className="mt-4 flex items-baseline gap-2">
          <span className="text-4xl font-bold tracking-tight">{result.overallScore}</span>
          <span className="text-muted-foreground">/ 100</span>
        </div>
        <div className="mt-3">
          <ScoreBar score={result.overallScore} />
        </div>

        <p className="mt-4 font-medium">{result.verdict}</p>
        <p className="mt-2 text-sm text-muted-foreground">{result.summary}</p>
      </div>

      <div className="rounded-lg border border-border p-6">
        <h3 className="font-semibold">Dimension breakdown</h3>
        <div className="mt-4 space-y-4">
          {result.dimensions.map((dimension) => (
            <div key={dimension.name}>
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium">{dimension.name}</span>
                <span className="tabular-nums text-muted-foreground">
                  {dimension.score}/100
                </span>
              </div>
              <div className="mt-1.5">
                <ScoreBar score={dimension.score} />
              </div>
              <p className="mt-1.5 text-sm text-muted-foreground">{dimension.reasoning}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <ListSection
          title="Strengths"
          items={result.strengths}
          icon={<CheckCircle2 className="h-4 w-4" />}
          tone="text-emerald-600"
        />
        <ListSection
          title="Red flags"
          items={result.redFlags}
          icon={<AlertTriangle className="h-4 w-4" />}
          tone="text-red-600"
        />
      </div>

      <ListSection
        title="Diligence questions"
        items={result.diligenceQuestions}
        icon={<HelpCircle className="h-4 w-4" />}
        tone="text-sky-600"
      />
    </section>
  );
}
