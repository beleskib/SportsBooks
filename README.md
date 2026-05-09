<div align="center">

# SportsBooks

**Find courts. Book coaches. Play together.**

The all-in-one platform for booking sports venues, finding coaches, and organizing games with friends.

[![Platforms](https://img.shields.io/badge/platforms-iOS%20%7C%20Android%20%7C%20Web-blue)]()
[![Backend](https://img.shields.io/badge/backend-Node.js%20%7C%20PostgreSQL-green)]()
[![Branch](https://img.shields.io/badge/branch-v2--practical--ux-orange)]()
[![License](https://img.shields.io/badge/license-Proprietary-lightgrey)]()

</div>

---

## What is SportsBooks?

SportsBooks is a cross-platform sports booking app. **Players** browse sports, find venues or coaches, and book hourly time slots between 9:00 and 22:00. **Partners** (venue owners and coaches) list their services, manage their calendar, and get paid via Stripe Connect.

It also handles the part nobody else does well: **finding people to play with**. Open a matchmaking lobby, let strangers or friends join, and split the cost automatically when it fills up.

The `v2-practical-ux` branch adds a unified home feed, one-tap rebook, split payments, direct messaging, subscription tiers, and a polished light-theme UI across all platforms.

---

## Architecture

```
Mobile Apps (iOS + Android)
         |
    REST API  ──────────  PostgreSQL 16
         |                     |
   Firebase Auth          58 migrations
   (token validation)     (full schema)
         |
   Stripe Connect
   (partner payouts)
```

- **Mobile** — Kotlin/Compose (Android), Swift/SwiftUI (iOS)
- **Web** — React 18 + Vite + TailwindCSS (admin & partner dashboard)
- **Backend** — Node.js + Express + TypeScript REST API
- **Database** — PostgreSQL 16 with 58 versioned migrations
- **Auth** — Firebase Auth (token validation only; all business data in PostgreSQL)
- **Payments** — Stripe Connect (direct payouts to partners)
- **Push** — Firebase Cloud Messaging
- **Storage** — Firebase Storage (venue/coach images)

---

## Features

### Core booking

| Feature | Description |
|---------|-------------|
| **Sport categories** | Basketball, football, tennis, padel, volleyball, swimming, boxing, MMA, yoga, pilates, crossfit, running, cycling, golf, badminton, table tennis, handball, baseball, cricket |
| **Venue booking** | Browse venues by sport, location, price. Real-time slot availability (9:00-22:00). Photos, equipment lists, reviews |
| **Coach booking** | Find coaches by sport and specialization. Certifications, experience, ratings. Same time-slot system |
| **Payments** | Stripe checkout for players, Stripe Connect direct payouts for partners |
| **Reviews & ratings** | Post-booking reviews for venues and coaches. Average ratings, review counts |
| **Discounts** | Percent or fixed-amount coupons with validity periods. Powers the "Top Deals" feed |

### Social

| Feature | Description |
|---------|-------------|
| **Friends** | Send/accept friend requests, search users, remove friends |
| **Direct messaging** | Real-time DMs between friends with unread badges and push notifications |
| **Communities** | Create or join sport communities. Community-scoped lobbies and chat |
| **Matchmaking lobbies** | Create open lobbies, invite friends or strangers, skill-level filtering |
| **Match system** | Host matches with recurrence rules, join requests, in-match chat, player ratings |
| **Parties** | Form a party with friends before booking together |
| **Available players** | Register as available for a sport so others can find and invite you |

### v2-practical-ux additions

| Feature | Description |
|---------|-------------|
| **Unified home feed** | One endpoint returns greeting, rebook suggestions, community lobbies, friends available to play, and upcoming bookings — replaces 5+ round trips |
| **Unified play search** | Lobbies + open slots + available players ranked together with distance and skill-level filtering |
| **One-tap rebook** | Copy a past booking to a new slot on the same venue/coach. Accepts `timeSlotId` or `slotDate + startTime` |
| **Split payments** | Multi-person cost-sharing with equal or custom splits. Optional expiration. Per-share payment tracking |
| **Booking participants** | Invite friends to a booking, RSVP tracking, attendance marking (attended/no-show) |
| **SportsBooks+** | Subscription tier with profile visibility controls and premium features, backed by Stripe Billing |
| **Inbox UI** | Web inbox with conversation list + message thread (split-panel layout, chat bubbles, date separators). Android chat screens with Material 3 |

### Partner & admin tools

| Feature | Description |
|---------|-------------|
| **Partner dashboard** | Reservation management, analytics (revenue, bookings, ratings), earnings tracking |
| **Calendar (v2)** | Weekly availability grid (9:00-22:00) with drag-to-block |
| **Listing approval** | New venues/coaches start as `pending`. Admin reviews and approves before they go public |
| **Admin overview** | Platform-wide metrics, user management, booking oversight |
| **Stripe Connect** | One-time partner onboarding, payout status, dashboard links |
| **Discount campaigns** | Create and manage promotions surfaced as "Top Deals" |

### Infrastructure

| Feature | Description |
|---------|-------------|
| **Push notifications** | Firebase Cloud Messaging for booking updates, chat messages, lobby invites, match events |
| **Rate limiting** | Per-route rate limits on auth and API endpoints |
| **Dev auth bypass** | `DEV_AUTH_BYPASS=true` skips Firebase token validation for local testing |
| **Structured logging** | Winston logger with file + console transports |
| **Email service** | Booking confirmation emails (file transport for dev, SMTP for production) |
| **Error handling** | Typed error classes (`NotFoundError`, `ForbiddenError`, `ValidationError`, `ConflictError`) with consistent API responses |

---

## Tech Stack

| Layer | Technology | Location |
|-------|------------|----------|
| Web Frontend | React 18, TypeScript, Vite, TailwindCSS | `src/frontend/` |
| Backend API | Node.js, Express, TypeScript | `src/backend/` |
| Android | Kotlin, Jetpack Compose, Hilt, Retrofit, Coil | `app/` |
| iOS | Swift, SwiftUI, URLSession | `src/ios/` |
| Database | PostgreSQL 16, 58 migrations | `database/` |
| Shared Contracts | TypeScript interfaces & endpoint constants | `src/shared/` |
| Auth | Firebase Auth | All platforms |
| Storage | Firebase Storage | All platforms |
| Payments | Stripe Connect + Stripe Billing | Backend + Mobile |

---

## API Endpoints

The backend exposes a comprehensive REST API. All endpoints require `Authorization: Bearer <Firebase ID Token>` unless noted.

<details>
<summary><strong>Click to expand full endpoint list</strong></summary>

| Group | Endpoints |
|-------|-----------|
| **Auth** | `POST /auth/register` |
| **Users** | `GET /users/me`, `PUT /users/me/role` |
| **Sports** | `GET /sports`, `GET /sports/:id` |
| **Venues** | `GET /venues`, `GET /venues/:id`, `GET /venues/by-sport/:sportType`, `GET /venues/top-deals`, `GET /venues/search`, `GET /venues/mine`, `POST /venues`, `PUT /venues/:id`, venues images/equipment/time-slots sub-routes |
| **Coaches** | `GET /coaches`, `GET /coaches/:id`, `GET /coaches/by-sport/:sportType`, `GET /coaches/top-deals`, `GET /coaches/search`, `GET /coaches/mine`, `POST /coaches`, `PUT /coaches/:id`, coaches images/certifications/time-slots sub-routes |
| **Bookings** | `POST /bookings`, `GET /bookings`, `GET /bookings/:id`, `PUT /bookings/:id/status`, `GET /bookings/mine`, `GET /bookings/partner` |
| **Time Slots** | `POST /time-slots/generate`, `PUT /time-slots/:id` |
| **Payments** | `POST /payments`, `GET /payments/booking/:bookingId` |
| **Reviews** | `POST /reviews`, `GET /reviews/venue/:id`, `GET /reviews/coach/:id`, `GET /reviews/mine` |
| **Discounts** | `POST /discounts`, `GET /discounts/:id`, `GET /discounts/venue/:id`, `GET /discounts/coach/:id` |
| **Notifications** | `GET /notifications`, `GET /notifications/unread-count`, `POST /notifications/mark-read`, `POST /notifications/device-token` |
| **Search** | `GET /search` |
| **Favorites** | `GET /favorites`, `POST /favorites/toggle`, `GET /favorites/check` |
| **Friends** | `GET /friends`, `GET /friends/requests`, `POST /friends/request`, `PUT /friends/request/:id/respond`, `DELETE /friends/:friendId`, `GET /friends/search-users` |
| **Direct Messages** | `GET /dm`, `GET /dm/unread-count`, `GET /dm/:friendUserId/messages`, `POST /dm/:friendUserId/messages`, `PUT /dm/:friendUserId/read` |
| **Parties** | `POST /parties`, `GET /parties/active`, `GET /parties/:id`, `POST /parties/:id/invite`, `POST /parties/:id/respond`, `POST /parties/:id/disband` |
| **Communities** | Community CRUD, membership, lobby management |
| **Matches** | Match CRUD, participants, chat, ratings, recurrence rules |
| **Available Players** | `GET /available-players`, `POST /available-players`, `DELETE /available-players/:sportType` |
| **Stripe Connect** | `POST /stripe-connect/onboard`, `GET /stripe-connect/status`, `GET /stripe-connect/dashboard-link` |
| **Dashboard** | `GET /dashboard/stats`, `GET /dashboard/reservations`, `GET /dashboard/billings`, `GET /dashboard/calendar` |
| **Admin** | `GET /admin/users`, `GET /admin/users/:id`, `GET /admin/bookings`, `GET /admin/analytics`, `GET /admin/venues`, `GET /admin/coaches` |
| **Subscription** | `GET /subscription`, `POST /subscription/checkout`, `POST /subscription/cancel`, `POST /subscription/reactivate`, `PUT /subscription/visibility` |
| **v2 Home** | `GET /home/feed` |
| **v2 Play** | `GET /play/search` |
| **v2 Rebook** | `POST /bookings/:id/rebook` |
| **v2 Split** | `POST /bookings/:id/split`, `GET /bookings/:id/split`, `POST /split-payments/:shareId/pay` |
| **v2 Participants** | `POST /bookings/:id/invite`, `POST /bookings/:id/respond`, `POST /bookings/:id/attendance` |

</details>

---

## Database Schema

58 versioned migrations covering:

| Migration range | Domain |
|----------------|--------|
| `0001`–`0016` | Core tables: users, venues, coaches, bookings, payments, reviews, time slots, availability templates |
| `0017`–`0021` | User interests: sports follow, bio, expertise, skill levels |
| `0022`–`0028` | Match system: matches, participants, chat, player ratings, recurrence |
| `0029`–`0036` | Social: notifications, device tokens, friendships, favorites, parties |
| `0037`–`0040` | Gamification: achievements, streaks, Stripe Connect accounts |
| `0041`–`0049` | v2 core: partner approval gates, communities, lobbies, venue booking lobbies, skill filters, listing approval |
| `0050`–`0058` | v2 finish: split payments, booking participants, partner reminders, SportsBooks+ subscription, triggers & constraints, direct messages |

All tables follow PostgreSQL conventions: `BIGSERIAL` primary keys, `TIMESTAMPTZ` timestamps, explicit foreign key behaviors, check constraints, and indexed foreign keys.

---

## Getting Started

### Prerequisites

- Node.js 18+
- PostgreSQL 15+
- Firebase project (Auth + Storage + Cloud Messaging)
- Stripe account (Connect + Billing)

### Backend

```bash
cd src/backend
cp .env.example .env       # fill in DATABASE_URL, FIREBASE_*, STRIPE_*
npm install
npm run dev
```

The API runs on `http://localhost:3000`.

> Set `DEV_AUTH_BYPASS=true` in `.env` to skip Firebase token validation during local development. Use raw Firebase UIDs as Bearer tokens.

### Web frontend

```bash
cd src/frontend
npm install
npm run dev
```

The web dashboard runs on `http://localhost:5173` and proxies API requests to the backend.

### Android

```bash
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

> Place `google-services.json` in `app/` (gitignored).

### iOS

```bash
cd src/ios
open SportsBooks.xcodeproj
# Build & run from Xcode
```

> Place `GoogleService-Info.plist` in the iOS app bundle (gitignored).

---

## Project Structure

```
SportsBooks/
├── app/                     # Android (Kotlin/Compose) — Gradle module
│   └── src/main/java/.../
│       ├── data/            # API service, DTOs, repositories
│       ├── di/              # Hilt modules (Network, Firebase, Repository)
│       ├── domain/          # Enums, models
│       └── ui/
│           ├── navigation/  # Routes, NavHost
│           └── screens/     # Player screens (home, booking, chat, match, profile)
├── src/
│   ├── backend/
│   │   └── src/
│   │       ├── config/      # Database, Firebase, logger
│   │       ├── controllers/ # Route handlers (30+ controllers)
│   │       ├── middleware/   # Auth, rate limiter
│   │       ├── repositories/ # Database queries
│   │       ├── routes/      # Express route definitions
│   │       └── services/    # Notification, email, Stripe
│   ├── frontend/
│   │   └── src/
│   │       ├── api/         # Typed API clients (axios)
│   │       ├── components/  # Layout (Sidebar, TopBar), UI (InboxBell, NotificationBell)
│   │       ├── context/     # Auth context
│   │       └── pages/       # Dashboard, Inbox, Venues, Coaches, Admin, v2 pages
│   ├── ios/                 # Swift/SwiftUI iOS app
│   └── shared/              # Shared TypeScript contracts
│       └── api/
│           └── endpoints.ts # All API path constants (used by backend + frontend)
├── database/
│   ├── schemas/             # SQL schema definitions
│   ├── migrations/          # 58 versioned migrations (NNNN_*.sql)
│   └── queries/             # Reusable SQL queries and procedures
└── docs/
    ├── diagrams.md          # Mermaid diagrams (architecture, flows, ER)
    └── images/              # SVG diagrams
```

---

## Security

- All secrets loaded from environment variables — no hardcoded keys
- Rate limiting on auth routes and API endpoints
- Firebase Auth tokens validated on every request
- Stripe webhooks verified with signing secret
- Friendship guard on DM endpoints — only accepted friends can message
- Listing approval gate — new venues/coaches require admin approval before going public
- All foreign keys use explicit `ON DELETE` / `ON UPDATE` rules
- `.env*`, `google-services.json`, `GoogleService-Info.plist`, and service account keys are gitignored

---

## Branch: v2-practical-ux

This branch consolidates the v2 UX improvements on top of the core platform. Key changes from `main`:

1. **Unified home feed** — single `/home/feed` endpoint runs 5 queries in parallel
2. **Unified play search** — `/play/search` ranks lobbies, slots, and players together
3. **One-tap rebook** — `POST /bookings/:id/rebook` with `timeSlotId` or `slotDate + startTime`
4. **Split payments** — equal or custom cost-sharing with expiration
5. **Booking participants** — invite friends, RSVP, attendance tracking
6. **Direct messaging** — friend-to-friend chat with push notifications and inbox UI
7. **SportsBooks+** — subscription tier with Stripe Billing and profile visibility
8. **Light-theme redesign** — Android app migrated to Material 3 light theme
9. **Partner dashboard** — reservations, analytics, earnings, calendar (v2)
10. **Admin panel** — listing approval queue, platform metrics, user management

---

## Documentation

- [`docs/diagrams.md`](docs/diagrams.md) — Mermaid diagrams (architecture, flows, ER, sequence)
- [`docs/slideshow.html`](docs/slideshow.html) — Pitch slideshow (open in any browser)

---

<div align="center">

**SportsBooks** · Find your court. Find your people. Just play.

</div>
