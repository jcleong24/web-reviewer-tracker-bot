# web-reviewer-bot — Product Spec

> This is the product prompt: the source-of-truth description of *what* to build.
> `CLAUDE.md` covers *how* this repo works (stack, commands, conventions).
> Build the app to satisfy this spec.

## One-liner

Paste a product/startup URL; get an instant, structured **market-potential assessment** — an overall verdict plus scored dimensions, with reasoning and diligence advice.

## Who it's for

An **investor or scout screening other people's products**. They land on an unfamiliar product page and want a fast, structured read on whether the market wants this — a decision-support tool, not a cheerleader. Tone: candid, comparative, risk-aware. Frame advice as *what to dig into*, not *how to fix your copy*.

## Core flow

1. User pastes a URL and clicks **Analyze**.
2. App fetches the page server-side and extracts its meaningful text.
3. Claude scores it and returns a structured assessment.
4. UI renders a **scorecard dashboard** with the verdict, dimension scores + reasoning, strengths, red flags, and diligence questions.

Single screen. No login, no saved history (see Non-goals).

## Input

- A single URL (validate it's a well-formed http/https URL before fetching).
- Handle failure gracefully with a clear message: unreachable site, timeout, non-HTML response, or a page with too little extractable text to assess. Never score a page the model couldn't actually read — say so instead.

## The assessment (core contract)

This structure is the product. The zod schema, the API response type, and the dashboard must stay in sync with it.

**Top level**
- `overallScore` — 0–100.
- `rating` — one of `Strong` | `Promising` | `Mixed` | `Weak`.
- `verdict` — one candid sentence: would the market want this, and would people pay?
- `summary` — 2–4 sentences expanding on the verdict.

**Dimensions** — each has `score` (0–100) and `reasoning` (1–2 sentences citing what on the page drove the score):
- **Market demand** — is there a real, felt problem here, and how many people have it?
- **Willingness to try** — how much friction to adoption (price, setup, switching cost, trust)?
- **Willingness to pay** — is there a credible path to revenue / evidence people pay for this?
- **Differentiation** — how distinct is this from existing alternatives; any moat?
- **Target audience clarity** — is it obvious who this is for?
- **Value proposition clarity** — does the page make the benefit unmistakable in seconds?
- **Traction & credibility signals** — visible proof (customers, logos, reviews, pricing, team) vs. vaporware feel.
- **Success likelihood** — overall odds this finds a market, all things considered.

**Analyst notes**
- `strengths` — 2–4 bullet points, what's genuinely working in this product's favor.
- `redFlags` — 2–4 bullet points, risks or gaps a scout should worry about.
- `diligenceQuestions` — 3–5 pointed questions the investor should ask the founder before betting.

Scoring must be grounded in what's actually on the page. If a dimension can't be judged from the page (e.g. no pricing shown), say so in the reasoning and score conservatively rather than inventing signal.

## Output / UI

- Prominent **overall score + rating + verdict** at the top (color-coded by rating).
- **Dimension grid**: each dimension as a labeled score (bar or ring) with its one-line reasoning.
- **Strengths / Red flags** side by side.
- **Diligence questions** as a checklist-style list.
- Show the analyzed URL and page title. Clean, scannable, single page. Loading state while analyzing (this takes a few seconds).

## Non-goals (MVP boundaries)

- No accounts / auth.
- No database or saved history.
- No comparing multiple URLs.
- No crawling beyond the single submitted page.
- No PDF export or sharing.

(These are deferred, not rejected — keep the analysis logic modular so history/comparison can be added later.)

## Tech (decided)

React.js + TypeScript web app; server-side fetch + `cheerio` extraction; Claude (`claude-opus-4-8`) for analysis via the official SDK with `zod` structured output. See `CLAUDE.md` for the pipeline, API-route boundary, and Claude integration rules.

## Done when

- Pasting a real product URL returns a filled-in scorecard within a few seconds.
- A dead/invalid/empty URL produces a clear error, not a crash or a fabricated score.
- `npm run typecheck` and `npm run build` pass.
