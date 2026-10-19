# Database

Flyway `V1__schema.sql` is the authoritative schema. It creates users, stores, categories, products, variants, addresses, cart items, wishlists, promotions, orders, seller portions, item snapshots, reservations, stock ledger, payment events, coupon redemptions, returns, refunds, reviews, outbox, notifications, audit logs, and reset tokens.

Products have one primary image and variants have SKU/color/price/original price/stock/reserved/threshold. Multiple independent product images are not modeled in this release. Gallery detail controls enlarge the primary image. Seller variants share that image.

Useful constraints: unique email, SKU, checkout `(user_id,idempotency_key)`, payment event id, per-portion refund and return, one customer review per product, unique outbox notification. Monetary columns use NUMERIC; stock bounds are enforced with CHECK constraints. Foreign keys protect parent/child consistency. Indexes include PostgreSQL GIN full-text search and customer order history. Primary/unique keys support other reads. Small-demo analytics use aggregate joins; they do not query one item at a time per order.

UTC TIMESTAMPTZ is used for event dates; React displays viewer-local dates. Deterministic seeding runs inside a transaction only when no users exist and `DEMO_ENABLED=true`. On the first run it creates 4 users, 2 stores, 8 categories, 48 products, 96 variants, 1 customer address, and 2 promotions. Restarting does not overwrite edits or recreate removed data. Reset the volume to reseed.

Order snapshots include title, color, SKU, seller name, primary image, unit price, quantity, and allocated discount/tax/shipping. Later catalog edits do not change these rows. Parent shipping address is JSONB snapshot. Currency is recorded on the parent as USD.

Redis loss never changes inventory. Redis unavailability currently prevents login rather than silently removing the throttle. Session persistence is in Java memory, so restarting Java logs users out.
