<div align="center">

# 🔄 RentLoop

### Peer-to-Peer Rental Marketplace for the University Community

[![Java CI with Maven](https://github.com/manthansharma6767/RentLoop-Backend/actions/workflows/maven.yml/badge.svg)](https://github.com/manthansharma6767/RentLoop-Backend/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=flat&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat&logo=react&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=flat&logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-Academic-blue?style=flat)

**RentLoop** is a full-stack, category-agnostic rental platform that lets university students list, discover, request, and rent virtually any item — from laptops and cameras to bicycles and musical instruments. Built with **Spring Boot** on the backend and **React + Vite** on the frontend, it features real-time chat, intelligent request-to-listing matching, a controlled booking lifecycle, and a trust-building review system.

[Features](#-key-features) · [Architecture](#-system-architecture) · [Tech Stack](#-technology-stack) · [Getting Started](#-getting-started) · [API Reference](#-api-reference) · [Roadmap](#-development-roadmap)

</div>

---

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 🔐 Authentication & Security
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (`USER` / `ADMIN`)
- Protected REST endpoints
- Spring Security integration

</td>
<td width="50%">

### 📦 Item & Listing Management
- Create, update, and delete rental listings
- Category-based organization with extensible attributes
- Item condition tracking & security deposits
- Multi-image upload via Cloudinary
- Location-aware listings (Google Maps)

</td>
</tr>
<tr>
<td width="50%">

### 🔍 Rental Requests & Matching
- Open rental requests for unmet demand
- Intelligent matching engine (category, budget, location, dates)
- Reverse-demand workflow connecting renters to owners
- Status lifecycle management

</td>
<td width="50%">

### 📅 Booking Lifecycle
- State-based workflow: `REQUESTED → CONFIRMED → ACTIVE → RETURNED`
- Cancellation support
- Date-overlap & availability validation
- Booking history tracking

</td>
</tr>
<tr>
<td width="50%">

### 💬 Real-Time Chat
- WebSocket + STOMP protocol
- Persistent conversations
- Rental-contextualized messaging
- Conversation history retrieval

</td>
<td width="50%">

### ⭐ Ratings & Reviews
- Bidirectional reviews (renter ↔ owner)
- Duplicate-prevention per booking
- Trust & reputation scoring
- Review aggregation per user

</td>
</tr>
<tr>
<td colspan="2">

### 🔔 Notifications
Real-time, in-app notifications for booking requests, confirmations, cancellations, rental returns, matching alerts, and new reviews.

</td>
</tr>
</table>

---

## 🧬 Category-Agnostic Item Model

RentLoop uses a single, flexible `Item` entity with extensible attributes — **no schema changes needed** when adding new item types.

```
Dell Laptop                     Yamaha Guitar
├── RAM: 16GB                   ├── Type: Acoustic
├── Storage: 512GB              ├── Strings: 6
└── Processor: Intel i7         └── Brand: Yamaha
```

Supported categories include laptops, tablets, gaming consoles, cameras, musical instruments, printers, bicycles, clothing, sports equipment, and more.

---

## 🏗 System Architecture

```
                    ┌──────────────────────────┐
                    │     React + Vite (SPA)    │
                    │    TailwindCSS · Axios    │
                    └────────────┬─────────────┘
                                 │
                          REST / WebSocket
                                 │
                                 ▼
              ┌──────────────────────────────────┐
              │         Spring Boot 4.1.1        │
              │                                  │
              │  Controllers → Services → Repos  │
              │       JPA / Hibernate ORM        │
              │    Spring Security + JWT Auth     │
              │     WebSocket + STOMP Chat       │
              └────────────────┬─────────────────┘
                               │
                               ▼
                        ┌─────────────┐
                        │  MySQL 8.4  │
                        └─────────────┘

              ┌────────────┐      ┌──────────────┐
              │ Cloudinary │      │ Google Maps  │
              │  (Images)  │      │  (Location)  │
              └────────────┘      └──────────────┘

         Planned ─────────────────────────────────
              ┌──────────┐      ┌──────────┐
              │  Redis   │      │  Kafka   │
              │  Cache   │      │  Events  │
              └──────────┘      └──────────┘
```

---

## 🛠 Technology Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 4.1.1 | Application framework |
| Spring Security | — | Authentication & authorization |
| JJWT | 0.12.5 | JWT token management |
| Spring Data JPA | — | Data access layer |
| Hibernate | — | Object-relational mapping |
| Spring WebSocket | — | Real-time communication (STOMP) |
| Jakarta Validation | — | Request validation |
| Lombok | — | Boilerplate reduction |
| MySQL Connector/J | — | Database driver |
| Maven | — | Build & dependency management |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| React | 19 | UI library |
| Vite | 8 | Build tool & dev server |
| React Router | 7 | Client-side routing |
| Axios | 1.20 | HTTP client |
| Tailwind CSS | 4 | Utility-first styling |
| Lucide React | 1.48 | Icon library |

### External Services

| Service | Purpose |
|---|---|
| Cloudinary | Image storage & CDN |
| Google Maps / Geocoding | Location services |

### CI/CD

| Tool | Purpose |
|---|---|
| GitHub Actions | Automated build & test on push/PR to `main` |
| MySQL 8.4 (service container) | Integration testing in CI |

---

## 📂 Project Structure

```
RentLoop-Backend/
│
├── src/main/java/com/manthan/rentloop/
│   ├── RentloopApplication.java        # Entry point
│   ├── configuration/
│   │   ├── SecurityConfig.java         # Spring Security & CORS
│   │   └── WebSocketConfig.java        # WebSocket/STOMP setup
│   ├── controller/
│   │   ├── AuthController.java         # Login & registration
│   │   ├── UserController.java         # User profiles
│   │   ├── CategoryController.java     # Category CRUD
│   │   ├── ItemController.java         # Item management
│   │   ├── ListingController.java      # Listing CRUD & search
│   │   ├── BookingController.java      # Booking lifecycle
│   │   ├── RentalRequestController.java# Rental requests
│   │   ├── ChatController.java         # Real-time messaging
│   │   ├── ReviewController.java       # Ratings & reviews
│   │   └── NotificationController.java # User notifications
│   ├── service/                        # Business logic layer
│   ├── repository/                     # Spring Data JPA repos
│   ├── model/                          # JPA entities & enums
│   ├── dto/                            # Request/response DTOs
│   ├── security/                       # JWT filter & utilities
│   └── exception/                      # Global exception handler
│
├── rentloop-frontend/
│   └── src/
│       ├── App.jsx                     # Route definitions
│       ├── pages/                      # Dashboard, Rent, Requests...
│       ├── components/                 # Navbar, Login, Register...
│       ├── context/                    # Auth context provider
│       └── api/                        # Axios API layer
│
├── .github/workflows/maven.yml        # CI pipeline
├── .env.example                        # Environment variable template
├── pom.xml                             # Maven configuration
└── postman/                            # Postman collections
```

---

## 🗃 Domain Model

```
User
 │
 ├── Item ──→ ItemAttribute
 │    │
 │    └── Listing ──→ ListingImage
 │         │
 │         └── Booking
 │              ├── Review
 │              └── Conversation ──→ ChatMessage
 │
 ├── RentalRequest ──→ RequestMatch
 │
 └── Notification
```

**Core Entities:** `User` · `Category` · `Item` · `ItemAttribute` · `Listing` · `ListingImage` · `Booking` · `RentalRequest` · `RequestMatch` · `Conversation` · `ChatMessage` · `Review` · `Notification`

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17+ |
| Maven | 3.9+ |
| MySQL | 8.0+ |
| Node.js | 18+ |
| Git | Any |

```bash
# Verify installations
java -version
mvn -version
mysql --version
node --version
git --version
```

### 1. Clone the Repository

```bash
git clone https://github.com/manthansharma6767/RentLoop-Backend.git
cd RentLoop-Backend
```

### 2. Database Setup

```sql
CREATE DATABASE rentloop_db;
```

### 3. Configure Environment Variables

Copy the example file and fill in your values:

```bash
cp .env.example .env
```

```env
# Database
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# JWT
JWT_SECRET=your_base64_encoded_secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Google Maps
GOOGLE_MAPS_API_KEY=your_google_maps_api_key
```

> ⚠️ **Never commit real credentials.** The `.env` file is excluded via `.gitignore`.

### 4. Run the Backend

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

The API server starts at `http://localhost:8080`.

### 5. Run the Frontend

```bash
cd rentloop-frontend
npm install
npm run dev
```

The dev server starts at `http://localhost:5173`.

---

## 📡 API Reference

All endpoints are prefixed with `/api`.

| Module | Endpoint | Description |
|---|---|---|
| **Auth** | `POST /api/auth/register` | User registration |
| | `POST /api/auth/login` | Login & JWT issuance |
| **Users** | `GET /api/users/profile` | Get user profile |
| | `PUT /api/users/profile` | Update profile |
| **Categories** | `GET /api/categories` | List categories |
| | `POST /api/categories` | Create category (Admin) |
| **Items** | `POST /api/items` | Create item |
| | `GET /api/items` | List user's items |
| **Listings** | `POST /api/listings` | Create listing |
| | `GET /api/listings` | Search & filter listings |
| | `PUT /api/listings/{id}` | Update listing |
| | `DELETE /api/listings/{id}` | Delete listing |
| **Bookings** | `POST /api/bookings` | Request booking |
| | `PUT /api/bookings/{id}/confirm` | Confirm booking |
| | `PUT /api/bookings/{id}/cancel` | Cancel booking |
| | `PUT /api/bookings/{id}/return` | Return item |
| **Rental Requests** | `POST /api/rental-requests` | Create request |
| | `GET /api/rental-requests` | List requests |
| | `PUT /api/rental-requests/{id}` | Update request |
| **Chat** | `WS /ws` | WebSocket endpoint |
| | `GET /api/conversations` | List conversations |
| | `GET /api/messages/{conversationId}` | Get messages |
| **Reviews** | `POST /api/ratings` | Submit review |
| | `GET /api/ratings/user/{id}` | Get user reviews |
| **Notifications** | `GET /api/notifications` | Get notifications |
| | `PUT /api/notifications/{id}/read` | Mark as read |

> 📬 A **Postman collection** is included in the `postman/` directory for quick API testing.

---

## 🗺 Development Roadmap

| Phase | Module | Status |
|:---:|---|:---:|
| 1 | Project Setup & Configuration | ✅ |
| 2 | Database Foundation (User, Category, Item, ItemAttribute) | ✅ |
| 3 | Authentication & Security (JWT, BCrypt, Spring Security) | ✅ |
| 4 | User & Category APIs | ✅ |
| 5 | Items & Listings | ✅ |
| 6 | Availability & Search | ✅ |
| 7 | Rental Requests | ✅ |
| 8 | Matching Engine | ✅ |
| 9 | Booking Lifecycle | ✅ |
| 10 | Real-Time Chat (WebSocket + STOMP) | ✅ |
| 11 | Notifications | ✅ |
| 12 | Ratings & Reviews | ✅ |
| 13 | React Frontend (Dashboard, Rent, Requests, Owner Panel) | ✅ |
| 14 | CI/CD Pipeline (GitHub Actions) | ✅ |
| 15 | Administration Panel | 🔜 |
| 16 | Redis (Caching, Rate Limiting, Booking Locks) | 🔜 |
| 17 | Kafka (Event-Driven Processing) | 🔜 |
| 18 | Swagger / OpenAPI Documentation | 🔜 |
| 19 | Comprehensive Test Suite | 🔜 |

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Windows
.\mvnw.cmd test
```

The CI pipeline automatically runs tests on every push and pull request to `main`, using a MySQL 8.4 service container for integration testing.

---

## 🔒 Security Practices

- Passwords hashed with **BCrypt** — never stored in plain text
- **JWT secrets** stored as environment variables, never in source code
- All protected endpoints require a valid `Authorization: Bearer <token>` header
- Admin-only operations gated by `ADMIN` role
- Ownership validation on update/delete operations
- Input validated at API boundaries via **Jakarta Validation**
- Centralized exception handling with meaningful HTTP status codes

---

## 🌱 Future Enhancements

| Feature | Description |
|---|---|
| 💳 Payment Gateway | Integrated rental payments |
| 🔒 Escrow Deposits | Secure security deposit handling |
| 🤖 AI-Powered Search | Smart item recommendations |
| 📸 Vision Categorization | Auto-categorize items from photos |
| 🚚 Delivery Integration | Logistics for item handoff |
| 📱 Mobile App | Native Android/iOS client |
| 📊 Dynamic Pricing | Demand-based rental pricing |
| 🛡️ Rental Insurance | Coverage for rented items |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit with meaningful messages (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Commit Convention

```
feat:     New feature
fix:      Bug fix
docs:     Documentation changes
style:    Code formatting
refactor: Code restructuring
test:     Adding/updating tests
chore:    Build/config changes
```

---

## 👨‍💻 Author

**Manthan Sharma**

Department of Computer Science and Engineering
Ramdeobaba University

[![GitHub](https://img.shields.io/badge/GitHub-manthansharma6767-181717?style=flat&logo=github)](https://github.com/manthansharma6767)

---

## 📄 License

This project is developed for **academic and educational purposes** at Ramdeobaba University.

A production deployment license and usage policy will be defined separately if the platform is released publicly.

---

<div align="center">

**Built with ❤️ at Ramdeobaba University**

</div>
