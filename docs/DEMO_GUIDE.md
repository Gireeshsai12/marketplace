# Demo guide

1. Start the stack and open localhost:8088. Browse eight categories and 48 products. Search for `headphones`.
2. Open Studio Wireless Headphones (`/product/1`). Select Midnight or Sand and quantity. Price and available inventory come from the selected variant. Save it to the wishlist or add to cart.
3. Sign in as customer. Cart changes persist on the server. Proceed to checkout, choose the Lowell address (or add one), shipping, and coupon `WELCOME10` ($50 minimum, 10% capped at $50, one use per customer). `SAVE15` is $15 off $100+.
4. Select successful demo payment. Place the order, note its parent id and seller portions. Stock is reserved and committed inside database transactions. Click orders to return to history.
5. Sign out and sign in as seller. The dashboard shows actual revenue from your paid portions. Open Orders and click Ship (demo), then Mark delivered. Sign in as customer to see shipment status.
6. With delivered items, request a return from order details. Seller → Returns → Approve & refund (demo) completes a simulated refund and restock. Repeat calls do not refund twice.
7. Try a declined payment to see FAILED order state without stock loss. Try delay for a pending reservation, then complete callback on its order page. Timeout requires 10 minutes plus up to 15 seconds for the expiry job. A late successful callback can be exercised with API requests after expiration; it enters REFUND_REQUIRED and admin → Orders → Inspect can complete its demo refund.
8. Seller → Products: create listing, upload PNG/JPEG, add/edit variants, archive/activate. Inventory adjustments require a written reason. Inventory History shows sales, reservation/release, refund, and manual adjustments.
9. Admin login: suspend a non-admin customer or seller, moderate a listing/review, rename category, create/end promotion, inspect order, see audit and outbox records.
10. Forgot password sends a link to Mailpit at localhost:8025. Reset invalidates older session authentication versions.

The second seller owns a separate subset of products. `seller2@marketplace.local` provides an easy ownership-isolation demonstration. The core headphones listing belongs to the first seller. New stores start with honest zero sales, not invented analytics.
