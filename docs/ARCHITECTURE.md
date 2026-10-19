# Architecture

One Spring Boot application and one React SPA. PostgreSQL owns business data. Redis is used only for login throttling, never inventory. Nginx serves static assets and proxies API/uploads under the same origin. Session identity is server-managed in servlet sessions, passwords use BCrypt. Sessions are local to one Java instance.

## Source boundaries

- Identity / Access / Security: session identity, role and ownership gates, reset email, CSRF.
- Catalog: search, customer addresses/cart/wishlist/reviews.
- Pricing: pure decimal-safe totals and allocation.
- Commerce: transactional checkout, payment processing, reservation expiry, cancellation/refunds, outbox.
- Orders: customer order DTOs and callback simulation.
- Seller: seller-scoped catalog/inventory/fulfillment/analytics.
- Admin: allowlisted moderation, audit, inspection, promotion operations.
- Db: parameterized Spring JDBC access; all variable values are bound parameters. Dynamic table/sort names come from explicit allowlists.

This version uses explicit JDBC SQL rather than Spring Data JPA, making the row locks and PostgreSQL search queries directly reviewable. It is a compact modular monolith, not a strict layered/domain package implementation of every requested module. REST responses are selected maps/records and never ORM entities. React Query holds remote state; backend records remain authoritative. React Hook Form and Zod validate login/registration; other forms use HTML validation and server validation. Styling uses a custom responsive design system, not Tailwind.

## Checkout invariants

1. Lock the customer row, then variants in ascending id order.
2. Replay an existing `(user_id, idempotency_key)` order before reading the cart.
3. Load prices and availability from PostgreSQL; reject inactive listings.
4. Lock the promotion row and enforce global/per-customer limits.
5. Create parent order, seller portions, snapshots, allocations, reservations, and redemption in one transaction.
6. Update reserved quantities only where `stock-reserved >= requested`. Database CHECK constraints disallow negative stock or reserved > stock.
7. Successful demo payment commits held reservations once, deducts stock, and records ledger entries. Failures release reservations. Checkout and settlement commit together for immediate payments.
8. Clear the cart only when transaction succeeds. Rollback restores all prior cart/inventory data on failure.

## States

Payment: PENDING → PAID | FAILED | EXPIRED | CANCELLED. A successful callback after a terminal unpaid state or after the deadline enters REFUND_REQUIRED, without acquiring or consuming stock. Admin can complete a simulated late-payment refund → REFUNDED. A paid parent becomes REFUNDED when all portions are refunded/cancelled.

Fulfillment per seller: PROCESSING → SHIPPED → DELIVERED → RETURNED. Cancellation is allowed only when all portions remain PROCESSING; order locking serializes cancellation and fulfillment. Paid cancellation refunds/restocks exactly once through the unique seller-portion refund constraint. Returns are available for delivered portions within 30 days of order creation. Seller approval completes the demo refund and restock.

Reservations: HELD → COMMITTED or RELEASED. Every deadline is stored in UTC. Expiry runs every 15 seconds. Pending demo timeout orders become EXPIRED after ten minutes. Delay orders require an explicit callback from the order page; there is no automatic external provider.

## Amount allocation

USD NUMERIC(12,2) / Java BigDecimal, HALF_UP tax rounding, demo tax 6.25% of discounted merchandise. Standard shipping is $5.99 below $50 merchandise, otherwise free; express is $12.99. One coupon only. Parent discount, tax and shipping are allocated proportionally by item line amount, with the penny remainder assigned to the last line, so seller portions reconcile exactly.

## Asynchronous work

Business transactions create outbox records. A scheduled consumer uses `FOR UPDATE SKIP LOCKED` and unique outbox-to-notification identity to prevent duplicate in-app notifications. The admin can inspect outbox state. Email is currently implemented for password resets only. Important limitations of retry behavior are listed separately.
