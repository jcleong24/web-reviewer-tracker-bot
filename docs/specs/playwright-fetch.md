# Spec: Playwright rendering + screenshot capture

**Status:** Implemented (defaults below chosen for the open questions)
**Date:** 2026-07-05

## 1. Goal

Improve extraction accuracy for JavaScript-rendered pages and capture visual
signals (design, layout, trust cues) that pure text loses, so Opus can make a
better market-potential judgment.

## 2. Decisions (agreed)

- **Strategy:** *Fallback when thin.* `RestClient` stays the fast default;
  Playwright is invoked only when the raw fetch yields too little text.
- **Vision:** *Text + screenshot.* Send Opus the rendered text **and** the
  screenshot as an image block.

## 3. Architecture

No change to `Controller → Service → Repository` layering. Fetching stays behind
the existing `PageFetchService` interface.

- New impl: `PlaywrightPageFetchService implements PageFetchService`
  (renders the page, returns HTML + a PNG screenshot).
- Existing `RestClientPageFetchService` is unchanged and remains the default.
- New orchestration in `AnalysisServiceImpl`:
  1. `RestClient` fetch → jsoup extract.
  2. If extracted text length `< webreviewer.fetch.thin-threshold`, re-fetch via
     Playwright (rendered HTML + screenshot), re-extract text.
  3. Send text (+ screenshot when present) to Opus.
- DTO change: `ExtractedContent` gains an optional `byte[]`/base64 `screenshot`
  field (immutable record preserved).
- Model call: add an image content block to the user message when a screenshot
  exists; text-only path is unchanged.

## 4. Configuration (application.properties, env-overridable)

| Key | Purpose | Default |
| --- | --- | --- |
| `webreviewer.fetch.playwright.enabled` | Master on/off switch | `false` |
| `webreviewer.fetch.thin-threshold` | Min chars before Playwright fallback triggers | `500` |
| `webreviewer.fetch.playwright.timeout-ms` | Render/nav timeout | reuse fetch timeout |
| `webreviewer.fetch.playwright.screenshot` | Capture screenshot | `true` |

## 5. Security (OWASP — SSRF is the primary concern)

Running a real browser against client-supplied URLs is a classic SSRF vector.
Required before shipping:

- Reject non-`http(s)` schemes (already partly done in `isValidHttpUrl`).
- **Block private/loopback/link-local ranges** and cloud metadata IPs
  (e.g. `169.254.169.254`, `127.0.0.0/8`, `10/8`, `192.168/16`, `172.16/12`).
- Resolve the host and validate the **resolved IP** (guard against DNS rebinding).
- Cap redirects; disable file downloads; block `file://`.
- Enforce hard nav/render timeouts and a max page/screenshot size.
- No new secrets; `ANTHROPIC_API_KEY` handling unchanged.

## 6. Operational impact

- **Dependency:** `com.microsoft.playwright:playwright` + browser binaries
  (~300MB+). Install step needed in build/Docker image.
- **Resources:** higher RAM/CPU per render; run headless with `--no-sandbox`
  considerations documented.
- **Latency:** seconds slower on the fallback path only (default path unaffected).
- **LLM cost:** screenshots add vision tokens on fallback calls only.

## 7. Out of scope

- Rendering every request (rejected — full-replacement strategy not chosen).
- Screenshot-only prompts (rejected — text + screenshot chosen).
- Video/scroll capture, multi-viewport screenshots.

## 8. Open questions — resolved

1. Screenshot scope: **above-the-fold viewport** (`full-page=false`, configurable).
   Cheaper, and where the pitch/hero lives.
2. Fallback failure: **degrade gracefully** to the thin RestClient text; log a
   warning, never fail the request on a render error.
3. Deploy target: no backend Dockerfile — the app runs via `mvn spring-boot:run`.
   Browser binaries install onto the host via the Playwright CLI (documented in
   `application.properties`); nothing Docker-side to change.

## 9. Implementation notes

- Fetching stays behind `PageFetchService`; rendering is a **separate**
  `PageRenderService` (`PlaywrightPageRenderService`) so the screenshot can ride
  alongside the HTML without distorting the fetch interface.
- Added a baseline SSRF guard (`UrlSafetyValidator`) applied before any
  outbound fetch/render — blocks loopback/private/link-local (incl. cloud
  metadata IP). Best-effort at DNS time; short timeouts back it up.
- `PlaywrightPageRenderService` is `@ConditionalOnProperty(...enabled=true)`, so
  the browser never launches while the feature is off.

## 10. Known follow-up — Opus call is non-streaming

The Analyze step calls Opus with `client.messages().create(...)` (non-streaming),
`max_tokens=16000` + adaptive thinking. This is the shape the Anthropic SDK flags
as stall-prone: on a cold first call the connection can sit silent until the
SDK's long timeout trips (observed once as a ~15-minute hang on the very first
request; warm requests return normally).

**Not fixed yet — working in practice.** If the silent multi-minute hang recurs,
the durable fix is to **stream** the call (`createStreaming(...)`, accumulate
text deltas, deserialize into `ModelAssessment`), which keeps the connection
alive with incremental events. Optionally set an explicit client timeout in
`AnthropicConfig` as a backstop.
