# Runtime Validation Report

**Generated**: 2026-11-24T17:43:04+05:30  
**Target**: `C:\Users\sheel\OneDrive\Desktop\career-assistant`

## Summary

| Step | Status | Exit code | Details |
|---|---|---:|---|
| Startup | PASS | 0 | `/api/health` returned `UP` and `geminiConfigured: true`. |
| Maven tests | PASS | 0 | `mvn test`: 1 test passed, 0 failed. |
| Live API journeys | PASS | 0 | 3 tests passed, 0 failed, 0 skipped. |
| Live Gemini workflows | PASS | 0 | 9 AI-backed checks passed, including indexing, generation, evaluation, and grounded chat. |
| Browser E2E | PASS | 0 | All 9 UI screens rendered; browser workflows completed without uncaught page errors. |
| External services | PASS | 0 | ChromaDB heartbeat returned HTTP 200; Gemini generation and embedding calls succeeded. |

**Overall**: NEEDS_SIGNOFF — the running app was verified with `gemini-3.5-flash-lite`, but the local `.env` still explicitly selects the unavailable `gemini-2.5-flash-lite`; update that setting before the next restart.

## Feature Coverage

| Feature | Result | Evidence |
|---|---|---|
| Registration and login | PASS | Browser registration and login; live API login and invalid-password rejection. |
| Profile and password settings | PASS | Profile update/read-back; password change; old password rejected and new password accepted. |
| Dashboard | PASS | Protected summary returned and reflected application status/count changes. |
| Resume upload and extraction | PASS | Browser and API PDF upload; resume list/detail and skill extraction verified; resume deletion verified. |
| Job descriptions and matching | PASS | Job create/list and match create/read-back; expected skills and positive match score returned. |
| Application tracker | PASS | Create, filter, update status, dashboard count, and delete verified. |
| Chat conversations | PASS | Conversation create/list/messages/delete verified. |
| Resume AI analysis and report | PASS | Synthetic resume analysis returned a valid score and downloadable report. |
| Resume embeddings and RAG indexing | PASS | Uploaded test resume was indexed successfully. |
| Interview preparation | PASS | Question generation and answer evaluation returned valid responses. A transient 503 on one generation attempt succeeded on a bounded retry. |
| Learning roadmaps | PASS | Roadmap generation returned tasks; task completion persisted. |
| Grounded chat answers | PASS | Embedding, Chroma retrieval, answer generation, and source reference were verified. |

## Test Evidence

- Existing Maven test: `mvn test` — 1 passed.
- Repeatable live API tests:
  - [`auth-profile-tracker.test.js`](../tests/runtime-validation/auth-profile-tracker.test.js)
  - [`resume-job-match.test.js`](../tests/runtime-validation/resume-job-match.test.js)
  - [`ai-features.test.js`](../tests/runtime-validation/ai-features.test.js)
- Explicit live Gemini test: [`gemini-live.js`](../tests/runtime-validation/gemini-live.js) — exit code 0, 9 checks passed.
- Browser coverage: signup/login, all nine navigation screens, profile update, resume upload/analyze/delete, job creation/matching, interview/roadmap configuration errors, tracker create/edit/delete, chat creation/configuration error, and password change/re-login.
- Environment: Docker 29.8.1 available; Node.js v22.14.0 available; Chromium installed with `npx playwright install chromium`.
- Database and app containers remained running after verification. Temporary users, database records, and uploaded resume files were removed.

## Limitations and Notes

- Google API reported that `gemini-2.5-flash-lite` is unavailable to new users and recommended `gemini-3.5-flash-lite`. A direct synthetic generation request to `gemini-3.5-flash-lite` returned HTTP 200.
- Source defaults in `application.yml` and `compose.yaml` now use `gemini-3.5-flash-lite`. The current container was started with that model via a process-level override; change `GEMINI_MODEL` in `.env` to the same value so future Compose restarts keep working.
- One interview generation request returned HTTP 503; a bounded retry and a separate generate/evaluate run both succeeded.
- An earlier environment-file check found a non-empty API key in `.env.example`. Rotate that key if you have not already, and replace the example value with a placeholder.
- The live Gemini suite sends synthetic prompts and can consume API quota. Its test data is cleaned automatically.
- This was a feature-level smoke pass, not an exhaustive edge-case, load, or security test.
- Requests to protected endpoints without a bearer token are denied with HTTP 403. Invalid login credentials return HTTP 401.
- Startup logs contain a non-blocking Spring Security configuration warning; the tested authentication flows succeeded.
- This was a feature-level smoke pass, not an exhaustive edge-case, load, or security test.

## Verdict

```yaml
environment:
  docker: AVAILABLE — Docker server 29.8.1
  node: AVAILABLE — v22.14.0
  playwright: AVAILABLE — Chromium installed; browser automation exercised
  infra-tier: PRIMARY(Docker-based) — Compose MySQL and ChromaDB were running
  browser-tier: PRIMARY(Playwright) — integrated browser used for UI workflows
startup: PASS — /api/health returned UP and geminiConfigured: true
integration: PASS — exit_code: 0; mvn test, 1 passed, 0 failed
e2e: PASS — exit_code: 0; 3 live API journeys and 9 live Gemini checks passed; all 9 UI screens rendered
overall: NEEDS_SIGNOFF — current container works with gemini-3.5-flash-lite; update GEMINI_MODEL in .env to persist this across restarts
```
