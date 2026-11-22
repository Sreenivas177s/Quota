# Resources microservice: hands-on Java course plan

**Status:** Revised master plan; Resources microservice sequence.
**Planning date:** 2026-11-22.
**Goal:** Learn to independently design, build, test, debug, and operate a Java Spring Boot Resources microservice, then integrate it with another Java service and a React frontend.
**Architecture:** The Resources service owns the resource catalog and its database and is independently runnable and deployable. A small companion Java service later consumes its versioned API. React is added after the backend service boundaries and contracts are understood. Avoid distributed infrastructure until an exercise has a concrete need for it.
**Execution:** Complete and test the Resources Spring server before beginning Phase 2 frontend work. Java microservice integration follows the frontend phase. The learner writes the application by hand. Future assistants generate individual chapter guides, provide graduated hints, and review attempts; they must not implement the application or reveal solutions unless explicitly asked.
**Specification:** This file contains the course scope, application policies, authoritative chapter sequence, and chapter-generation contract. No earlier conversation is required.

## 1. Learning agreement

- Assume basic React, Java, and Spring familiarity, but verify knowledge through small working tasks.
- Plan around approximately one hour every two days, without a deadline or fixed week count.
- Make sessions approximately 45-60 minutes, allowing difficult tasks to span sessions.
- Finish familiar foundations quickly through demonstration-based fast-track gates.
- Keep related concepts together in manageable chapters rather than scattering isolated framework features.
- Build one product system across independently deployable services; keep each service's code, data, and runtime boundary clear rather than creating unrelated mini-projects.
- Keep the first phase backend-only: exercise endpoints with backend tests and an HTTP client, not a React app. Begin frontend work only after the Resources service phase gate is met.
- Teach microservice boundaries through independent ownership, deployment, API contracts, configuration, and failure handling. Do not introduce a shared database, generic platform framework, or infrastructure for its own sake.
- Include writing tests in every chapter, starting with the first vertical slice.
- Teach both failure investigation and failure prevention. Do not equate adding a defensive check with understanding a root cause.
- Keep hints, solution sketches, and reference implementations separate from exercise instructions.
- Do not claim that finishing the course alone establishes mastery; use independent explanations and changed-requirement exercises to assess transfer.

### Session loop

1. State the observable requirement and predict edge cases.
2. Choose a small behavioral test; run it failing for the intended reason when practical.
3. Implement the smallest change and rerun that test.
4. Inspect the running application with the appropriate debugging tools.
5. Reproduce the assigned failure; collect evidence before changing code.
6. Fix the cause and add a regression test that fails without the fix.
7. Refactor with passing checks, then record a checkpoint.

Scaffolding and exploratory tasks do not need artificial failing tests. Resume test-first work when the behavior is clear. Never accept a missing dependency or broken test setup as evidence of a behavioral regression.

### Chapter guide format

Every generated chapter must include: prerequisites; starting state; user stories; acceptance criteria; grouped concepts; ordered sessions; tests the learner must write; a debugging lab; a completion gate; a fast-track gate where applicable; optional extensions; and a handoff checkpoint.

Each session must state a concrete deliverable, an approximate time budget, test scenarios, a debugging observation, and a stopping point. Split oversized work rather than forcing it into one hour. No complete implementation or completed test bodies by default.

## 2. Resources service specification

### Service boundary and product

Build an independently runnable Spring Boot microservice that owns the catalog and lifecycle of reservable resources such as rooms, desks, and equipment. The service is responsible for resource data and its API; it does not own reservations, members, booking rules, or booking workflows.

- Resource managers create, inspect, update, and archive resources through the service API.
- API consumers list and inspect resources. The React application and the later Java consumer use the API, never the Resources database.
- A companion `booking-service` may be introduced as a separate learning service. It owns any reservation data it adds and treats Resources as a remote dependency.
- Authentication may begin with a clearly local development setup, but production authorization must be enforced by the service, not by hidden UI controls.

### Resource policies

- A resource has a stable UUID, name, type, IANA `ZoneId`, organization identifier, and lifecycle status.
- Validate names, types, zones, and organization scope at the service boundary. Return stable machine-readable errors without stack traces.
- Archive resources rather than hard-delete them; historical or external references must remain meaningful.
- List/search APIs use deterministic sorting and bounded pagination. Organization-owned reads and writes are always scoped to the authenticated organization once identity is introduced.
- The Resources service is the source of truth for resource data. No other service may read or write its tables directly.
- Version the HTTP API and document it with an OpenAPI contract. Evolve contracts compatibly where practical; make breaking changes explicit.
- Keep service-to-service calls bounded by timeouts and handle remote failure explicitly. Retry only operations whose semantics make retry safe.

### Microservice learning goals

Practice a meaningful service boundary, independent startup and deployment, service-owned persistence, API and error contracts, externalized configuration, health/readiness, logs/metrics, containerized local composition, and consumer-driven integration tests. Service discovery, Kubernetes, a message broker, and an API gateway are outside the core course unless a later requirement justifies them.

Booking rules and workflows from the previous course plan are not part of the Resources service. They remain optional material for a separate booking-service extension and must not be used to expand the Resources service boundary.

## 3. Stack and repository conventions

### Version policy

This plan does not assert an unverified latest version. The current backend POM targets Java 25 and Spring Boot 4.1.2; treat the POM and generated Maven Wrapper as the current executable baseline, and reconcile `docs/versions.md` with them before generating the first guide. If the project intentionally changes versions, verify official compatibility and record the decision. Use stable releases, not previews, snapshots, or release candidates.

- Java: the version selected in `backend/pom.xml` (currently 25), without preview features.
- Backend: the Spring Boot version selected in `backend/pom.xml` (currently 4.1.2), Spring MVC, Maven Wrapper, Bean Validation, Spring Data JPA, Spring Security, and Actuator as introduced by the relevant chapter.
- Service integration: a maintained Spring HTTP client and test server for remote API tests; add resilience tooling only when its behavior is taught and tested.
- Frontend: current stable React and TypeScript, Vite, and a supported Node.js LTS compatible with the selected tools.
- Database: a supported PostgreSQL release, with Flyway migrations.
- Frontend tests: Vitest, React Testing Library, user-event, and Mock Service Worker when network mocking becomes useful.
- Backend tests: JUnit Jupiter; Mockito for useful isolated boundaries; Spring test support; PostgreSQL Testcontainers for the Resources-owned database and contract/integration tests for remote consumers.
- Browser tests: Playwright. Later load experiments may use k6 or another documented maintained tool.
- API contract: OpenAPI, with generated client types only if they fit the existing build and are verified against the actual API.

Use official documentation for verification. Pin reproducible versions in manifests and lockfiles. Do not blindly upgrade mid-chapter; treat upgrades as a documented maintenance exercise.

### Repository and service layout

The current `backend/` directory is the Resources microservice; `frontend/` is its eventual React client. Keep these current paths unless a concrete build or deployment need justifies a move. A future Java consumer is a separate application with its own build, configuration, tests, and persistence:

```text
backend/                         Resources microservice, Maven Wrapper, independent lifecycle
backend/src/main/java/com/quota/backend/   Resource API, domain, persistence, and configuration
backend/src/main/resources/      Service configuration and Flyway migrations
backend/src/test/java/           Unit, web, database, security, and contract tests
frontend/                        React client added after the backend phase
services/booking-service/        Optional later Java consumer; separate database and lifecycle
e2e/                              Cross-service and browser workflows when introduced
docs/chapters/                    Generated chapter guides
docs/decisions/                   Service-boundary and API decisions
docs/debugging/                   Learner investigation notes
docs/versions.md                  Verified versions and prerequisites
docs/progress.md                  Current chapter, checks, decisions, and next task
```

Keep the Resources service internally cohesive with focused resource, API, persistence, and configuration packages. Separate HTTP DTOs from persistence entities. The future consumer must call the Resources API and must not import its persistence entities or access its database. Avoid generic base controllers, speculative shared libraries, and unnecessary service splits.

### HTTP conventions

- Version Resources endpoints under `/api/v1`; preserve the existing `GET /api/v1/resources` and `GET /api/v1/resources/{id}` routes.
- Add create, update, and archive operations in the chapter that introduces resource lifecycle. Use UUID identifiers and explicit request/response DTOs.
- Use 400 for malformed/invalid input, 401 for missing authentication, 403 for denied actions, 404 for absent or inaccessible resources, and 409 for state/version conflicts.
- Define consistent Problem Details errors with stable machine-readable codes and field errors. Do not expose stack traces.
- Define pagination envelopes and deterministic sorting when resource querying is introduced; cap page size at 100 and default to 20.
- Publish and test an OpenAPI contract. The companion service consumes this contract over HTTP; never share database tables or assume in-process calls.
- Record endpoint, compatibility, timeout, and error-handling decisions in each chapter handoff so future consumers do not reinvent contracts.

### Test and debugging rules

- Test service behavior and resource invariants, not implementation details or snapshot volume.
- Keep domain tests fast; use PostgreSQL, not an in-memory substitute, for persistence and database constraints.
- Test HTTP contracts at the service boundary. For consumers, use a controlled HTTP test server or contract test rather than reaching into the Resources service database.
- Test timeouts, unavailable dependencies, malformed responses, and safe retry behavior in the chapter that introduces remote calls.
- Test authorization through direct service HTTP requests, not only hidden UI controls.
- Reproduce asynchronous frontend failures with controlled response ordering after the frontend phase begins.
- Never introduce intentionally vulnerable faults into a deployed environment. Use a local scratch change, reproduce, repair, and leave the baseline passing.
- Document focused commands when each tool is installed; do not invent commands or claim unrun checks passed.
- Do not require a fixed coverage percentage. Require demonstrated detection of meaningful defects.

## 4. Chapter roadmap

Chapters are independently generatable, not independent implementations. This sequence is authoritative. Chapters 01-05 complete the Resources Spring server. Chapters 06-08 are Phase 2: return to the React frontend after the server completion gate. Chapters 09-10 add the separate Java consumer and multi-service operations. Chapter 11 verifies and releases the complete system. Earlier booking-platform briefs below are legacy topic material only and do not define this course's scope or order. The existing Chapter 01 guide predates this revision and must be regenerated; inspect the current repository and reuse existing work rather than assuming it is empty.

### Phase 1: Complete the Resources Spring server

Use backend tests and direct HTTP requests. The existing frontend may remain in the repository, but return to frontend feature work only after Chapter 05's server completion gate.

#### Chapter 01: Resources service foundation

**Outcome:** Understand the service boundary and verify the existing Spring Boot service's list/detail API, startup, and tests.
**Focus:** Java/Spring project structure, dependency injection, controller-service-repository flow, API versioning, DTOs, HTTP status, deterministic behavior, unit versus web tests.
**Gate:** Explain what Resources owns, run the service and focused tests, and verify `GET /api/v1/resources` and `GET /api/v1/resources/{id}` directly. Record actual project versions and commands.

#### Chapter 02: Resource lifecycle and API contracts

**Outcome:** Create, update, validate, and archive resources through a stable versioned API.
**Focus:** REST semantics, request validation, domain invariants, Problem Details, OpenAPI, compatibility, error handling, API tests.
**Gate:** Valid and invalid requests have documented outcomes; archived resources remain identifiable; the OpenAPI contract matches the tested HTTP behavior.

#### Chapter 03: Service-owned persistence

**Outcome:** The Resources service owns durable resource data in PostgreSQL and can recreate its schema from migrations.
**Focus:** JPA, repository boundaries, Flyway, constraints, configuration, Testcontainers, migration and restart behavior.
**Gate:** PostgreSQL-backed tests prove persistence and constraints; a clean database is reconstructed using migrations; no other application accesses these tables.

#### Chapter 04: Resource search and API evolution

**Outcome:** Consumers can filter and page through resources with predictable results as the API evolves.
**Focus:** Search criteria, deterministic sorting, bounded pagination, query plans, compatible DTO changes, generated client types where appropriate.
**Gate:** Tests cover combinations, empty results, stable ordering, page boundaries, and contract compatibility; query improvements are based on evidence.

#### Chapter 05: Service security and organization scope

**Outcome:** Resource reads and mutations are authenticated and scoped to the caller's organization.
**Focus:** Spring Security resource-server patterns, JWT validation or an explicitly documented local identity, role checks, tenant-scoped queries, secrets/configuration, direct HTTP security tests.
**Gate:** Unauthorized and cross-organization requests fail through the service API; no client-supplied organization or hidden UI control is trusted as authorization.

### Phase 2: Return to the React frontend

Begin after Chapter 05 passes the Resources Spring server completion gate. The frontend consumes the public Resources API; it does not import Java models or access service databases. Keep API correctness covered by backend tests.

#### Chapter 06: Resource catalog client

**Outcome:** The React/TypeScript client lists and inspects resources from the running Resources service.
**Focus:** API client configuration, Vite proxy, generated or handwritten DTO types, loading/empty/error states, accessible rendering, component tests.
**Gate:** UI content comes from the API; tests distinguish HTTP, network, and invalid-payload failures; browser DevTools confirms the real request path.

#### Chapter 07: Resource management workflows

**Outcome:** Authorized users create, edit, search, and archive resources with clear validation and conflict feedback.
**Focus:** Forms, routing, server-state caching, mutation invalidation, authentication/session UX, accessible interactions, preserving form state after failures.
**Gate:** Frontend tests verify meaningful user behavior, and server-side validation and authorization remain effective when requests bypass the UI.

#### Chapter 08: Frontend and API verification

**Outcome:** Browser tests verify the React client against the completed Resources service.
**Focus:** Playwright, test data, API contract checks, controlled backend failures, responsive and keyboard workflows.
**Gate:** Critical resource workflows pass against the real HTTP boundary; tests do not depend on backend implementation details or arbitrary sleeps.

### Phase 3: Java microservices

After returning to the UI, add a separate Java consumer to practice service-to-service communication and independent operation. The consumer must use the Resources API and must never access its database.

#### Chapter 09: A separate Java API consumer

**Outcome:** A small companion Spring Boot service consumes Resources over HTTP while remaining independently buildable and runnable.
**Focus:** Separate service ownership and persistence, OpenAPI/client contract, Spring HTTP client, request deadlines, mapping remote errors, timeout and unavailable-service tests, safe retry/idempotency decisions.
**Gate:** The consumer has no Resources database dependency; controlled tests prove behavior for successful, missing, invalid, slow, and unavailable Resources responses. Keep the companion domain small and do not move resource ownership out of Resources.

#### Chapter 10: Operating the services

**Outcome:** Run the independent Java services and their databases locally and inspect their health and behavior.
**Focus:** Externalized configuration, profiles, Actuator health/readiness, structured logs, correlation IDs, basic metrics, Docker images, Docker Compose, startup/shutdown, CI boundaries.
**Gate:** Each service can be built and started independently, then run together from documented local configuration; health and dependency failures are observable without leaking secrets.

#### Chapter 11: Production and portfolio release

**Outcome:** Publish a reproducible, secured demonstration of the independently deployable services and client.
**Focus:** CI, configuration and secrets, migrations, container deployment, health checks, logging/metrics, performance investigation, backup/recovery, architecture documentation.
**Gate:** Demonstrate clean builds, API and browser checks, service-failure behavior, documented deployment/recovery, and measured results. Report only checks actually run.

### Legacy detailed chapter briefs

The chapter descriptions below preserve useful exercises, test scenarios, debugging labs, and domain details from the previous outline. They are not the current sequence and their chapter numbers, prerequisites, and milestone groupings are superseded by the phase roadmap above. Use them as source material when generating the newly numbered guides; adjust dependencies so backend chapters remain ahead of all frontend implementation.

### Chapter 01: First working vertical slice

**Prerequisites:** Basic Java, JavaScript, terminal use, and HTTP vocabulary. No existing application required.
**Starting state:** Empty project; approved plan only.
**Outcome:** A React page displays resources returned by a running Spring endpoint.
**Concepts:** Toolchain, React rendering and props, TypeScript DTOs, Spring request mapping, JSON, HTTP status, debugger attachment.

- [ ] Session 1: Verify versions, initialize the two projects yourself, record startup/test commands, and run their baseline checks.
- [ ] Session 2: Implement an in-memory resource endpoint returning stable IDs, names, types, and zones; write a web-layer response test.
- [ ] Session 3: Fetch and display that endpoint in React with explicit loading, empty, and error states; write a component test.
- [ ] Session 4: Trace one request from browser to controller and back; diagnose a wrong URL, unavailable backend, and incorrect response shape.

**Tests to write:** Endpoint JSON contract; multiple resources; empty response; frontend success and failure rendering. Mock the network boundary, not React internals.
**Debugging evidence:** Capture URL, status, response body, and values at one frontend and one backend breakpoint. Distinguish an HTTP failure from a browser cross-origin failure.
**Gate / fast track:** Start both apps from documented commands; show passing tests; explain the complete request path without copying an implementation.
**Handoff:** Exact versions, resource response fields, development proxy/origin setup, and executable commands.
**Extension:** An environment-specific API address without hardcoded production URLs.

### Chapter 02: Interactive resource catalog

**Prerequisites:** Chapter 01.
**Starting state:** Read-only in-memory catalog with basic rendering tests.
**Outcome:** Users search, filter by resource type, and sort a usable catalog.
**Concepts:** Composition, controlled inputs, immutable updates, event handlers, state ownership, derived data, stable keys, accessible labels.

- [ ] Session 1: Split meaningful components and add controlled search and resource-type filters.
- [ ] Session 2: Add deterministic sorting and a clear-filters action without redundant synchronized state.
- [ ] Session 3: Write interaction tests, then investigate mutation and index-key faults using React DevTools.

**Tests to write:** Combined filters; no matches versus empty catalog; clearing filters; stable selection after reorder; keyboard-accessible controls.
**Debugging lab:** A selected item changes after sorting, or the UI fails to update after an in-place mutation. Prove why before repairing it.
**Gate / fast track:** Implement an additional filter independently and explain why computed results usually do not belong in an effect.
**Handoff:** Component boundaries, filter semantics, and which state is local versus shared.
**Extension:** Persist selected filters in URL parameters with back/forward navigation tests.

### Chapter 03: Persistence and resource querying

**Prerequisites:** Chapter 02; local container runtime available for PostgreSQL and Testcontainers.
**Starting state:** Resource endpoint still uses in-memory data.
**Outcome:** Durable resources, reproducible migrations, and server-side paginated queries.
**Concepts:** JPA mappings, repositories, DTO mapping, migrations, constraints, pagination, sorting, basic SQL inspection.

- [ ] Session 1: Design the initial resource schema and Flyway migrations; replace the in-memory store.
- [ ] Session 2: Add reproducible development seed data and PostgreSQL repository integration tests.
- [ ] Session 3: Move filtering/sorting/pagination to the API, preserving Chapter 02 behavior and resetting page selection when filters change.
- [ ] Session 4: Investigate an invalid mapping, migration failure, and duplicate or missing results from unstable pagination order.

**Tests to write:** Empty database; persistence after restart; type/search combinations; page boundaries; maximum page size; stable order with duplicate names; migration from an empty database.
**Debugging lab:** Inspect actual SQL and database state instead of assuming a repository method did what its name suggests.
**Gate / fast track:** Recreate the database from migrations and pass real-PostgreSQL tests without manual schema edits.
**Handoff:** Schema, migration policy, pagination envelope, sort defaults, and seed instructions.
**Extension:** Compare query plans before and after a justified resource-search index.

### Chapter 04: Booking forms and API contracts

**Prerequisites:** Chapter 03.
**Starting state:** Persistent catalog; no booking workflow.
**Outcome:** Navigate resource details, create a basic reservation, and view personal bookings using a local development identity.
**Concepts:** Routing, form state, Bean Validation, records/DTOs, exception handling, Problem Details, POST responses, ownership boundaries.

- [ ] Session 1: Specify the booking request/response and error contracts; add resource detail and booking routes.
- [ ] Session 2: Implement the booking form and accessible field/server error display.
- [ ] Session 3: Persist simple bookings with structural validation and retrieve them for the development identity.
- [ ] Session 4: Diagnose malformed timestamps, rejected validation, and an error response the frontend incorrectly treats as success.

**Tests to write:** Successful creation; required fields; invalid UUID/time format; end before start; unknown resource; request-supplied owner ignored or rejected; useful frontend errors.
**Debugging lab:** Client validation passes but the server rejects the request. Compare the actual payload and server contract.
**Gate / fast track:** Bypass the browser and prove that server validation still protects the API.
**Handoff:** Booking DTOs, status model, error codes, routes, and development-identity mechanism.
**Extension:** Preserve an unsent form when a recoverable server request fails.

### Chapter 05: Reliable frontend server state

**Prerequisites:** Chapter 04.
**Starting state:** Catalog and booking forms use basic request handling.
**Outcome:** Predictable loading, refreshing, retries, cancellation, and cache invalidation.
**Concepts:** Effect dependencies and cleanup, stale closures, request races, server versus client state, query keys, mutation invalidation, URL state.

- [ ] Session 1: Reproduce a race where an older resource response arrives after a newer one; repair the manual request path.
- [ ] Session 2: Introduce a maintained server-state library such as TanStack Query after documenting the problems it solves.
- [ ] Session 3: Define query keys and invalidation for resources and personal bookings; retain errors and retry behavior appropriate to each operation.
- [ ] Session 4: Use network mocking to test delayed, failed, cancelled, and out-of-order requests.

**Tests to write:** Rapid filter changes; unmount during fetch; stale response cannot overwrite current selection; successful booking refreshes relevant data; failed mutation retains form values.
**Debugging lab:** A cache key omits a filter or organization, returning believable but incorrect data. Add organization scoping when identity lands in Chapter 08.
**Gate:** Explain which state belongs in the URL, component, form, or query cache. Do not use blanket effects or premature memoization to mask incorrect ownership.
**Handoff:** Query-key conventions, retry policy, invalidation dependencies, and network-mocking setup.
**Extension:** Optimistic updates for a reversible resource edit, not speculative confirmation of a contested booking.

### Chapter 06: Booking rules and time

**Prerequisites:** Chapter 05.
**Starting state:** Structurally valid bookings exist, but business rules are incomplete.
**Outcome:** A tested domain model enforces duration, opening hours, overlap, and cancellation rules.
**Concepts:** Domain services, Java time APIs, Clock injection, half-open intervals, parameterized tests, state transitions, error translation.

- [ ] Session 1: Write a booking-rule decision table, including adjacency and all overlap shapes.
- [ ] Session 2: Implement minimum/maximum duration, future-start, resource-zone opening hours, and an availability endpoint.
- [ ] Session 3: Implement cancellation and personal booking history without deleting records.
- [ ] Session 4: Debug a time-zone assumption and a boundary error with deterministic fixtures.

**Tests to write:** Identical/contained/containing/partial overlap; adjacent slots; exact duration limits; past start; opening and closing boundaries; weekends; cancellation before/after start; cancelled booking releases a slot.
**Debugging lab:** Code works on the developer's machine but fails under a different JVM/browser time zone. Eliminate accidental dependence on system defaults.
**Gate:** Explain why instants and local scheduling rules are different. Demonstrate rule tests without wall-clock sleeps.
**Handoff:** Rule matrix, clock strategy, resource zones, availability contract, and cancellation semantics.
**Extension:** Configurable per-resource opening hours with interval validation.

### Chapter 07: Transactions and concurrent reservations

**Prerequisites:** Chapter 06.
**Starting state:** Sequential booking checks work but may race.
**Outcome:** Concurrent booking cannot violate the overlap invariant, including requests that bypass the UI.
**Concepts:** Transaction boundaries, isolation, check-then-act races, PostgreSQL range/exclusion constraints, locking tradeoffs, optimistic versions, idempotency.

- [ ] Session 1: Write a coordinated competing-request test and demonstrate that application-only overlap checks can fail.
- [ ] Session 2: Add database-enforced overlap protection and translate constraint failures into stable 409 responses.
- [ ] Session 3: Test rollback and introduce optimistic versioning for resource edits as a separate concurrency problem.
- [ ] Session 4: Specify and implement client-generated idempotency keys for booking creation, including payload mismatch and replay behavior.

**Tests to write:** Two conflicting requests leave exactly one active reservation; adjacent requests both succeed; different resources do not block each other; failed transaction leaves no partial reservation; duplicate key replays the original result; same key/different payload conflicts.
**Debugging lab:** A transaction annotation is ineffective because the call path bypasses the Spring proxy. Observe actual behavior rather than annotation presence.
**Gate:** Demonstrate the race before and after repair against PostgreSQL. Explain why synchronized, disabled buttons, and optimistic entity versions alone do not prevent competing new rows.
**Handoff:** Database constraints, required extensions, conflict mapping, transaction boundaries, and idempotency contract. Scope keys to the authenticated organization/member in Chapter 08.
**Extension:** Compare a justified pessimistic locking approach with exclusion constraints under controlled contention.

### Chapter 08: Identity, roles, and organization isolation

**Prerequisites:** Chapter 07.
**Starting state:** Strong booking integrity with a development identity; local use only.
**Outcome:** Authenticated, tenant-scoped workflows with enforced member, manager, and administrator permissions.
**Concepts:** Spring Security filter chain, sessions, password hashing, CSRF, secure cookies, membership authorization, scoped repositories, security tests.

- [ ] Session 1: Define the permission matrix and replace the development identity with authenticated sessions. Start with seeded accounts, not public signup.
- [ ] Session 2: Add organization membership, ownership, schema backfill, and organization selection; scope every existing endpoint and idempotency key.
- [ ] Session 3: Implement browser login/logout, CSRF handling, session-expiry UX, and query-cache clearing on logout or organization switch.
- [ ] Session 4: Attack authorization boundaries through direct requests, including guessed booking/resource IDs and privileged payload fields.
- [ ] Session 5: Add administrator-controlled memberships and document deployment cookie/origin assumptions.

**Tests to write:** Missing authentication; missing/invalid CSRF token; owner versus other member cancellation; denied role escalation; cross-tenant read/write/search; logout; organization switch with cached data; removed membership denies access.
**Debugging lab:** A hidden manager button looks secure, but the endpoint is callable by a member. Repair server authorization and prove it with tests.
**Gate:** Pass a complete permission matrix through HTTP tests; no development identity or client-trusted ownership remains in production paths.
**Handoff:** Authentication flow, cookies/origins, CSRF strategy, role matrix, tenant-scoping conventions, and test identities.
**Extension:** Evaluate external OIDC login without building a custom token protocol.

### Chapter 09: Availability calendar and accessible interaction

**Prerequisites:** Chapter 08.
**Starting state:** Authenticated list/detail booking flow with tested instant-based rules.
**Outcome:** Responsive calendar discovery and booking across viewer and resource zones.
**Concepts:** Maintained calendar/date libraries, local/instant conversion, keyboard interaction, focus management, semantic forms, responsive layouts, Playwright.

- [ ] Session 1: Evaluate a maintained calendar library for licensing, accessibility, time-zone support, and React compatibility; record the choice.
- [ ] Session 2: Add day/week navigation and explicit viewer/resource zone labeling backed by availability data.
- [ ] Session 3: Connect slot selection to the existing booking form; provide equivalent keyboard and non-calendar navigation.
- [ ] Session 4: Add browser tests and investigate a shifted booking or focus-loss defect.

**Tests to write:** Same instant in two zones; date boundary crossing; DST transition rendering; keyboard booking; focus after dialog close; mobile form usability; server conflict displayed without losing context.
**Debugging lab:** A date-only value is parsed as an instant and moves the selected day. Trace conversions at each boundary.
**Gate:** Complete a booking without a mouse and verify narrow/mobile and desktop layouts without clipped controls or overlapping text.
**Handoff:** Calendar library, zone-display policy, conversion boundaries, and browser-test commands.
**Extension:** Calendar export with a maintained iCalendar library and tenant/ownership checks.

### Chapter 10: Administration, approvals, and audit history

**Prerequisites:** Chapter 09.
**Starting state:** Members can book ordinary resources; role boundaries exist.
**Outcome:** Managers archive resources, create closures, and decide restricted-resource requests with auditability.
**Concepts:** Explicit workflow states, atomic transitions, maintenance capacity, optimistic UI conflicts, append-only audit events, administration UX.

- [ ] Session 1: Implement resource management and archival while preserving history and preventing new bookings on archived resources.
- [ ] Session 2: Add maintenance windows using the same database-protected capacity model as bookings, not an uncoordinated second overlap check.
- [ ] Session 3: Add pending/approved/rejected workflows; pending requests do not hold capacity and approval rechecks atomically.
- [ ] Session 4: Record and display transactionally consistent audit events; redact sensitive fields.
- [ ] Session 5: Exercise concurrent decisions and closure-versus-booking races through HTTP tests.

**Tests to write:** Member cannot administer; closure conflicts with confirmed booking; booking conflicts with closure; pending requests coexist; exactly one conflicting approval succeeds; duplicate decisions are safe; failed mutations leave no success audit entry.
**Debugging lab:** Audit says approved even though the booking transaction rolled back. Repair the consistency boundary.
**Gate:** Demonstrate approval and closure races without violating capacity, plus attributable audit history.
**Handoff:** Capacity representation, state-transition table, admin contracts, and audit fields.
**Extension:** Manager override as a distinct, reason-required audited workflow; never silently bypass constraints.

### Chapter 11: Recurring reservations

**Prerequisites:** Chapter 10.
**Starting state:** Individual bookings, constraints, and calendar are reliable.
**Outcome:** Weekly series with explicit zone semantics and individual occurrence exceptions.
**Concepts:** Local recurrence versus duration arithmetic, maintained recurrence libraries, bounded expansion, atomic bulk changes, series/occurrence identity.

- [ ] Session 1: Specify weekly recurrence inputs and distinguish local wall time from UTC repetition.
- [ ] Session 2: Evaluate a maintained recurrence library and generate at most 12 occurrences; reject ambiguous/nonexistent local times explicitly.
- [ ] Session 3: Validate all occurrences and create the series atomically through the existing capacity protection.
- [ ] Session 4: Cancel one occurrence or all future occurrences without rewriting completed history.
- [ ] Session 5: Present structured conflicts and test concurrent series creation.

**Tests to write:** Stable local hour across DST; missing/repeated local hour rejection; one conflicting occurrence rolls back all new occurrences; maximum bound; single cancellation preserves neighbors; future cancellation preserves past occurrences; concurrent series cannot overbook.
**Debugging lab:** Adding seven 24-hour durations changes the local start time after a DST boundary. Explain and repair the semantic error.
**Gate:** Show the same series in two viewer zones and explain persisted series rules versus occurrence instants.
**Handoff:** Library/version, recurrence schema, conflict payload, limits, and edit/cancel policy.
**Extension:** Partial acceptance or edit-future-series with an explicit replacement policy and new tests.

### Chapter 12: Waitlists and live collaboration

**Prerequisites:** Chapter 11.
**Starting state:** Capacity rules cover bookings, closures, and series.
**Outcome:** Fair exact-slot waitlists, expiring offers, and live availability refresh.
**Concepts:** Queue ordering, leases, atomic claims, background expiry, SSE, reconnection, authorized subscriptions, cache refresh.

- [ ] Session 1: Implement exact-slot waitlist join/leave for ordinary resources; reject duplicate active entries.
- [ ] Session 2: Promote the earliest eligible waiter to a five-minute capacity-holding offer inside a transaction.
- [ ] Session 3: Implement offer acceptance and deterministic expiry; use the existing capacity invariant for offers too.
- [ ] Session 4: Add authenticated organization/resource-scoped server-sent events that trigger authoritative refetching.
- [ ] Session 5: Handle duplicate events, reconnects, and organization switches; refetch on reconnection rather than assuming event delivery was complete.

**Tests to write:** Stable fairness; cancellation promotes once; two workers cannot create two active offers for one slot; acceptance-versus-expiry race; expiry promotes next eligible member; direct bookings cannot bypass an active offer; unauthorized event stream denied; reconnect refreshes stale data.
**Debugging lab:** Two workers promote the same waiter, or a reconnect leaves stale availability. Use transaction/event evidence to distinguish the causes.
**Gate:** Demonstrate two browser sessions and coordinated backend workers without duplicate allocation. Jobs remain single-instance development infrastructure until Chapter 13 hardens delivery.
**Handoff:** Offer lifecycle, clock/expiry mechanism, capacity migration, event schema, subscription authorization, and reconnect behavior.
**Extension:** Evaluate WebSockets only for a genuinely bidirectional requirement.

### Chapter 13: Reliable background jobs and notifications

**Prerequisites:** Chapter 12.
**Starting state:** Background expiry and live invalidation exist, but durable delivery is incomplete.
**Outcome:** Recoverable booking notifications, reminders, and waitlist work across restarts and multiple workers.
**Concepts:** Transactional outbox, at-least-once processing, claiming/leases, deduplication, retry/backoff, failure inspection, external-provider boundaries.

- [ ] Session 1: Store domain mutation and outbox record in the same transaction; describe failure windows before coding the publisher.
- [ ] Session 2: Implement safe worker claiming, bounded retries with backoff, and inspectable terminal failure with controlled replay.
- [ ] Session 3: Add email through a local capture server or stub provider; use provider idempotency if supported and document the residual duplicate-delivery window.
- [ ] Session 4: Add pre-start reminders using an injected clock; recheck current booking status before sending and suppress cancelled bookings.
- [ ] Session 5: Make waitlist expiry/recovery safe after restart and with multiple workers; exercise crashes at processing boundaries.

**Tests to write:** No outbox event after rollback; eventual processing after restart; two-worker claim safety; provider timeout; repeated event handling; cancellation suppresses reminder; retry exhaustion visible; replay does not duplicate internal effects.
**Debugging lab:** Provider accepted a message but the worker crashed before recording success. Explain what can and cannot be guaranteed.
**Gate:** Demonstrate durable recovery, bounded retries, observable failures, and explicit duplicate-delivery semantics without arbitrary sleeps.
**Handoff:** Outbox/job schemas, retry policy, provider behavior, deduplication scope, recovery and replay runbook.
**Extension:** Signed outbound webhooks with replay protection and a test receiver; no broker is required for the core course.

### Chapter 14: Production, performance, and portfolio release

**Prerequisites:** Chapter 13.
**Starting state:** Complete local application and meaningful automated tests.
**Outcome:** Reproducible, secured deployment with evidence of correctness and operational understanding.
**Concepts:** CI gates, container builds, secrets/configuration, migrations, structured logs, metrics, request correlation, query analysis, load testing, deployment recovery.

- [ ] Session 1: Run frontend type/build/test checks, backend unit/integration checks, and critical browser flows in CI with isolated test data.
- [ ] Session 2: Add request correlation and useful latency/conflict/job metrics; secure operational endpoints and exclude secrets/personal data from logs.
- [ ] Session 3: Reproduce an N+1 or slow availability query, inspect SQL/query plans, improve it, and record comparable before/after results.
- [ ] Session 4: Load-test ordinary traffic and contested-slot traffic separately; assert booking integrity while measuring latency and errors.
- [ ] Session 5: Containerize and deploy with TLS, explicit allowed origins, secure cookies, externalized secrets, health checks, and a migration/backup/recovery procedure.
- [ ] Session 6: Run browser workflows at desktop/mobile sizes, verify keyboard access, and package the public demonstration and architecture notes.

**Tests to write/run:** Login-to-book-to-cancel; manager approval; cross-tenant denial; two-user conflict; waitlist expiry; dependency outage; migration against a previous schema snapshot; clean production build; backup restoration smoke check.
**Debugging lab:** A deployment-only login loop caused by proxy/origin/cookie configuration, plus a slow page traced to the database rather than guessed frontend fixes.
**Gate:** Deploy from documented instructions, show fresh test evidence, demonstrate recovery, and publish limitations honestly. Do not benchmark real public services without authorization.
**Handoff:** Release checklist, CI commands, deployment/runbook, demo accounts, architecture decisions, performance evidence, and known limitations.
**Extension:** Measure virtual threads for blocking workloads on the selected JDK, including database-pool limits, before claiming improvement. Do not add reactive programming solely for novelty.

## 5. Milestones and scope control

| Milestone | Chapters | Demonstrable result |
| --- | --- | --- |
| Resources Spring server | 01-05 | Tested, persistent, documented, and secured Resources API; server completion gate |
| Phase 2: React frontend | 06-08 | API-backed catalog and management workflows with browser verification |
| Java microservices | 09-10 | Independent Java consumer plus observable local multi-service runtime |
| Portfolio release | 11 | Integrated deployment, CI, measured performance, and recovery documentation |

Foundations may be fast-tracked only by passing their gates. Never skip database integrity or authorization because a happy-path UI works. Optional extensions do not block the next core chapter. If motivation or available time changes, stop at a milestone and document limitations rather than adding unfinished features.

### Outside the core course

- Kubernetes, service meshes, event sourcing, mandatory message brokers, service discovery, and API gateways unless a concrete later requirement justifies them.
- Payments, pooled equipment quantities, marketplace settlement, and complex billing.
- Native mobile applications or a bespoke calendar/recurrence engine.
- AI features added only as portfolio decoration.
- Full enterprise account provisioning, compliance certification, or claims of production-scale reliability without evidence.

## 6. Progress and independent chapter generation

### Master progress tracker

Update this table after each chapter. Detailed session history belongs in the learner's progress document once the application repository exists.

| Chapter | State | Evidence / next action |
| --- | --- | --- |
| 01 | Not started | Requires existing project inspection; Resources service foundation |
| 02 | Not started | Requires 01; resource lifecycle and API contracts |
| 03 | Not started | Requires 02; service-owned PostgreSQL persistence |
| 04 | Not started | Requires 03; search and API evolution |
| 05 | Not started | Requires 04; service security and organization scope |
| 06 | Not started | Requires completed Resources server (01-05); Phase 2 React catalog |
| 07 | Not started | Requires 06; resource management workflows |
| 08 | Not started | Requires 07; frontend/API browser verification |
| 09 | Not started | Requires 08; separate Java API consumer |
| 10 | Not started | Requires 09; local multi-service operations and observability |
| 11 | Not started | Requires 10; production and portfolio release |

### Checkpoint template

Use this after every session, including partially completed sessions:

```text
Chapter and session:
Current state: not started / in progress / blocked / verified
Implemented behavior:
Files and public contracts added or changed:
Schema/migration changes:
Decisions and reasons:
Commands actually run and their results:
Failing scenario and diagnostic evidence:
What I can now explain without notes:
Known limitations or skipped exercises:
Next smallest task:
```

### Copyable chapter-generation prompt

```text
Read COURSE_PLAN.md and generate the complete learning guide for Chapter NN only.
For an optional algorithm lab, replace Chapter NN with Lab A1, A2, A3, or A4
and use the relevant algorithm-lab prerequisites, contracts, references, and gates.
Use Section 4's authoritative Resources microservice roadmap for chapter number,
scope, order, and prerequisites. Earlier booking-platform briefs are legacy topic
references only and are outside the core course unless explicitly requested.
Chapters 01-05 complete the Resources Spring server. Phase 2 is Chapters 06-08:
return to React only after the Chapter 05 server completion gate. Chapters 09-10
cover Java microservice integration and operations. The Resources service owns
resource data; a companion Java consumer must use its HTTP contract and must
never access its database directly.

I will write all application and test code myself. Do not implement the app,
execute the chapter tasks, or include complete implementation/test solutions.
You may write the chapter guide to docs/chapters/NN-short-title.md.

Read docs/progress.md, docs/versions.md, relevant decisions, the preceding
chapter handoff, and the current application code when they exist. State what
actually exists versus what this plan proposes. Do not invent completed work.
If the repository or handoff is unavailable, state the assumed starting contract
and ask only for missing information that prevents a usable chapter guide.

Include:
1. Starting state, prerequisites, and exact end-of-chapter outcomes.
2. User stories, acceptance criteria, domain policies, and explicit exclusions.
3. Related concepts explained briefly with official documentation references.
   Use Section 9's reading map to choose 2-4 essential references for this chapter,
   name the relevant sections, and connect each reading to an exercise. Keep
   deeper references optional; do not assign an entire documentation site.
   For algorithm labs, also require a baseline oracle, complexity analysis,
   seeded differential tests, and reproducible performance measurements.
4. Ordered approximately hour-sized sessions, each with a concrete deliverable,
   test scenarios I must implement, debugging observations, and a stopping point.
5. Test names or scenario descriptions and assertions, but not finished test bodies.
6. A local debugging lab with symptoms, reproduction conditions, evidence to
   collect, and a regression-test requirement. Keep the root-cause answer hidden.
7. Three graduated hints in a separate optional section, not inline spoilers.
8. A completion checklist, independent explanation questions, a changed-requirement
   exercise, and a fast-track gate if this is a foundation chapter.
9. Optional advanced extensions and a checkpoint/handoff template recording actual
   contracts, decisions, commands, limitations, and the next chapter's starting state.

Preserve earlier contracts unless the chapter explicitly teaches a migration.
Do not begin Phase 2 frontend work before the Resources server completion gate
in Chapter 05. The repository may already contain frontend code; inspect it,
distinguish completed work from the learning sequence, and reuse it without
rewriting unrelated work. Match the
actual Java and Spring versions in backend/pom.xml and docs/versions.md; surface
any mismatch rather than silently changing the project baseline.
Verify current stable framework compatibility when versions have not yet been
recorded; do not silently upgrade an existing working application. Do not claim
that unrun tests pass. Ask before adding major features outside this master plan.
```

### Copyable help prompt during an exercise

```text
I am working on Chapter NN, Session N of COURSE_PLAN.md.
Expected behavior:
Observed behavior:
Smallest reproduction:
Commands/test results and relevant code:
My current hypothesis:

Help me investigate. Ask for the most useful missing evidence or give the next
small diagnostic step. Do not reveal the full fix unless I explicitly ask.
```

## 7. Portfolio evidence checklist

- [ ] A README with the Resources service boundary, owned data, API contract, local startup, tests, and demo instructions.
- [ ] A service diagram showing Resources, its database, the separate Java consumer, and the React client; document which service owns each datum.
- [ ] A direct API demonstration of resource creation, search, validation errors, and archival.
- [ ] A service-to-service demonstration showing contract use and behavior when Resources is slow or unavailable.
- [ ] Passing CI with meaningful unit, API, database, security, consumer contract, and browser checks.
- [ ] Measured performance results with hardware/environment, dataset, method, and limitations disclosed.
- [ ] Debugging writeups showing evidence, root cause, repair, and regression tests for API and remote-service failures.
- [ ] An independently deployable Resources service and a documented local multi-service run configuration.
- [ ] A deployed demonstration with safe sample data, no committed secrets, and documented service configuration.
- [ ] A recovery runbook and an honest list of limitations and future extensions, including any separately scoped booking service.

## 8. Master-plan validation

Before treating a generated chapter as ready, verify that it preserves this plan's policies, uses the actual previous state, contains a runnable verification path, includes both test-writing and debugging work, and leaves a clear handoff. A chapter is a learning guide, not evidence that the application has been implemented or verified.

## 9. Reference library and reading map

### How to use these sources

Read to solve the current exercise, not to postpone implementing it. Start with the chapter's essential references, spend approximately 10-15 minutes on the relevant sections, then apply what you read. Longer investigations can have a dedicated session.

Official documentation is the primary reference for behavior and APIs. Maintainer guides, standards, and specialist articles provide deeper explanations. Examples are learning material, not code to paste into the project without understanding.

Versionless documentation and links containing `current`, `latest`, or `stable` can move to newer releases. Select documentation matching the versions recorded in the project. In particular, use Java API references matching the selected JDK and Spring APIs supported by the selected Spring Boot release. A working link is not proof of API compatibility.

### A. React, TypeScript, and browser fundamentals

| ID | Source | Read for / apply to |
| --- | --- | --- |
| F1 | [React Learn](https://react.dev/learn) | Components, rendering lists, events, and state. Rebuild a small interaction without looking at the example. |
| F2 | [React: Managing State](https://react.dev/learn/managing-state) | State ownership, state structure, and preserving/resetting state. Explain where catalog filters and selection should live. |
| F3 | [React: You Might Not Need an Effect](https://react.dev/learn/you-might-not-need-an-effect) | Remove redundant derived state and event-driven effects from the catalog. |
| F4 | [React: Synchronizing with Effects](https://react.dev/learn/synchronizing-with-effects) | Dependencies, cleanup, development remounting, and request synchronization. Reproduce stale-response behavior. |
| F5 | [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html) | Narrowing, object types, unions, and generics. Model loading/error/success states without unsafe assertions. |
| F6 | [MDN: Using Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) | HTTP errors versus rejected requests, JSON parsing, and cancellation. Explain why a 400 response does not itself reject fetch. |
| F7 | [React Router documentation](https://reactrouter.com/start/declarative/installation) | Routes, links, parameters, and URL search state. Use the mode matching the existing Vite application. |
| F8 | [TanStack Query: React Overview](https://tanstack.com/query/latest/docs/framework/react/overview) | Query keys, caching, mutations, invalidation, and retries. Explain server state versus form state. |
| F9 | [Vite Guide](https://vite.dev/guide/) | Project setup, development proxy, environment variables, and production builds. Understand that client environment values are not secrets. |

### B. Java, Spring, persistence, and HTTP

| ID | Source | Read for / apply to |
| --- | --- | --- |
| B1 | [Dev.java learning material](https://dev.java/learn/) | Targeted refreshers on records, exceptions, collections, streams, and JVM tools. Match language features to the project's selected JDK. |
| B2 | [Java 25 java.time API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/package-summary.html) | ZoneId and other time types used in resource metadata. Use the API version matching the selected project JDK. |
| B3 | [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/index.html) | Configuration, profiles, application startup, test support, and production features. Match the project's Boot version. |
| B4 | [Spring Guides](https://spring.io/guides) | Short official exercises such as Building a RESTful Web Service and Testing the Web Layer. Transfer the concepts to resources rather than copying the sample app. |
| B5 | [Spring MVC Reference](https://docs.spring.io/spring-framework/reference/web/webmvc.html) | Request mapping, DTO binding, validation, exception handling, and Problem Details responses. |
| B6 | [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/) | Repository queries, pagination, entity persistence, locking, and auditing. Distinguish convenience APIs from database guarantees. |
| B7 | [Spring transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html) | Declarative transactions, proxies, propagation, isolation, and rollback. Investigate self-invocation and ineffective boundaries. |
| B8 | [Flyway documentation](https://documentation.red-gate.com/fd) | Versioned migrations, schema history, validation, and migration lifecycle. Rebuild the database from an empty instance. |
| B9 | [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html) | Standard error representation. Define stable application codes without leaking stack traces. |
| B10 | [Spring Framework REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html) | Select and configure an HTTP client for the companion Java service; make timeouts and error handling explicit. |
| B11 | [OpenAPI Specification](https://spec.openapis.org/oas/latest.html) | Describe the public Resources API and reason about compatible changes for independent consumers. |
| B12 | [Spring Microservices](https://spring.io/microservices) | Spring's microservice overview and ecosystem orientation. Use it to understand the Java service context without adopting unnecessary infrastructure. |

### C. Writing useful automated tests

| ID | Source | Read for / apply to |
| --- | --- | --- |
| T1 | [Vitest Guide](https://vitest.dev/guide/) | Test setup, assertions, mocks, controlled time, and focused runs. Choose the runner version compatible with Vite. |
| T2 | [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/) | Test observable DOM behavior rather than component internals. |
| T3 | [Testing Library query priorities](https://testing-library.com/docs/queries/about/) | Accessible role/name and label queries; when asynchronous queries are necessary. Avoid arbitrary waits and fragile selectors. |
| T4 | [user-event documentation](https://testing-library.com/docs/user-event/intro/) | Typing, clicking, tabbing, and realistic interaction sequences. |
| T5 | [Mock Service Worker documentation](https://mswjs.io/docs/) | Network-boundary mocking, errors, delays, and controlled request behavior for frontend tests. |
| T6 | [JUnit User Guide](https://docs.junit.org/current/user-guide/) | Assertions, parameterized tests, lifecycle, and test organization. Choose the version supported by the Boot-managed dependencies. |
| T7 | [Spring Framework testing reference](https://docs.spring.io/spring-framework/reference/testing.html) | Unit versus integration scope, MockMvc, test contexts, and transaction behavior in tests. |
| T8 | [Testcontainers for Java](https://java.testcontainers.org/) | Real PostgreSQL integration tests, container lifecycle, isolation, and reproducible database setup. |
| T9 | [Playwright introduction](https://playwright.dev/docs/intro) | Browser setup, isolated contexts, assertions, and critical end-to-end workflows. |
| T10 | [Playwright best practices](https://playwright.dev/docs/best-practices) | User-facing locators, auto-waiting, independent tests, and diagnosing CI failures. |
| T11 | [The Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html) | Specialist explanation of test boundaries and tradeoffs. Justify which service boundary owns each assertion. |

### D. PostgreSQL correctness and performance

| ID | Source | Read for / apply to |
| --- | --- | --- |
| D1 | [PostgreSQL range types](https://www.postgresql.org/docs/current/rangetypes.html) | Half-open intervals, overlap operators, and exclusion constraints. The reservation examples directly match this application. |
| D2 | [PostgreSQL transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html) | Read Committed, Repeatable Read, Serializable, and retry implications. Predict interleaved booking outcomes. |
| D3 | [PostgreSQL explicit locking](https://www.postgresql.org/docs/current/explicit-locking.html) | Row locks, deadlocks, and lock ordering. Compare an actual locking design with database exclusion protection. |
| D4 | [PostgreSQL EXPLAIN](https://www.postgresql.org/docs/current/using-explain.html) | Read plans, estimates versus actual rows, index choices, and buffers. Understand that EXPLAIN ANALYZE executes the statement. |
| D5 | [Use The Index, Luke!](https://use-the-index-luke.com/) | Specialist material on indexes, composite predicates, sorting, and pagination. Validate advice with the project's own PostgreSQL query plans. |

### E. Security, accessibility, and time-sensitive interfaces

| ID | Source | Read for / apply to |
| --- | --- | --- |
| S1 | [Spring Security Servlet Reference](https://docs.spring.io/spring-security/reference/servlet/index.html) | Filter chain, authentication, sessions, authorization, CSRF, and security testing. Use Servlet material, not a reactive configuration. |
| S2 | [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html) | Deny by default, object-level access checks, and server-side enforcement. Build the role/tenant test matrix. |
| S3 | [OWASP CSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html) | Session-authenticated browser threat model, tokens, cookie attributes, and origin assumptions. Do not disable CSRF just to make requests pass. |
| S4 | [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html) | Session lifecycle, secure cookies, expiry, logout, and sensitive logging concerns. |
| U1 | [WAI-ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/) | Keyboard/focus patterns for dialogs, grids, and date pickers. Prefer native controls where possible. |
| U2 | [WAI Forms Tutorial](https://www.w3.org/WAI/tutorials/forms/) | Labels, instructions, errors, validation feedback, and accessible booking forms. |
| U3 | [FullCalendar documentation](https://fullcalendar.io/docs) | A calendar-library candidate: inspect React integration, zones, accessibility, and licensing before choosing. Listing it does not prescribe adoption. |
| U4 | [RFC 5545: iCalendar](https://www.rfc-editor.org/rfc/rfc5545.html) | Recurrence, exceptions, and time-zone semantics as a reference standard. Implement only the course's bounded weekly subset through a maintained library. |

### F. Debugging and production reliability

| ID | Source | Read for / apply to |
| --- | --- | --- |
| O1 | [Chrome DevTools: Debug JavaScript](https://developer.chrome.com/docs/devtools/javascript/) | Breakpoints, call stacks, scopes, and async execution. Use equivalent browser tools if preferred. |
| O2 | [Chrome DevTools: Network](https://developer.chrome.com/docs/devtools/network/) | Request payloads, response timing, headers, failed requests, and throttling. Distinguish server failures from client rendering errors. |
| O3 | [VS Code Java debugging](https://code.visualstudio.com/docs/java/java-debugging) | Attach/launch configuration, conditional breakpoints, exceptions, and variable inspection. |
| O4 | [Playwright Trace Viewer](https://playwright.dev/docs/trace-viewer-intro) | Inspect action timelines, DOM snapshots, console, and network evidence for browser failures. |
| O5 | [Transactional outbox pattern](https://microservices.io/patterns/data/transactional-outbox.html) | Specialist architecture reference for commit/publication failure windows and duplicate delivery. The pattern also applies to a monolith. |
| O6 | [Spring Boot Actuator Reference](https://docs.spring.io/spring-boot/reference/actuator/index.html) | Health, metrics, endpoint exposure, and operational security. Do not publicly expose every endpoint. |
| O7 | [GitHub Actions documentation](https://docs.github.com/en/actions) | Reproducible CI jobs, database dependencies, secrets, and failure artifacts. |
| O8 | [Docker: Java guide](https://docs.docker.com/guides/java/) | Building and running Java applications in containers. Adapt examples to the selected project JDK. |
| O9 | [Grafana k6 documentation](https://grafana.com/docs/k6/latest/) | Workload modeling, thresholds, checks, and interpreting load-test results. Include correctness checks, not just request throughput. |
| O10 | [Docker Compose documentation](https://docs.docker.com/compose/) | Define and run the local multi-service environment without coupling application builds or database ownership. |

### Chapter-by-chapter reading map

IDs refer to the tables above. Read the essential set first; use deeper material when the exercise exposes the need. A generator should identify specific headings within these resources rather than assigning whole manuals.

| Chapter | Essential starting references | Deeper or diagnostic references |
| --- | --- | --- |
| 01: Resources service foundation | B3, B4, B5, B12, T6, T7 | O3, O1, O2 |
| 02: Resource lifecycle/contracts | B5, B9, B11, T7 | B4, T6 |
| 03: Service-owned persistence | B6, B8, T8 | T7, D4 |
| 04: Search/API evolution | B6, B11, D4, D5 | T8, T11 |
| 05: Service security/scope | S1, S2, T7 | B3, O2 |
| 06: React resource catalog | F1, F5, F6, T1, T2 | F9, O1, O2 |
| 07: Resource management UI | F2, F7, F8, T3, T4, U2 | S1, T5 |
| 08: Frontend/API browser tests | T9, T10, T5, O4 | O2, T11 |
| 09: Java API consumer | B10, B11, B12, T7, T8 | B3, O3, O6 |
| 10: Operating services | B3, O6, O7, O8, O10 | O9, O3 |
| 11: Production release | O6, O7, O8, O9 | D4, D5, T10, O4 |

For Chapter 09, test remote behavior with a controlled HTTP server and the public API contract, not with direct database access. Use MSW for frontend network behavior in Chapters 06-08. Booking recurrence and SSE references below apply only if those features are later added to a separate service.

### Optional deeper study

- [Designing Data-Intensive Applications](https://dataintensive.net/): a paid book, optional for deeper reasoning about transactions, replication, and delivery guarantees. It is not a prerequisite or a step-by-step Spring tutorial.
- [Spring Academy](https://spring.academy/): guided Spring learning as a supplement; check current account/access requirements and choose material matching the project's release.
- [Dev.java Watch and Listen](https://dev.java/learn/watchlisten/): videos and talks for Java/JVM topics; verify that an example uses features supported by the selected project JDK before adopting it.

When a tutorial disagrees with official documentation or the recorded application policy, investigate the version and context instead of combining incompatible snippets. Record useful corrections and newly discovered references in the chapter handoff.

## 10. Optional booking-service algorithm extension

### Purpose and boundaries

These inherited labs provide algorithm-intensive practice for a separately approved booking-service extension. They are not features of the Resources service or part of the core course. They are inspired by difficult interval, data-structure, graph, and optimization problems, not copies of LeetCode questions or a claim that every introductory variant is Hard. Difficulty increases through explicit constraints and extensions.

They do not block any core chapter or milestone. Generate them only after a booking service with the required data and contracts exists and the user chooses to pursue that extension. Their old chapter-number prerequisites refer to the superseded booking plan and must not be used as prerequisites for the current 11-chapter course.

Implement the learning algorithms yourself in isolated Java domain components, without Spring dependencies in their core logic. Match language features to the selected JDK. This is a deliberate educational exception to using proven libraries for production domain engines. Keep calendar rendering and recurrence parsing on maintained libraries. Compare with a maintained solver when useful, but do not delegate the learning task to it.

Algorithm outputs are recommendations from a snapshot, not reservations. Any accepted recommendation must still pass the booking service's authenticated, tenant-scoped, transactional rules and database constraints. Never replace database integrity protection with an in-memory tree or scheduler.

### Shared lab requirements

- Define inputs, outputs, bounds, tie rules, and invalid inputs before coding. Identify whether the variant is an exact polynomial-time problem or a harder generalization.
- Begin with a simple correct baseline that can act as an oracle on small inputs. Keep it available in tests after optimization.
- Write named example tests, then seeded randomized differential tests comparing the optimized result with the oracle.
- For non-unique answers, compare feasibility and objective value rather than requiring identical output lists.
- State time and space complexity with defined variables, including preprocessing, reconstruction, storage, and update costs.
- Add adversarial cases, investigate an intentional local defect, and retain its regression test.
- Measure performance on reproducible datasets at increasing sizes. Separate algorithm measurements from HTTP, serialization, and database costs; do not turn noisy elapsed-time thresholds into unit tests.
- Add a thin application integration only after pure-domain correctness is established. Use existing authorization and API error conventions.
- Record the practical crossover point: an optimized solution is not automatically worthwhile for small organization datasets.

### Lab A1: Common availability across calendars

**Prerequisites:** A separately implemented booking service with resource availability, participant calendars, and authenticated data access.
**Starting state:** Tested interval rules and authoritative resource availability already exist.
**Outcome:** Recommend meeting windows when every selected participant and at least one eligible resource are available.
**Contract:** Inputs are a finite instant-based search window, positive meeting duration, participant availability intervals, and each eligible resource's available intervals. Normalize each calendar by clipping to the search window and merging overlapping or adjacent intervals. Return maximal continuous common windows per resource, retaining only windows at least as long as the duration; do not enumerate every possible start instant.
**Algorithms:** Brute-force boundary partitioning, sorted interval merging/intersection, multiway intersection or sweep-line alternatives.

- [ ] Session 1: Specify the contract and construct a tiny boundary-partition oracle with example-based tests.
- [ ] Session 2: Implement normalized interval intersection and compare it with the oracle on seeded random calendars.
- [ ] Session 3: Add deterministic ordering by window start and resource ID; analyze complexity in terms of calendars, resources, and interval counts.
- [ ] Session 4: Add an authorized suggestion endpoint and React results view; revalidate when the user chooses a slot.

**Tests to write:** Empty participant or resource selection is rejected; a selected participant with no availability yields no windows; unsorted/duplicate/nested intervals; adjacency; exact-duration fit; clipping; no common window; input permutation invariance; equivalent instants supplied with different offsets; booking changes after suggestion.
**Debugging lab:** A sweep processes starts and ends inconsistently at equal timestamps and invents or drops a valid window. Collect the event trace and add a boundary regression test.
**Gate:** Demonstrate oracle agreement, maximal non-overlapping output per resource, complexity analysis, and a stale suggestion rejected safely at booking time.
**Handoff:** Input normalization, window semantics, reference/optimized implementations, random seeds, benchmark datasets, and integration contract.
**Harder extension:** Find windows meeting a participant quorum, rank by attendee preference, and return top-k results. Define ranking and tie policies before choosing a heap or sweep strategy; do not silently change the all-participants core contract.

### Lab A2: Dynamic availability index

**Prerequisites:** A booking service with persisted capacity intervals and an explicit policy for refreshing or invalidating any derived index.
**Starting state:** Availability is computed from persisted capacity-blocking intervals.
**Outcome:** Compare a linear availability index with a segment-tree index supporting repeated additions, removals, and range queries.
**Contract:** The lab uses a finite fixed UTC horizon divided into five-minute cells. Updates carry unique interval IDs and grid-aligned half-open intervals. Adding an existing ID or removing an unknown ID fails without changing state. Each interval contributes +1 to covered cells; deleting it reverses exactly that contribution. Query whether the maximum occupancy over a non-empty range is zero. Reject out-of-horizon or non-grid inputs in the pure lab.
**Algorithms:** Baseline cell counters; lazy-propagation segment tree for range add and range maximum; optional coordinate-compressed variants.

- [ ] Session 1: Implement the cell-counter baseline and interval-ID ledger; define validation and invariant tests.
- [ ] Session 2: Implement range add and range maximum with lazy propagation, preserving the ledger contract.
- [ ] Session 3: Differential-test long generated update/query sequences and retain the smallest failing sequence as a regression fixture.
- [ ] Session 4: Benchmark linear versus indexed workloads and document a safe rebuild/invalidation strategy before any read-only application experiment.

**Tests to write:** Full horizon; one cell; touching boundaries; overlapping contributions; removing one overlapping interval leaves the other; duplicate add; repeated remove; empty/reversed range; invalid grid alignment; update followed by inverse restores prior queries.
**Debugging lab:** A partially covered node receives or propagates a lazy value incorrectly, so only some later queries fail. Inspect node ranges and accumulated values rather than trusting a successful full-range query.
**Gate:** Show O(log H) updates and queries, O(H + N) storage including the ledger, and oracle agreement, where H is cell count and N is tracked interval count. Measure both sparse and dense workloads.
**Handoff:** Horizon/grid contract, tree invariants, generated sequence seeds, memory measurements, and snapshot freshness policy.
**Integration boundary:** The core application accepts arbitrary supported instants, not just this grid. If projecting real occupancy, round occupied intervals outward conservatively, label the result advisory, and recompute exact availability before acceptance. A stale index must never authorize a booking.
**Harder extension:** Use offline coordinate compression and specify what happens when an unseen endpoint arrives; or augment a structure to find the first contiguous free window of a requested length. Re-derive invariants and complexity for the chosen variant rather than assuming the core tree already supports it.

### Lab A3: Batch resource allocation

**Prerequisites:** A booking service with organization-scoped requests, resource eligibility, and an authorized batch workflow; familiarity with graph traversal.
**Starting state:** Members can request resources and managers can inspect organization-scoped availability.
**Outcome:** A manager receives a maximum-cardinality allocation of pending requests to eligible resources for one shared time interval.
**Contract:** Every request in the batch uses the same fixed interval and needs exactly one resource. An edge means that a resource satisfies that request's requirements and is available for the interval. Each request and resource may appear at most once in the result. Unmatched requests remain visible. Distinct resource IDs, not display names, define identity.
**Algorithms:** Exhaustive assignment baseline on tiny graphs; augmenting-path bipartite matching; Hopcroft-Karp as an optional scale-up; min-cost flow for the weighted extension.

- [ ] Session 1: Define eligibility and implement a tiny exhaustive oracle; construct a counterexample to greedy first-fit assignment.
- [ ] Session 2: Implement augmenting-path matching and verify maximum cardinality against the oracle.
- [ ] Session 3: Add an organization-scoped preview UI showing matched and unmatched requests; keep preview separate from acceptance.
- [ ] Session 4: Apply the chosen batch in one transaction through existing capacity protection; reject and recompute if availability changed rather than silently accepting a different assignment.

**Tests to write:** No edges; more requests than resources; more resources than requests; disconnected graph; a reassignment chain needed to reach the optimum; multiple valid maxima; ineligible resource never assigned; tenant boundaries; stale preview causes atomic rollback.
**Debugging lab:** Search marks a vertex visited across independent augmentation attempts and misses an augmenting path. Record matching changes and compare the cardinality with the oracle.
**Gate:** Prove every assignment is feasible and maximum-cardinality on oracle-sized cases; analyze the chosen algorithm using V vertices and E eligibility edges; demonstrate transactional acceptance of the preview.
**Handoff:** Eligibility rules, graph representation, matching contract, complexity, tie/ordering policy, and batch-acceptance behavior.
**Harder extension:** Among maximum-cardinality matchings, minimize integer preference cost using min-cost max-flow. Test residual reverse edges, negative residual costs, and potential cost overflow. Compare objective pairs of accepted count and total cost, not one arbitrary matching.
**Scope boundary:** Requests with arbitrary overlapping durations or multi-resource bundles are not the same bipartite matching problem. Treat them as a new scheduling model and analyze tractability before choosing a solver or heuristic.

### Lab A4: Weighted booking optimization

**Prerequisites:** A booking service with priority-bearing requests, persisted intervals, and authenticated organization scope.
**Starting state:** The application can evaluate candidate intervals without confirming them.
**Outcome:** Recommend the highest-total-priority compatible subset of requests for one resource.
**Contract:** Requests have unique IDs, fixed half-open intervals, and non-negative integer priority scores; start precedes end. Remove candidates conflicting with already committed capacity before optimizing. Requests cannot move in time. Return selected IDs and total score; any maximum-score feasible subset is acceptable when tied. Use a numeric representation that can safely hold the total score.
**Algorithms:** Exhaustive subset oracle for small inputs; weighted interval scheduling with sorting, predecessor binary search, dynamic programming, and reconstruction.

- [ ] Session 1: Implement a bounded exhaustive oracle and construct counterexamples to earliest-start, highest-score-first, and unweighted earliest-finish heuristics.
- [ ] Session 2: Implement predecessor lookup and the optimal-score computation; test equal end/start boundaries explicitly.
- [ ] Session 3: Reconstruct an actual selected schedule and differential-test objective value and feasibility against the oracle.
- [ ] Session 4: Add a manager-only preview and benchmark the optimizer; confirm chosen requests only through the existing transactional booking path.

**Tests to write:** Empty input; one request; all overlap; all adjacent; nested intervals; duplicate endpoints with different IDs; zero priorities; large totals; multiple optima; unsorted input; committed-capacity conflicts; reconstructed IDs are unique and sum to the reported score.
**Debugging lab:** Binary search uses a strict comparison and incorrectly rejects back-to-back bookings, or reconstruction returns a set inconsistent with the optimal score. Retain independent score and feasibility checks.
**Gate:** Demonstrate O(N log N) time and O(N) auxiliary storage including reconstruction, oracle agreement on bounded inputs, and a before/after scaling report. State the oracle's exponential cost and cap generated oracle cases accordingly.
**Handoff:** Objective, score bounds, tie behavior, predecessor semantics, reconstruction tests, and preview acceptance policy.
**Harder extension:** Model K identical resources with fixed intervals and additive priorities using a time-expanded min-cost flow network. Explain why a new resource-specific eligibility or coupled multi-resource constraint changes that model; do not present arbitrary constrained scheduling as ordinary weighted interval scheduling.

### Algorithm references and reading map

Use the conceptual sections first; some references expose complete implementations, so consult their code only after attempting the exercise. Translate examples to the selected Java version instead of copying another language's code.

| ID | Reference | Purpose |
| --- | --- | --- |
| G1 | [MIT OpenCourseWare: Introduction to Algorithms](https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-fall-2011/) | Complexity, sorting, graph traversal, and dynamic programming foundations with lectures and exercises. |
| G2 | [CP-Algorithms: Segment Tree](https://cp-algorithms.com/data_structures/segment_tree.html) | Range-query invariants, tree storage, and lazy propagation; match the operation combination to the lab. |
| G3 | [CP-Algorithms: Kuhn's maximum bipartite matching](https://cp-algorithms.com/graph/kuhn_maximum_bipartite_matching.html) | Augmenting paths, correctness intuition, and matching complexity. |
| G4 | [CP-Algorithms: Minimum-cost flow](https://cp-algorithms.com/graph/min_cost_flow.html) | Residual networks, costs, and the weighted allocation extensions. |
| G5 | [Princeton Algorithms: Dynamic Programming](https://www.cs.princeton.edu/~wayne/kleinberg-tardos/pdf/06DynamicProgrammingI.pdf) | Weighted interval scheduling, predecessor computation, recurrence reasoning, and reconstruction. |
| G6 | [jqwik User Guide](https://jqwik.net/docs/current/user-guide.html) | Optional Java property-based test generation, reproducible seeds, and shrinking. Verify compatibility before adding it. |
| G7 | [OpenJDK JMH](https://github.com/openjdk/jmh) | Optional JVM microbenchmark harness; account for warmup and optimization when measuring pure Java algorithms. |

| Lab | Essential references | Additional references |
| --- | --- | --- |
| A1 | G1, B2, D1 | G6, T6 |
| A2 | G2, T6 | G6, G7 |
| A3 | G3, G1 | G4, G6, T8 |
| A4 | G5, G1 | G4, G6, G7 |

### Algorithm progress and generation

Use the existing chapter-generation prompt with the desired lab ID, for example: "Read COURSE_PLAN.md and generate the complete learning guide for Lab A2 only." Save generated guides under `docs/chapters/A2-dynamic-availability.md` or the corresponding lab name. Generate only the requested lab, not its solutions or a new application.

| Lab | State | Earliest recommended point |
| --- | --- | --- |
| A1 | Not started | After a booking service implements the required availability contracts |
| A2 | Not started | After a booking service persists capacity intervals |
| A3 | Not started | After a booking service implements organization-scoped batch requests |
| A4 | Not started | After a booking service implements priority-bearing requests |

For each completed lab, add an optional portfolio appendix containing the input contract, baseline and optimized complexity, correctness properties, reproducible benchmark method, observed crossover point, and the reason to adopt or reject production integration. Keep these achievements separate from unimplemented extensions and do not claim a formal LeetCode difficulty rating.