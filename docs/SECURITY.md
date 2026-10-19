# Security

Sessions: server-managed servlet session cookie, HttpOnly, SameSite=Lax, 30-minute expiry. Login invalidates the prior session. Logout invalidates it. Password reset increments the account authentication version; existing sessions fail on their next authenticated API call. Secure cookie is configurable and must be true under HTTPS.

CSRF: Spring Security token repository with XSRF-TOKEN cookie (intentionally readable by the frontend), matching X-XSRF-TOKEN request header, and explicit request handler. Only GET is used for read-only operations. No broad CORS allowance; browser and API share the frontend proxy origin. SameSite alone is not the CSRF defense.

Access: each protected operation reloads the account and checks active status, role, and ownership. Customer order reads use id + user_id. Seller operations use session user's active store id and owned resource predicates. Frontend guards merely assist navigation. Admin operations use an allowlist and write audit records. No endpoint supports arbitrary table names or role escalation.

Passwords: BCrypt, minimum 10 / maximum 100 input characters. Redis limits login to 20 attempts per minute per backend remote address + account email. Deploy a trusted ingress with additional IP-level throttling; behind a shared proxy the application remote address is shared. Account reset replies do not disclose whether an email exists. Tokens are random UUIDs, one-use, and expire in 20 minutes. Tokens are currently stored as plaintext in the development database; production should hash them and throttle reset requests.

Uploads: only JPEG/PNG signatures, image reader validation before pixel decode, max 5MB and 16 megapixels, server UUID file names, normalized fixed storage directory, re-encoded PNG to strip uploaded metadata. No user-specified path or SVG upload is accepted. React renders user content as text. Nginx sends a restrictive content policy and nosniff. No card data is collected or stored.

Production profile: set SPRING_PROFILES_ACTIVE=prod, DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, REDIS_HOST, MAIL_HOST, UPLOAD_DIR. `DEMO_ENABLED=false` disables seed accounts and the simulated checkout API. This release has no production payment adapter, so a production commerce launch requires further implementation. Existing development accounts remain if an old database is reused; use a clean deployment database. See DEPLOYMENT.md.

Limitations: in-memory sessions across one instance, no MFA, no email address verification, no external security assessment, no real tax/carrier/payment integration. Do not expose the development Compose stack publicly as-is.
