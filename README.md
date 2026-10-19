# MARKETPLACE order-details fix

This update fixes two observed problems:

1. Shipping addresses were returned as a PostgreSQL driver JSON wrapper, so the customer page rendered blank address fields. Order details now decode the stored JSON snapshot into a plain address object. Existing orders use their existing saved snapshots; no database migration/reset is needed.
2. Cancel order was displayed for all paid orders, including shipped ones. The backend now includes can_cancel only when payment is PAID/PENDING and every seller portion is still PROCESSING. The page uses that capability and hides the action after shipment/delivery. The existing transaction-safe cancellation endpoint still validates the current state if shipment races with a customer click.

## Install on your current Windows project

1. Extract this ZIP to `E:\marketplace-order-fix`. That folder must contain Apply-MarketplaceFix.ps1, manifest.json, and files/.
2. Run in PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File E:\marketplace-order-fix\Apply-MarketplaceFix.ps1 -ProjectPath E:\marketplace
cd E:\marketplace
docker compose up -d --build backend frontend
docker compose ps
```

Wait for backend/frontend health checks, then open http://localhost:8090 and press Ctrl+F5. Sign in as customer, open existing Order #1, confirm the address is visible and Cancel order is absent for the shipped order. A new paid PROCESSING order should still allow cancellation.

The update preserves your existing docker-compose.yml port fixes, .env, database, uploads, and orders. It does not stop/remove database services or volumes. Source files are backed up in a sibling `marketplace-fix-backups` folder. The script checks every old and new SHA-256 hash before making changes and stops if it detects unexpected local code edits. It skips files that already have the fix. A failed source copy restores files already copied in that attempt.

The PowerShell installer was reviewed and its package hashes verified here; Windows PowerShell execution itself was not available in this Linux environment. You can alternatively back up your project and copy the contents of files/ over matching source paths, then rebuild only backend/frontend. Never replace docker-compose.yml or use `docker compose down -v` to install this fix.

Publishing is a separate next step. This package does not deploy the application or create paid resources.
