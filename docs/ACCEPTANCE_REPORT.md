# Acceptance report

## Delivered

A documented source ZIP with Java/Spring Boot, React/TypeScript, a 48-product catalog, bundled assets, PostgreSQL schema, Redis login limits, Mailpit configuration, Dockerfiles/Compose, Maven Wrapper, npm lockfile, tests, and actual screenshots. Customer, seller and admin workflows use real APIs and persistent database records. The source was extracted into a fresh folder, compiled, and the core API/browser checks rerun.

## Functional evidence

36 passing API checks include authentication/registration/logout, CSRF denial, role/ownership isolation, catalog search/filter validation, cart quantity bounds, server totals, coupon per-user limit, successful/declined/delayed payments, repeated checkout/callback/cancellation/refund behavior, inventory release/restock, fulfillment transitions, verified reviews, seller revenue, admin inspection/suspension, product editing/archive/version conflict, variant creation, negative-stock rejection, upload validation, and audit records.

Six browser journey stages cover shopping through customer confirmation, seller shipment, customer tracking, and administrator inspection. Seven backend pricing/CSV unit tests and three frontend formatting tests pass. Eight PostgreSQL integration tests are included and skipped here because Docker is absent. See TESTING.md for the full environment boundary.

## Incomplete acceptance areas

This is a working portfolio demonstration, **not full acceptance of every requirement in REQUESTED_SPECIFICATION.txt**. Important gaps: no supplied reference image for comparison, partial use of photography, single-image gallery, compact JDBC/CSS architecture instead of requested JPA/Tailwind, simplified returns/onboarding/analytics, password-reset-only email delivery, single-instance sessions, no real payment provider, and unverified standard Docker/Java 21/Flyway/concurrency startup. See KNOWN_LIMITATIONS.md before presenting claims about production readiness or complete specification coverage.

## Visual verification

Actual desktop homepage, product page and seller dashboard screenshots are included, plus 390px home/product views. The UI uses the written navy/orange palette, compact navigation, category/product grids, product purchase panel, seller sidebar, metrics, chart, and order table. No reference image was attached, so no pixel-match claim is made. Mobile home/product pages measured no horizontal overflow; this is not an exhaustive accessibility or cross-browser certification.

## Safe demo defaults

Development-only credentials are documented. Local ports bind to loopback in Compose. Production profile disables seeding and the simulated checkout provider. No credentials for paid resources or real payments are present, and no deployment has been performed.
