import { useState, type FormEvent } from "react";
import { analyzeUrl } from "@/lib/api";
import { AssessmentReport } from "@/components/AssessmentReport";
import type { Assessment } from "@/types/assessment";

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
      <div className="mx-auto max-w-3xl px-6 py-16">
        <h1 className="text-3xl font-bold tracking-tight">web-reviewer-bot</h1>
        <p className="mt-2 text-muted-foreground">
          Paste a product URL to get a market-potential assessment.
        </p>

        <form onSubmit={handleAnalyze} className="mt-8 flex gap-2">
          <input
            type="url"
            required
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            placeholder="https://example.com"
            className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />
          <button
            type="submit"
            disabled={loading}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
          >
            {loading ? "Analyzing…" : "Analyze"}
          </button>
        </form>

        {error && <p className="mt-6 text-sm text-destructive">{error}</p>}

        {result && <AssessmentReport result={result} />}
      </div>
    </div>
  );
}

export default App;
