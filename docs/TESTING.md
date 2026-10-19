# Test results

These are observed runs, not inferred successes.

| Check | Observed outcome |
|---|---|
| Java backend clean source build | PASS using Maven 3.9.9 and installed Java 17 with `-Djava.version=17` override |
| Java business-rule tests | 7 passed, 0 failed |
| Testcontainers PostgreSQL integration tests | 8 skipped: Docker executable/socket unavailable |
| Frontend clean `npm ci` | PASS from a fresh ZIP extraction |
| TypeScript and Vite production build | PASS; bundle-size warning remains (about 811KB main JS before gzip) |
| Vitest frontend formatter tests | 3 passed, 0 failed |
| API smoke script | 36 checks passed on fresh test data |
| Direct Playwright browser journey | 6 stages passed: customer login; search/variant/cart/checkout; history; seller fulfillment; customer tracking; admin inspection |
| Visual snapshots | 5 actual browser screenshots: desktop home/product/seller; mobile home/product |
| Browser runtime errors during screenshots | None observed |
| Mobile overflow | document scroll width 390 = viewport 390 on home and product |
| ZIP integrity and extraction | PASS; extracted source compiled and core API/browser smoke repeated |
| Docker Compose / Java 21 container startup | NOT RUN: Docker is unavailable; only Java 17 installed |
| Flyway migration through standard PostgreSQL | NOT VERIFIED: test runtime used schema SQL directly, see below |
| Mailpit SMTP/reset email delivery | NOT RUN: no local Mailpit executable/server |
| Maven Wrapper distribution bootstrap | BLOCKED in this environment by Java network/proxy access; jar integrity verified and cached Maven used instead |

## Environment boundary

The actual Java API ran against PostgreSQL 18.6 compiled to WASM (PGlite), exposed through a PostgreSQL wire socket, and real Redis 7.0.15. The one-connection test pool used JDBC prepareThreshold=0. The schema migration SQL executed successfully in that PostgreSQL engine, but Flyway's standard advisory-lock startup path could not be exercised reliably in this restricted environment, so Flyway was disabled only in the test process. The deliverable's default Compose uses ordinary PostgreSQL 16 and enables Flyway.

PGlite is not shipped as the application database or dependency. These runs verify API, SQL, security and state workflows but **do not verify multi-connection locking or overselling races on a production PostgreSQL server**. The eight real PostgreSQL Testcontainers tests cover those critical behaviors and must run on a Docker-capable host.

The standard Playwright test-runner worker path was restricted here. A direct Playwright browser script exercised the same journey using installed Chromium headless shell. Its result is reported as six journey stages, not six independent test-runner tests. No fabricated screenshots or mocked frontend catalog were used.

## Reproduce

Start the fresh Compose stack, then:

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm ci
npm run build
npm test
npx playwright install chromium
npm run e2e
cd ..
python scripts/api_smoke.py
node scripts/browser_smoke.cjs
```

Python smoke assumes freshly seeded demo data and makes deliberate mutations, including an archived test listing, suspension of a newly registered QA account, and paid/refunded orders. Reset the development volumes first if repeating it. Set API_URL / APP_URL when changing ports. Use CHROMIUM_PATH only if supplying an already installed alternate browser.

## Evidence

- api-smoke-results.txt: all 36 API checks.
- browser-test-results.txt: direct browser journey stages.
- visual-check-results.txt: runtime error and mobile-width measurements.
- java-build-results.txt: clean extracted-source Maven build, seven passing and eight skipped tests.
- screenshots/: actual pages captured at 1440px and 390px.
