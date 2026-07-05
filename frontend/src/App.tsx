import { useState, type FormEvent } from "react";
import { Globe, LineChart, ScanText } from "lucide-react";
import { analyzeUrl } from "@/lib/api";
import { AssessmentReport } from "@/components/AssessmentReport";
import { Hero } from "@/components/Hero";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import type { Assessment } from "@/types/assessment";

const STEPS = [
  {
    icon: Globe,
    title: "Fetch",
    body: "The page is loaded server-side, rendering JavaScript when the raw HTML is thin.",
  },
  {
    icon: ScanText,
    title: "Extract",
    body: "Content is cleaned down to the text and visuals that actually signal potential.",
  },
  {
    icon: LineChart,
    title: "Assess",
    body: "Claude scores market potential across eight dimensions with cited reasoning.",
  },
];

function App() {
  const [url, setUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<Assessment | null>(null);

  async function handleAnalyze(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      setResult(await analyzeUrl(url));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <nav className="mx-auto flex max-w-5xl items-center gap-2.5 px-6 py-5">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 text-primary ring-1 ring-primary/20">
          <LineChart className="h-4 w-4" />
        </div>
        <span className="font-semibold tracking-tight">Web Analysis Tracker</span>
      </nav>

      <Hero>
        <form onSubmit={handleAnalyze} className="flex gap-2">
          <Input
            type="url"
            required
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            placeholder="https://example.com"
            className="flex-1"
          />
          <Button type="submit" disabled={loading}>
            {loading ? "Analyzing…" : "Analyze"}
          </Button>
        </form>
        {error && <p className="mt-4 text-sm text-destructive">{error}</p>}
      </Hero>

      <main className="mx-auto max-w-4xl px-6 pb-24">
        {result ? (
          <AssessmentReport result={result} />
        ) : (
          <div className="mt-16 grid gap-6 sm:grid-cols-3">
            {STEPS.map((step) => {
              const Icon = step.icon;
              return (
                <Card key={step.title} className="p-6">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary ring-1 ring-primary/20">
                    <Icon className="h-5 w-5" />
                  </div>
                  <h3 className="mt-4 font-semibold">{step.title}</h3>
                  <p className="mt-1 text-sm text-muted-foreground">{step.body}</p>
                </Card>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
