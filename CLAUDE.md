# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`web-reviewer-bot` analyzes a web page/URL and outputs a **market-potential assessment** — an overall score plus dimension breakdowns (demand, willingness to try, willingness to pay, differentiation, likelihood of success, etc.), strengths, risks, and recommendations.

This is a multi-module repository split into two main systems:
1. **Frontend:** React + TypeScript (Vite SPA) + Tailwind CSS + ShadCN UI
2. **Backend:** Java Spring Boot REST API + PostgreSQL Database

> **Status:** The backend analyze pipeline is implemented — fetch (`RestClient`) → extract (`jsoup`) → analyze (Anthropic Java SDK with structured output), plus endpoint rate limiting, a global exception handler, and an optional Playwright rendering fallback for JS-heavy pages (off by default). The frontend is scaffolded (Vite SPA, API client, shared types); UI components are not built yet. Maintain strict separation between the `frontend/` and `backend/` directories.

## Commands

### Frontend (From /frontend directory)
```bash
npm install                     # install web UI dependencies
npx shadcn@latest init          # initialize ShadCN UI configuration
npx shadcn@latest add <item>    # add specific shadcn primitives (e.g. card, button)
npm run dev                     # start the React development server
npm run build                   # compile production build assets
npm run lint                    # run eslint checks
```

### Backend (From /backend directory)
```bash
mvn clean install               # compile and download Java project dependencies
mvn spring-boot:run              # start the Java Spring local backend server
```

## Environment & Database

### Backend Requirements (`backend/src/main/resources/application.properties`)
- **Database:** PostgreSQL configurations must be resolved via env bindings (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`). Provide a default fallback pointing to `localhost:5432`.
- **Database Initialization:** Place core structural DDL tables cleanly inside `backend/src/main/resources/schema.sql`.
- **Anthropic LLM:** Requires `ANTHROPIC_API_KEY` system environment mapping. 

## Architecture & Production-Grade Requirements

The system processes logic through an isolated, production-ready backend layout pipeline:

1. **Rate Limiting:** Protect API budgets. Implement an endpoint-level bucket-rate limiter using `Bucket4j` on the `/api/v1/analyze` path. Block users exceeding the token pool by throwing clean HTTP 429 exceptions.
2. **Dependency Injection (DI):** Strictly utilize constructor-based dependency injection across all Spring beans. Do not use field-level `@Autowired` attributes. Decouple components using explicit `Interface -> Implementation` design hierarchies.
3. **Fetch (Java):** Execute outbound HTTP requests to scrape domains on the server side using Spring's `RestClient` or `WebClient` wrapped with explicit connect/read timeouts.
4. **Extract (Java):** Clean raw layouts using the `jsoup` parsing library. Strip formatting and code nodes (`<script>`, `<style>`, `<nav>`, `<footer>`). Truncate final context payloads to a safe length before model processing.
5. **Analyze (Java):** Transmit processing prompts to Claude using the official [`com.anthropic:anthropic-java` SDK](https://github.com/anthropics/anthropic-sdk-java). Use strict record types or Jackson parsing structures to deserialize outputs safely.

### Claude Integration Conventions

- **Model:** Use `claude-opus-4-8` as the absolute system baseline configuration string. Support `ANTHROPIC_MODEL` string environment overrides.
- **Thinking Performance:** Enable adaptive extended thinking modes (`thinking: { type: "adaptive" }`). Remove legacy parameters like `temperature`, `top_p`, or custom token pool ranges when running this profile to prevent API errors.
- **Error Mapping:** Handle SDK errors (`AnthropicServiceException`) inside global exception handlers (`@ControllerAdvice`) to gracefully output readable error objects to the client application.

## Conventions

- **Frontend:** Pure functional React modules. Store UI design building blocks strictly within `@/components/ui/`. Enforce precise typing parameters across all components.
- **Backend:** Maintain clean encapsulation patterns (`Controller -> Service -> Repository`). All data payloads moving through services must use immutable Java `record` declarations. Code files must display standard JavaDoc summaries on structural class definitions.
