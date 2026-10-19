# Deployment preparation

The Dockerfiles produce a static Nginx frontend and non-root Java runtime. Default Compose binds ports only to localhost. It is intended for development. No resources have been provisioned or deployed.

## Example AWS path

1. Build frontend/backend images and push to a private ECR repository. Choose pinned image digests in a real deployment.
2. Use ECS Fargate services behind an HTTPS Application Load Balancer. Route `/api/*` and `/uploads/*` to Java; other routes to the static frontend. Alternatively configure the frontend Nginx upstream to the Java service's private DNS.
3. Use RDS PostgreSQL in private subnets with restricted security groups and TLS JDBC settings. Give the application a least-privilege runtime account; consider a separate migration job/account. Flyway applies versioned schema migrations at startup.
4. Use managed Redis (ElastiCache) with network controls; this implementation's simple Redis host property is not a complete TLS/auth setup. Supply Spring's `SPRING_DATA_REDIS_PASSWORD`, `SPRING_DATA_REDIS_SSL_ENABLED` as appropriate and verify before release.
5. The upload adapter is filesystem-based. Use a persistent EFS mount at UPLOAD_DIR for container restarts. An S3 adapter is not implemented. Do not store uploads on ephemeral container disk.
6. Enable production profile, HTTPS Secure cookies, trusted proxy handling at ingress, Secrets Manager secrets, TLS-only database access, and constrained network egress. Do not enable permissive CORS. Keep frontend and API under one origin so cookie/CSRF behavior is consistent.
7. Replace the demo provider with a verified hosted payment adapter, production tax/shipping rules, production email delivery, account onboarding, and the production hardening listed in KNOWN_LIMITATIONS before accepting real money.

## Environment

DATABASE_URL is a JDBC URL, e.g. jdbc:postgresql://db.internal:5432/marketplace?sslmode=verify-full. DATABASE_USER / DATABASE_PASSWORD are separate secrets. REDIS_HOST controls the development rate-limit store; MAIL_HOST points at SMTP (Spring properties configure auth/TLS). UPLOAD_DIR is writable persistent storage. COOKIE_SECURE must be true behind HTTPS. DEMO_ENABLED must be false outside a private demo. Production profile defaults to these safe values and disables simulated checkout.

Start with 1 vCPU / 1–2GB RAM for Java and an appropriately sized database; these are planning estimates, not benchmark results. JVM memory is bounded by MaxRAMPercentage. Frontend assets should be cached by a CDN with versioned filenames. The chart bundle is relatively large; see limitations.

Log structured JSON via production profile; use a central log collector with secret redaction and retention policy. Observe `/actuator/health`, latency, error rate, outbox backlog, and database connections. Publicly expose only health summaries, not database/SMTP credentials. Restrict OpenAPI and development utilities at ingress in hosted deployments.

RDS automated backups, point-in-time recovery, encrypted snapshots, and tested restore procedures are necessary. Back up uploaded files separately and test restoration with a matching database snapshot. Use expand/contract schema changes for future rolling deployments.

Current sessions are in-memory: use one Java replica for the demo or implement Spring Session + Redis before horizontal scaling. Stock and outbox locking are database-based, but this does not make session identity distributed.
