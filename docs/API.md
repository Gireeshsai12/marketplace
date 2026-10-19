# API

Browse generated OpenAPI at `/swagger-ui/index.html` on the backend. All application endpoints have `/api` prefix. JSON errors use `{code,message}` and HTTP statuses 400/401/403/404/409/429/500. Responses include X-Correlation-ID. Unexpected SQL details are logged server-side and not returned to clients.

## Authentication

GET `/auth/csrf` returns token and sets XSRF-TOKEN cookie. POST/PUT/DELETE requests send `X-XSRF-TOKEN: <token>` alongside session cookies. POST `/auth/login` takes email/password. POST `/auth/register` takes email/password/name and creates a customer. GET `/auth/me`, POST `/auth/logout`, POST `/auth/forgot` (email), POST `/auth/reset` (token/password).

## Customer

GET `/categories`, `/stores`, `/products`, `/products/{id}`, `/suggestions?q=...`.
Product query fields: q, category, seller, min, max, rating, stock, sort (newest/relevance/price/price_desc/rating), page. Page size 12; metadata includes total/pages/page. Unsupported sort values use newest. Filters and search are shareable URL params in the UI.

GET/POST `/addresses`. GET/POST `/cart` (POST variantId/quantity). PUT `/cart/{variantId}` sets quantity, DELETE removes. Quantities 1–20. GET `/wishlist`, POST/DELETE `/wishlist/{productId}`. POST `/products/{id}/reviews` takes rating/title/body; requires a paid order record. DELETE `/reviews/{id}` removes own review. GET `/notifications`, POST `/notifications/read`.

POST `/checkout/quote`: `{addressId,shipping:"STANDARD"|"EXPRESS",coupon,mode}`. Quotes do not reserve or consume promotions.
POST `/checkout`: same JSON, with required `Idempotency-Key` header (8–100 characters). Replays return the original order even if the body changes; use a new key for a new purchase. Client prices are never accepted. Mode: success / decline / delay / timeout.

GET `/orders`, `/orders/{id}`; POST `/orders/{id}/cancel`; POST `/orders/{id}/demo-payment`: `{eventId,outcome:"success"|"failed"}`; POST `/orders/{id}/returns/{sellerPortionId}` with reason. Demo payment callbacks require the owning customer session and CSRF. This is an interactive test adapter, not a public payment webhook.

## Seller

`/seller/overview?days=7|30|90` (any bounded 1–365 accepted), `/seller/products`, `/seller/inventory`, `/seller/orders?q=&status=`, `/seller/returns`, `/seller/settings` are GET routes. POST and PUT `/seller/products[/{id}]` create/edit listing fields. Edit requires version. POST `/seller/products/{id}/variants` creates a variant; PUT `/seller/variants/{id}` edits price and attributes. Inventory changes use POST `/seller/inventory/{variantId}` with delta/reason. GET `/seller/inventory/{id}/history` lists ledger. POST `/seller/orders/{portionId}/fulfillment` with status/tracking. POST `/seller/returns/{id}/approve` completes refund/restock. POST `/seller/settings` with name. POST `/seller/uploads` multipart `file`, PNG/JPEG max 5MB / 16MP, returns a generated PNG URL. GET `/seller/export` returns formula-safe CSV.

## Admin

GET `/admin/{resource}` for users, sellers, categories, products, orders, returns, refunds, promotions, reviews, audit, outbox. GET `/admin/orders/{id}` inspects snapshots. POST `/admin/{users|sellers|products|reviews}/{id}/active` with active boolean. POST `/admin/categories` and PUT `/admin/categories/{id}` with name. POST `/admin/promotions` validates fixed/percentage amount, minimum, maxDiscount, starts/ends, usageLimit, perUser. POST `/admin/promotions/{code}/expire`. POST `/admin/orders/{id}/late-refund` completes a refund-required demo payment. Administrator accounts cannot be suspended through the user moderation route.

No API for upgrading a customer into a seller is exposed; demo sellers are seeded. No real payment credentials are accepted.
