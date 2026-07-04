# Frontend — web-reviewer-bot

React + TypeScript **SPA** built with Vite, styled with Tailwind CSS and ShadCN UI.

## Setup

```bash
npm install
npx shadcn@latest init      # if regenerating ShadCN config (components.json already present)
npm run dev                 # http://localhost:5173
```

`npm run dev` proxies `/api/*` to the Spring backend at `http://localhost:8080`
(see `vite.config.ts`), so run the backend alongside it. The browser never
fetches target domains directly — that happens server-side in the backend.

## Scripts

- `npm run dev` — start the Vite dev server
- `npm run build` — type-check (`tsc --noEmit`) and produce a production build
- `npm run preview` — preview the production build
- `npm run lint` — run ESLint

## Structure

- `src/App.tsx` — root component; posts a URL and renders the assessment
- `src/lib/api.ts` — calls `POST /api/v1/analyze`
- `src/types/assessment.ts` — assessment contract (keep in sync with the backend DTO)
- `src/components/ui/` — ShadCN primitives (add via the ShadCN CLI)
- `@/*` path alias maps to `src/*`
