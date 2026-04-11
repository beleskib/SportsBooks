# SportsBooks — Visual Overview

All diagrams below are written in [Mermaid](https://mermaid.js.org/) and render automatically on GitHub. No images, no Figma — just commit the markdown.

---

## 1. System Architecture

```mermaid
graph TB
    subgraph Clients["📱 Clients"]
        Android["Android App<br/>(Kotlin · Compose)"]
        iOS["iOS App<br/>(Swift · SwiftUI)"]
        Web["Web Dashboard<br/>(React · Vite)"]
    end

    subgraph Backend["⚙️ Backend"]
        API["REST API<br/>(Node · Express · TS)"]
        Auth["Auth Middleware<br/>(Firebase Token)"]
        Limiter["Rate Limiter<br/>(5/15min auth · 100/15min api)"]
    end

    subgraph Data["💾 Data Layer"]
        PG[("PostgreSQL 16<br/>users · venues · coaches<br/>bookings · payments")]
        FBStore["Firebase Storage<br/>(images)"]
    end

    subgraph External["☁️ External Services"]
        FBAuth["Firebase Auth"]
        FCM["Firebase Cloud<br/>Messaging"]
        Stripe["Stripe Connect<br/>(payments + payouts)"]
    end

    Android --> Limiter
    iOS --> Limiter
    Web --> Limiter
    Limiter --> Auth
    Auth --> API
    API --> PG
    API --> FBStore
    API --> Stripe
    API --> FCM

    Android -.token.-> FBAuth
    iOS -.token.-> FBAuth
    Web -.token.-> FBAuth
    FBAuth -.verify.-> Auth

    FCM -.push.-> Android
    FCM -.push.-> iOS

    classDef client fill:#1e3a8a,stroke:#60a5fa,color:#fff
    classDef backend fill:#064e3b,stroke:#34d399,color:#fff
    classDef data fill:#451a03,stroke:#fbbf24,color:#fff
    classDef external fill:#3b0764,stroke:#a78bfa,color:#fff

    class Android,iOS,Web client
    class API,Auth,Limiter backend
    class PG,FBStore data
    class FBAuth,FCM,Stripe external
```

---

## 2. Player Journey

```mermaid
journey
    title Player — From Signup to Playing
    section Onboarding
      Sign up with email: 5: Player
      Pick role · sports · skill: 4: Player
    section Discover
      Browse sports tiles: 5: Player
      Filter venues by location: 4: Player
      View venue details + reviews: 5: Player
    section Book
      Pick a date and time slot: 5: Player
      Confirm booking: 5: Player
      Pay with Stripe: 4: Player
    section Play
      Receive push reminders: 5: Player
      Show up and play: 5: Player
      Rate venue and players: 4: Player
```

---

## 3. Partner Journey

```mermaid
journey
    title Partner — From Listing to Getting Paid
    section Setup
      Sign up as venue/coach: 5: Partner
      Complete profile: 4: Partner
      Connect Stripe account: 3: Partner
    section List
      Add photos and pricing: 5: Partner
      Open weekly time slots: 4: Partner
    section Operate
      Receive booking notification: 5: Partner
      Approve booking: 5: Partner
      Get paid via Stripe: 5: Partner
    section Grow
      Track revenue analytics: 5: Partner
      Reply to reviews: 4: Partner
      Run discounts: 5: Partner
```

---

## 4. Booking Flow (Sequence)

```mermaid
sequenceDiagram
    autonumber
    participant P as Player
    participant App as Mobile App
    participant API as Backend API
    participant DB as PostgreSQL
    participant Stripe
    participant Partner

    P->>App: Tap "Book this slot"
    App->>API: POST /api/bookings (slotId)
    API->>DB: Insert booking (status: pending)
    API->>App: 201 { bookingId }
    App->>API: POST /api/payments/create-intent
    API->>Stripe: Create PaymentIntent
    Stripe-->>API: client_secret
    API-->>App: { client_secret }
    App->>Stripe: Confirm payment
    Stripe-->>App: payment_succeeded
    App->>API: POST /api/bookings/:id/pay
    API->>DB: Update booking (status: paid_pending_approval)
    API->>Partner: 🔔 Push notification

    Partner->>API: POST /api/bookings/:id/approve
    API->>DB: status: confirmed + close time slot
    API->>P: 🔔 "Booking confirmed"

    Note over P,Partner: Match day
    P->>Partner: Show up and play 🎾
```

---

## 5. Matchmaking Lobby Flow

```mermaid
flowchart TD
    Start([Player wants to play<br/>but needs more people]) --> CreateLobby[Create Venue Booking Lobby]
    CreateLobby --> PickType{Choose payment type}

    PickType -->|Split| Split[Everyone pays equal share]
    PickType -->|Creator pays| Creator[Creator covers everyone]
    PickType -->|Team split| Teams[Two teams · leaders pay]

    Split --> Open[Lobby is open · others can join]
    Creator --> Open
    Teams --> Open

    Open --> Join{Lobby<br/>full?}
    Join -->|No| Open
    Join -->|Yes| Notify[🔔 Notify all members<br/>'Lobby is full']

    Notify --> Submit[Creator submits booking]
    Submit --> Approve{Partner approves?}
    Approve -->|No| Cancelled([❌ Lobby cancelled<br/>refunds issued])
    Approve -->|Yes| Pay[Trigger payment for<br/>responsible parties]

    Pay --> WhoPays{Payment type}
    WhoPays -->|Split| AllPay[All members pay]
    WhoPays -->|Creator pays| OnePay[Creator pays in full]
    WhoPays -->|Team split| LeadsPay[Team leaders pay]

    AllPay --> Confirm
    OnePay --> Confirm
    LeadsPay --> Confirm[✅ Lobby confirmed<br/>time slot closed]
    Confirm --> Play([🏀 Match day])

    classDef start fill:#064e3b,stroke:#34d399,color:#fff
    classDef done fill:#064e3b,stroke:#34d399,color:#fff
    classDef cancel fill:#7f1d1d,stroke:#f87171,color:#fff
    classDef decision fill:#1e3a8a,stroke:#60a5fa,color:#fff

    class Start start
    class Play,Confirm done
    class Cancelled cancel
    class PickType,Join,Approve,WhoPays decision
```

---

## 6. Domain Model (ER Diagram)

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "makes"
    USERS ||--o{ VENUES : "owns (partner)"
    USERS ||--o{ COACHES : "is (partner)"
    USERS ||--o{ REVIEWS : "writes"
    USERS ||--o{ FRIENDSHIPS : "has"
    USERS ||--o{ NOTIFICATIONS : "receives"

    VENUES ||--o{ TIME_SLOTS : "has"
    COACHES ||--o{ TIME_SLOTS : "has"
    VENUES ||--o{ REVIEWS : "receives"
    COACHES ||--o{ REVIEWS : "receives"

    TIME_SLOTS ||--o| BOOKINGS : "books"
    BOOKINGS ||--o| PAYMENTS : "has"
    BOOKINGS ||--o{ BOOKING_CHAT : "discusses"

    VENUE_BOOKING_LOBBIES ||--o{ LOBBY_MEMBERS : "contains"
    VENUE_BOOKING_LOBBIES ||--o{ LOBBY_TEAMS : "contains"
    VENUE_BOOKING_LOBBIES ||--o| BOOKINGS : "creates"

    USERS {
        bigint id PK
        text firebase_uid
        text email
        text display_name
        text role "player or partner"
        int xp
        timestamptz created_at
    }

    VENUES {
        bigint id PK
        bigint owner_id FK
        text name
        text sport_type
        numeric hourly_price
        text location
    }

    TIME_SLOTS {
        bigint id PK
        bigint venue_id FK
        bigint coach_id FK
        date slot_date
        time start_time
        boolean is_available
    }

    BOOKINGS {
        bigint id PK
        bigint user_id FK
        bigint time_slot_id FK
        text status "pending → confirmed → completed"
        numeric total_price
    }

    PAYMENTS {
        bigint id PK
        bigint booking_id FK
        text stripe_payment_intent_id
        text status
    }

    VENUE_BOOKING_LOBBIES {
        bigint id PK
        bigint creator_id FK
        bigint time_slot_id FK
        text payment_type "split | creator_pays | split_to_teams"
        text status "open | full | confirmed"
        int max_players
    }
```

---

## 7. Auth & Rate Limiting

```mermaid
sequenceDiagram
    participant Client
    participant Limiter as Rate Limiter
    participant Auth as Auth Middleware
    participant Firebase
    participant Controller

    Client->>Limiter: Request /api/...
    alt Auth route (POST /api/auth/*)
        Limiter->>Limiter: Check IP · max 5 / 15 min
    else Other API route
        Limiter->>Limiter: Check IP · max 100 / 15 min
    end

    alt Limit exceeded
        Limiter-->>Client: 429 Too Many Requests
    else Within limit
        Limiter->>Auth: Forward request
        Auth->>Firebase: Verify Bearer token
        alt Invalid token
            Auth-->>Client: 401 Unauthorized
        else Valid token
            Firebase-->>Auth: { uid }
            Auth->>Auth: Lookup user in PostgreSQL
            Auth->>Controller: req.user = { id, role }
            Controller-->>Client: 200 + data
        end
    end
```

---

## 8. Tech Stack at a Glance

```mermaid
mindmap
  root((SportsBooks))
    Mobile
      Android
        Kotlin
        Jetpack Compose
        Hilt DI
        Retrofit
      iOS
        Swift
        SwiftUI
        URLSession
    Web
      React 18
      TypeScript
      Vite
      TailwindCSS
    Backend
      Node.js
      Express
      TypeScript
      Zod validation
    Data
      PostgreSQL 16
      Firebase Storage
    Cloud
      Firebase Auth
      Firebase FCM
      Stripe Connect
```
