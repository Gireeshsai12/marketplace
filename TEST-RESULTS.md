# Order-details regression checks — 2026-10-20

Observed results:
- Java compilation: PASS using Eclipse ECJ compiler targeting the available Java 17 runtime and the existing project's Spring dependency jars. No compiler/runtime is added to the deliverable.
- JUnit business-rule and order-presentation unit tests: 13 passed, 0 failed.
- TypeScript + Vite production build: PASS. Existing bundle-size warning remains.
- Vitest: 6 passed, 0 failed, including cancellation component rendering.
- Actual Java API + PostgreSQL WASM wire protocol engine + real Redis: 40 smoke checks passed.
- Actual Playwright browser journey: all 6 stages passed, including visible shipping address before/after shipping and absence of cancellation after shipment.
- Real PostgreSQL Testcontainers regression test added, but not run here: Docker is unavailable. The standard database concurrency limitations from the original report still apply.
- Windows PowerShell installer not executed here; package old/new hashes and installer logic reviewed. Source files are backed up, and existing compose/env/data files are excluded from the patch.

The SQL address::text projection makes the JSON boundary explicit; the response converts this string to plain address fields before serialization. No schema/data migration is needed. The can_cancel response field reflects payment and every seller portion's state. The actual cancellation endpoint retains its transaction-time checks for races.

Publication/deployment is not performed by this fix.
