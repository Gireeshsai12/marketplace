# Marketplace

A full-stack, multi-vendor e-commerce portfolio application built with **Java Spring Boot, React, TypeScript, PostgreSQL, Redis, and Docker**.

Marketplace connects customer shopping workflows with seller inventory management and administrator tools. Its backend includes transactional checkout, stock reservations, idempotent order creation, seller-specific fulfillment, and an inventory ledger.

> **Demo scope:** Payments, refunds, taxes, and carrier tracking are simulated. This application does not process real money or arrange real deliveries.

---

## Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Backend Design](#backend-design)
- [Getting Started](#getting-started)
- [Demo Accounts](#demo-accounts)
- [Application Walkthrough](#application-walkthrough)
- [Configuration](#configuration)
- [API Overview](#api-overview)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Useful Commands](#useful-commands)
- [Troubleshooting](#troubleshooting)
- [Deployment Status](#deployment-status)
- [Known Limitations](#known-limitations)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Overview

Marketplace demonstrates how a shopping application coordinates multiple business workflows beyond displaying products and accepting a cart.

Customers can browse products, select variants, manage saved addresses, place demo orders, and follow fulfillment progress. Sellers manage their own listings, stock, orders, and returns. Administrators inspect platform activity and moderate users, stores, products, and reviews.

The application includes:

- A seeded catalog of 48 products with two inventory variants per product.
- Separate customer, seller, and administrator experiences.
- PostgreSQL-backed application data.
- Redis-backed login throttling.
- Local password-reset email delivery through Mailpit.
- Docker Compose orchestration for the complete development environment.
- Unit tests, API smoke checks, and browser journeys.

---

## Features

### Customer Experience

- Browse a product catalog with category navigation.
- Search products and receive search suggestions.
- Filter by category, seller, price, rating, and stock availability.
- Sort and paginate search results.
- View product details and select variants.
- Register and sign in using session-based authentication.
- Request password-reset emails.
- Save shipping addresses.
- Add products to a cart and update quantities.
- Save products to a wishlist.
- Preview checkout totals.
- Apply eligible promotion codes.
- Choose standard or express demo shipping.
- Exercise successful, declined, delayed, and timeout payment scenarios.
- View order history and order details.
- Follow seller-specific fulfillment progress.
- Cancel eligible orders before shipment.
- Request returns for eligible seller portions.
- Submit reviews when purchase eligibility is satisfied.
- Read in-app notifications.

### Seller Workspace

- View sales and revenue summaries.
- Inspect revenue charts and product rankings.
- Create and edit product listings.
- Manage product variants and prices.
- Upload validated product images.
- Adjust stock with a recorded reason.
- Inspect inventory movement history.
- Search and filter seller-owned orders.
- Update fulfillment status and tracking information.
- Review and approve return requests.
- Edit the store name.
- Export seller data as CSV.

### Administrator Tools

- Inspect users and sellers.
- Activate or suspend eligible users and stores.
- Moderate listings and reviews.
- Manage categories.
- Create and expire promotions.
- Inspect orders and their saved item snapshots.
- Review returns and refund records.
- Inspect audit and notification-outbox records.
- Complete simulated late-payment refunds.

---

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend language | Java 21 | Application and business logic |
| Backend framework | Spring Boot | REST API and application runtime |
| Authentication | Spring Security | Sessions, authorization, and CSRF protection |
| Database access | Spring JDBC | SQL queries and transactional data access |
| Validation | Jakarta Bean Validation | Request validation |
| Database | PostgreSQL 16 | Persistent application data |
| Schema migrations | Flyway | Versioned database changes |
| Throttling | Redis 7 | Login attempt throttling |
| Frontend | React 19 | Customer, seller, and admin interfaces |
| Frontend language | TypeScript | Typed frontend development |
| Build tooling | Vite | Development server and production builds |
| Routing | React Router | Client-side navigation |
| Server state | TanStack Query | API data fetching and caching |
| Forms | React Hook Form and Zod | Form handling and validation |
| Charts | Recharts | Seller analytics |
| Icons | Lucide React | Interface icons |
| Styling | Custom CSS | Responsive marketplace styling |
| Reverse proxy | Nginx | Static frontend hosting and API forwarding |
| Development email | Mailpit | Local SMTP capture and email inspection |
| Containers | Docker and Docker Compose | Reproducible local environment |
| Backend tests | JUnit and Spring Boot Test | Backend verification |
| Database integration tests | Testcontainers | PostgreSQL-backed integration scenarios |
| Frontend tests | Vitest | Component and formatting checks |
| Browser tests | Playwright | End-to-end journeys |
| API documentation | Springdoc OpenAPI | Swagger UI and API specification |

**Implementation note:** The backend uses JDBC rather than JPA/Hibernate, and the frontend uses custom CSS rather than Tailwind.

---

## Architecture

The local environment runs five services:

| Service | Responsibility |
|---|---|
| Frontend / Nginx | Serves the React build and forwards API requests |
| Spring Boot backend | Handles authentication, business rules, and database operations |
| PostgreSQL | Stores users, catalog data, orders, inventory records, and platform activity |
| Redis | Supports login throttling |
| Mailpit | Captures development password-reset emails |

The browser accesses the application through Nginx. Requests under `/api/` are forwarded to Spring Boot, while uploaded-image requests are forwarded under `/uploads/`.

The backend connects to PostgreSQL, Redis, and Mailpit through the Docker Compose network.

### Local URLs

| Resource | URL |
|---|---|
| Marketplace | http://localhost:8090 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Backend health | http://localhost:8080/actuator/health |
| Frontend health | http://localhost:8090/healthz |
| Mailpit inbox | http://localhost:8025 |

The current local frontend port is **8090**. If you change the Compose port mapping, use the corresponding port in your browser and test configuration.

---

## Backend Design

### Transactional Checkout

Checkout calculates prices on the server rather than accepting totals supplied by the browser.

The checkout workflow coordinates:

1. Customer and cart validation.
2. Product and seller availability.
3. Variant inventory checks.
4. Promotion eligibility.
5. Stock reservation.
6. Order and seller-portion creation.
7. Saved order-item snapshots.
8. Demo payment handling.
9. Inventory and notification records.

Database transactions group related changes so that checkout operations can succeed or roll back together.

### Concurrency Controls

Checkout uses database row locking to coordinate competing operations.

Variant rows are locked during stock-sensitive operations. A customer-level lock coordinates that customer's checkout, cart, and promotion-use operations.

These mechanisms are intended to prevent conflicting purchases and inconsistent stock changes. PostgreSQL concurrency tests are included; their execution requires a Docker-capable test environment.

### Idempotent Order Creation

Checkout requires an `Idempotency-Key` header.

Repeated requests using the same key return the original order rather than creating another purchase. A new purchase must use a new key.

This supports retry scenarios such as a client repeating a request after an uncertain network response.

### Multi-Seller Orders

One checkout can create a parent order containing multiple seller portions.

Each seller portion groups the items belonging to one store, allowing sellers to manage fulfillment independently.

Customers can inspect the parent order while following each seller portion's progress.

### Order Snapshots

Order items retain saved purchase details instead of relying entirely on the current product listing.

This preserves the order's historical item and price information when a seller later changes the catalog.

### Inventory Ledger

Stock adjustments produce inventory-history records.

The ledger supports inspection of inventory changes associated with purchases, releases, returns, and seller adjustments.

### Reservation Expiry

Pending orders have an expiry workflow.

A scheduled backend task processes expired reservations and releases inventory according to the order policy.

### Payment Event Deduplication

The demo payment adapter tracks payment events to avoid repeatedly applying the same event.

Late successful payment events follow a refund-required policy when the original reservation can no longer be fulfilled.

> Demo payment endpoints are authenticated test adapters. They are not production payment-provider webhooks.

### Cancellation Rules

Cancellation is available only when:

- The payment state is `PAID` or `PENDING`.
- The order contains seller portions.
- Every seller portion is still `PROCESSING`.

The backend returns a `can_cancel` value for the order page. The frontend uses it to show or hide the cancellation button.

The cancellation endpoint also checks the current order state before applying changes.

### Notification Outbox

Business actions can write notification events to an outbox.

A scheduled processor creates in-app notifications from those events. Outbox records track processing attempts and errors and are available for administrator inspection.

Current delivery is **in-app only**. Password-reset email uses a separate SMTP workflow.

---

## Getting Started

### Prerequisites

For the Docker setup:

- Git.
- Docker Desktop with Docker Compose.
- A running Docker engine.
- Available local ports for the services.

Java, Maven, and Node.js do not need to be installed locally when running the application entirely through Docker.

### 1. Clone the Repository

```powershell
git clone https://github.com/Gireeshsai12/marketplace.git
cd marketplace
```

### 2. Create the Environment File

For a new checkout:

```powershell
Copy-Item .env.example .env
```

If `.env` already exists, keep your existing configuration.

Example development values:

```dotenv
DATABASE_PASSWORD=marketplace-dev
COOKIE_SECURE=false
DEMO_ENABLED=true
```

Do not commit `.env`.

### 3. Check Docker

```powershell
docker version
docker compose version
```

`docker version` should display both client and server information.

### 4. Build and Start

```powershell
docker compose up -d --build
```

The first build downloads container images and application dependencies.

### 5. Check Service Health

```powershell
docker compose ps
```

Allow time for PostgreSQL initialization, migrations, application startup, and health checks.

### 6. Open the Application

Visit:

http://localhost:8090

Use one of the development accounts below to explore the application.

---

## Demo Accounts

These credentials are intentionally provided for the development demo.

| Role | Email | Password |
|---|---|---|
| Customer | customer@marketplace.local | MarketDemo!2026 |
| Seller | seller@marketplace.local | MarketDemo!2026 |
| Second seller | seller2@marketplace.local | MarketDemo!2026 |
| Administrator | admin@marketplace.local | MarketDemo!2026 |

The login page includes buttons that fill demo credentials. Select an account and click **Sign in**.

New registrations create customer accounts.

Seller dashboards begin without fabricated sales. Orders created through demo checkout populate the relevant seller data.

---

## Application Walkthrough

### Customer Purchase

1. Sign in as the demo customer.
2. Browse or search the catalog.
3. Open a product and choose a variant.
4. Add the item to the cart.
5. Open checkout.
6. Select or create a shipping address.
7. Choose a shipping method.
8. Select a demo payment scenario.
9. Place the order.
10. Open order details to inspect totals and fulfillment.

### Seller Fulfillment

1. Sign out of the customer account.
2. Sign in as the seller responsible for the purchased item.
3. Open the seller order workspace.
4. Find the corresponding seller portion.
5. Update fulfillment status and tracking details.
6. Return to the customer account to inspect the updated order.

### Administrator Inspection

1. Sign in as the administrator.
2. Open the order-management view.
3. Inspect the order and its saved details.
4. Explore moderation, promotions, audit records, and outbox inspection.

### Password Reset

1. Request a password reset through the application.
2. Open http://localhost:8025.
3. Inspect the captured reset email.
4. Follow the reset path.

The development reset link may still reference port `8088`. If necessary, change the link's origin to `http://localhost:8090` while preserving its `/reset?token=...` path.

---

## Configuration

| Variable | Purpose | Local development default |
|---|---|---|
| `DATABASE_URL` | JDBC PostgreSQL connection URL | Compose points to the PostgreSQL service |
| `DATABASE_USER` | Database username | `marketplace` |
| `DATABASE_PASSWORD` | Database password | `marketplace-dev` |
| `REDIS_HOST` | Redis hostname | `redis` in Compose |
| `MAIL_HOST` | SMTP hostname | `mailpit` in Compose |
| `UPLOAD_DIR` | Uploaded-image directory | `/app/uploads` in Compose |
| `COOKIE_SECURE` | Restricts session cookies to HTTPS | `false` locally |
| `DEMO_ENABLED` | Enables demo behavior | `true` |

The supplied application configuration uses:

- Backend HTTP port: `8080`.
- Redis port: `6379`.
- Development SMTP port: `1025`.
- Maximum multipart upload size: `5 MB`.

Redis authentication, TLS, alternate service ports, and public deployment settings require additional configuration when using external providers.

### Persistent Local Data

Docker Compose uses named volumes for:

- PostgreSQL data.
- Uploaded product images.

Normal container rebuilds and `docker compose down` preserve these volumes.

Do not delete volumes if you want to retain existing accounts, orders, and uploads.

---

## API Overview

Application endpoints use the `/api` prefix.

For example, the authentication endpoint is:

```text
POST /api/auth/login
```

### Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/auth/csrf` | Obtain a CSRF token |
| POST | `/api/auth/register` | Register a customer |
| POST | `/api/auth/login` | Sign in |
| GET | `/api/auth/me` | Read the current user |
| POST | `/api/auth/logout` | Sign out |
| POST | `/api/auth/forgot` | Request a reset email |
| POST | `/api/auth/reset` | Reset a password |

State-changing requests send the CSRF token through the `X-XSRF-TOKEN` header together with the session cookie.

### Catalog and Shopping

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/categories` | List categories |
| GET | `/api/stores` | List stores |
| GET | `/api/products` | Search and filter products |
| GET | `/api/products/{id}` | Read product details |
| GET | `/api/suggestions` | Retrieve search suggestions |
| GET / POST | `/api/addresses` | List or create addresses |
| GET / POST | `/api/cart` | Read or add cart items |
| PUT / DELETE | `/api/cart/{variantId}` | Update or remove a cart item |
| GET | `/api/wishlist` | Read the wishlist |
| POST / DELETE | `/api/wishlist/{productId}` | Add or remove a wishlist item |

### Checkout and Orders

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/checkout/quote` | Calculate checkout totals |
| POST | `/api/checkout` | Create an order |
| GET | `/api/orders` | List customer orders |
| GET | `/api/orders/{id}` | Read order details |
| POST | `/api/orders/{id}/cancel` | Cancel an eligible order |
| POST | `/api/orders/{id}/demo-payment` | Submit a demo payment event |
| POST | `/api/orders/{id}/returns/{sellerPortionId}` | Request a return |

Example checkout body:

```json
{
  "addressId": 1,
  "shipping": "STANDARD",
  "mode": "success"
}
```

Checkout requests must include a unique key for each new purchase:

```http
Idempotency-Key: marketplace-demo-purchase-001
```

### Seller and Admin APIs

Seller endpoints are grouped under:

```text
/api/seller/
```

Administrator endpoints are grouped under:

```text
/api/admin/
```

See [docs/API.md](docs/API.md) and the generated Swagger UI for additional routes and request details.

### Error Responses

Application errors use a JSON structure such as:

```json
{
  "code": "COUPON_INELIGIBLE",
  "message": "Coupon is expired, exhausted, previously used, or below minimum"
}
```

Responses include an `X-Correlation-ID` header to support troubleshooting.

---

## Testing

Run smoke and browser tests against a dedicated development database. These tests create and modify application data.

### Backend Tests

From the repository root:

```powershell
cd backend
.\mvnw.cmd test
```

On macOS or Linux:

```bash
cd backend
./mvnw test
```

The backend includes PostgreSQL integration tests using Testcontainers. Docker must be available for those scenarios; inspect the test report for skipped tests.

### Frontend Unit Tests

```powershell
cd frontend
npm ci
npm test
```

### Frontend Production Build

```powershell
npm run build
```

This runs TypeScript checks and creates the Vite production build.

### Playwright Browser Tests

With the Docker application running:

```powershell
cd frontend
npx playwright install chromium
$env:APP_URL = "http://localhost:8090"
npm run e2e
```

`APP_URL` is required for the current port because the test configuration defaults to `8088`.

### API Smoke Checks

From the repository root, with the backend running:

```powershell
python scripts/api_smoke.py
```

The script targets the local API on port `8080`.

### Verification Scope

Recent regression checks covered:

- Shipping-address serialization.
- Cancellation eligibility before and after shipment.
- Frontend cancellation-button rendering.
- API shopping and order workflows.
- Browser journeys.
- Frontend compilation and bundling.

The latest development validation reported 13 Java unit tests, 6 frontend tests, 40 API smoke checks, and 6 browser journey stages passing.

The API/browser validation environment used PostgreSQL-compatible PGlite and real Redis. Those results do not establish a successful real-PostgreSQL concurrency-test run.

Local Windows Docker startup, the order-page fixes, and healthy service statuses were subsequently checked manually.

See:

- [docs/TESTING.md](docs/TESTING.md)
- [docs/ACCEPTANCE_REPORT.md](docs/ACCEPTANCE_REPORT.md)
- [docs/ORDER_FIX.md](docs/ORDER_FIX.md)

---

## Project Structure

| Path | Contents |
|---|---|
| `backend/` | Spring Boot application, Maven configuration, and backend tests |
| `backend/src/main/java/com/marketplace/` | Controllers, security, persistence helpers, and business logic |
| `backend/src/main/resources/` | Application configuration and database migrations |
| `backend/src/test/` | Backend unit and integration tests |
| `frontend/` | React application and frontend tooling |
| `frontend/src/` | Pages, components, API helpers, styles, and unit tests |
| `frontend/public/assets/` | Bundled product imagery and illustrations |
| `frontend/e2e/` | Playwright browser tests |
| `frontend/nginx.conf` | Static serving and reverse-proxy configuration |
| `scripts/` | API and browser smoke-check scripts |
| `docs/` | API documentation, testing notes, and scope limitations |
| `docker-compose.yml` | Local service orchestration |
| `.env.example` | Example development configuration |
| `.gitignore` | Exclusions for secrets, dependencies, uploads, and generated files |

---

## Useful Commands

### Start Existing Containers

```powershell
docker compose up -d
```

### Rebuild the Application

```powershell
docker compose up -d --build backend frontend
```

### Check Status

```powershell
docker compose ps
```

### Read Backend Logs

```powershell
docker compose logs --tail=100 backend
```

### Follow Logs

```powershell
docker compose logs -f backend frontend
```

### Stop the Environment

```powershell
docker compose down
```

This preserves named volumes.

### Inspect Frontend Health

```powershell
docker inspect marketplace-frontend-1 --format '{{json .State.Health}}'
```

---

## Troubleshooting

### Docker Engine Is Unavailable

Start Docker Desktop and wait for the engine to finish starting.

Then run:

```powershell
docker version
```

If Docker is not recognized, reopen the terminal after installing Docker Desktop.

### Port Already in Use

Check `docker-compose.yml` for a conflicting host port.

The frontend uses:

```yaml
ports:
  - "127.0.0.1:8090:80"
```

PostgreSQL does not need a host-port mapping when only the backend container accesses it. The backend connects through the internal Compose hostname `postgres`.

### Frontend Is Unhealthy but the Website Loads

Use the explicit IPv4 loopback address in the frontend health probe:

```text
http://127.0.0.1/healthz
```

A probe using `localhost` can fail when it resolves to an address on which Nginx is not listening.

Verify the endpoint:

```powershell
docker compose exec frontend wget -S -O - http://127.0.0.1/healthz
```

### Source Changes Do Not Appear

Confirm that changes are inside the actual `backend/` and `frontend/` directories.

Rebuild:

```powershell
docker compose up -d --build backend frontend
```

Refresh the browser with **Ctrl+F5**.

### Password-Reset Link Uses the Wrong Port

Keep the token and reset path, but replace the development origin with the active frontend URL.

### Backend Startup Fails

Inspect the backend logs:

```powershell
docker compose logs --tail=150 backend
```

Check database availability, credentials, migrations, and dependent service health.

---

## Deployment Status

The application has been run and checked locally with Docker.

**Public hosting is being prepared. A live deployment URL is not yet available.**

The current Compose setup is intended for local development. Public hosting requires additional work for:

- Provider-specific build and startup configuration.
- HTTPS and secure cookies.
- Database credentials and TLS.
- Redis connection configuration.
- Persistent uploaded-image storage.
- Public password-reset URLs.
- Email delivery.
- Resource limits and operational monitoring.

A free hosted demo may have cold starts, usage limits, and storage restrictions.

---

## Known Limitations

- No real payment processor, refund provider, tax service, or carrier integration.
- Demo sellers are seeded; seller registration and approval are not implemented.
- Sessions are local to one backend instance.
- Uploaded images use local filesystem storage.
- Returns cover an entire seller portion rather than selected item quantities.
- Return handling uses a simplified approval and refund workflow.
- Only password-reset emails use SMTP; order notifications are delivered in-app.
- Customer address editing/deletion and guest-cart merging are not implemented.
- Product galleries do not provide a complete multi-image, per-variant image model.
- Catalog imagery combines photographs with original SVG illustrations.
- Production account security, abuse controls, token handling, and infrastructure require further work.
- Map-based API responses limit the detail of some generated OpenAPI schemas.
- The production profile disables the demo provider and seeding; real checkout requires another payment adapter.

See [docs/KNOWN_LIMITATIONS.md](docs/KNOWN_LIMITATIONS.md) for additional scope details. Some earlier execution notes describe the original delivery environment; this README separates subsequent local checks from automated validation.

---

## Future Improvements

- Integrate a real payment provider with verified webhook signatures.
- Store uploads in durable object storage.
- Add seller onboarding and approval.
- Add shared session storage for multiple backend instances.
- Configure production email delivery and public reset-link origins.
- Add partial returns and quantity-based refunds.
- Expand product galleries and variant-specific images.
- Improve API request and response DTOs.
- Expand concurrency, failure-recovery, and accessibility tests.
- Add continuous integration and repeatable deployment workflows.
- Add structured operational dashboards and alerting.

These are planned enhancements, not currently implemented features.

---

## Author

**Gireesh Sai Kalluri**

- GitHub: [Gireeshsai12](https://github.com/Gireeshsai12)
- Repository: [marketplace](https://github.com/Gireeshsai12/marketplace)

Built as a portfolio project to demonstrate full-stack development, backend business workflows, relational data modeling, and containerized application delivery.
