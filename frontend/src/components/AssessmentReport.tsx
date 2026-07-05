import type { ReactNode } from "react";
import { AlertTriangle, CheckCircle2, HelpCircle } from "lucide-react";
import type { Assessment, Rating } from "@/types/assessment";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

const RATING_VARIANT: Record<Rating, "success" | "info" | "warning" | "destructive"> = {
  Strong: "success",
  Promising: "info",
  Mixed: "warning",
  Weak: "destructive",
};

/** Progress-bar fill color banded by score: strong / mixed / weak. */
function scoreColor(score: number): string {
  if (score >= 70) return "bg-emerald-500";
  if (score >= 40) return "bg-amber-500";
  return "bg-red-500";
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
    <Card className="p-6">
      <h3 className="flex items-center gap-2 font-semibold">
        <span className={tone}>{icon}</span>
        {title}
      </h3>
      <ul className="mt-3 space-y-2 text-sm text-muted-foreground">
        {items.map((item, index) => (
          <li key={index} className="flex gap-2.5">
            <span className={cn("mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-current", tone)} />
            <span>{item}</span>
          </li>
        ))}
      </ul>
    </Card>
  );
}

/**
 * Renders a full market-potential assessment: overall score, per-dimension
 * breakdown, and the strengths / red-flags / diligence-question lists.
 */
export function AssessmentReport({ result }: { result: Assessment }) {
  return (
    <section className="mt-10 space-y-6">
      <Card className="p-6">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="truncate text-sm text-muted-foreground">{result.url}</div>
            <h2 className="mt-1 text-lg font-semibold">{result.pageTitle}</h2>
          </div>
          <Badge variant={RATING_VARIANT[result.rating]}>{result.rating}</Badge>
        </div>

        <div className="mt-5 flex items-baseline gap-2">
          <span className="text-5xl font-bold tracking-tight">{result.overallScore}</span>
          <span className="text-muted-foreground">/ 100</span>
        </div>
        <Progress
          className="mt-3"
          value={result.overallScore}
          indicatorClassName={scoreColor(result.overallScore)}
        />

        <p className="mt-5 font-medium leading-relaxed">{result.verdict}</p>
        <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{result.summary}</p>
      </Card>

      <Card className="p-6">
        <h3 className="font-semibold">Dimension breakdown</h3>
        <div className="mt-5 space-y-5">
          {result.dimensions.map((dimension) => (
            <div key={dimension.name}>
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium">{dimension.name}</span>
                <span className="tabular-nums text-muted-foreground">
                  {dimension.score}/100
                </span>
              </div>
              <Progress
                className="mt-2"
                value={dimension.score}
                indicatorClassName={scoreColor(dimension.score)}
              />
              <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                {dimension.reasoning}
              </p>
            </div>
          ))}
        </div>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        <ListSection
          title="Strengths"
          items={result.strengths}
          icon={<CheckCircle2 className="h-4 w-4" />}
          tone="text-emerald-400"
        />
        <ListSection
          title="Red flags"
          items={result.redFlags}
          icon={<AlertTriangle className="h-4 w-4" />}
          tone="text-red-400"
        />
      </div>

      <ListSection
        title="Diligence questions"
        items={result.diligenceQuestions}
        icon={<HelpCircle className="h-4 w-4" />}
        tone="text-sky-400"
      />
    </section>
  );
}
