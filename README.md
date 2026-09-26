# RentLoop

### Peer-to-Peer Rental Platform for the University Community

RentLoop is a web-based peer-to-peer rental platform designed initially for the university community. It enables users to list items for rent, discover available items, submit rental requests when an item is unavailable, communicate with owners, manage bookings, and build trust through ratings and reviews.

The platform is designed to be **category-agnostic**, allowing users to rent a wide range of items such as laptops, tablets, gaming consoles, cameras, musical instruments, printers, bicycles, clothing, sports equipment, and more.

---

## Overview

Traditional classified platforms are primarily designed for buying and selling, while many rental platforms focus on a specific category such as vehicles, fashion, or electronics.

RentLoop addresses this limitation by providing a unified rental marketplace where:

* Owners can list virtually any item for rent.
* Renters can search and filter available listings.
* Renters can create rental requests when a suitable listing does not exist.
* The platform can match rental requests with relevant listings.
* Owners and renters can communicate through real-time chat.
* Rentals follow a controlled booking lifecycle.
* Users can rate and review each other after completed rentals.

The initial target audience is the **RBU/university community**, with the architecture designed to be extensible to a broader user base.

---

## Key Features

### Authentication & Authorization

* User registration and login
* JWT-based authentication
* BCrypt password hashing
* Role-based authorization
* USER and ADMIN roles
* Protected REST APIs

### Item & Listing Management

* Create rental listings
* Update and delete listings
* Category-based organization
* Item condition
* Rental pricing
* Security deposits
* Item availability
* Location information
* Image support

### Category-Agnostic Item Model

RentLoop does not create separate database tables for every item type.

A single generic `Item` model can represent:

```text
Laptop
Tablet
PS5
Camera
Guitar
Printer
Bicycle
Shirt
Sports Equipment
...
```

Category-specific information is handled using an extensible attribute model.

For example:

```text
Dell Laptop
├── RAM: 16GB
├── Storage: 512GB
└── Processor: Intel i7
```

and:

```text
Yamaha Guitar
├── Type: Acoustic
├── Strings: 6
└── Brand: Yamaha
```

This allows new item types to be introduced without changing the database schema.

---

## Rental Request & Matching

If a renter cannot find a suitable listing, they can create an open rental request.

Example:

```text
Item: PS5
Budget: ₹700/day
Duration: 3 days
Location: Near RBU
Required Dates: 10–13 October
```

The matching engine can compare rental requests against available listings using factors such as:

* Category
* Item/name
* Location
* Budget
* Availability
* Required dates

This creates a reverse-demand workflow where renters can signal unmet demand to potential owners.

---

## Booking Lifecycle

Bookings follow a controlled state-based workflow:

```text
REQUESTED
    ↓
CONFIRMED
    ↓
ACTIVE
    ↓
RETURNED
```

Cancellation is also supported where applicable.

The backend validates booking state transitions and checks item availability before confirming a rental.

---

## Real-Time Communication

RentLoop uses **Spring WebSocket with STOMP** for real-time communication between renters and owners.

The chat system supports:

* Conversations
* Real-time messages
* Message persistence
* Rental-related communication
* Retrieval of previous conversations

---

## Notifications

The notification system can inform users about important platform events, including:

* New rental requests
* Matching listings
* Booking requests
* Booking confirmations
* Booking cancellations
* Rental returns
* Ratings and reviews
* Relevant messages

The initial implementation uses application/database-backed notifications, with Kafka planned for event-driven processing.

---

## Ratings & Reviews

After a rental is completed:

* Renters can rate owners.
* Owners can rate renters.
* Reviews can be submitted.
* Duplicate ratings for the same booking are prevented.

The rating system contributes to trust and reputation within the marketplace.

---

# System Architecture

RentLoop is designed as a **modular monolith** using a layered Spring Boot architecture.

```text
                    ┌─────────────────────┐
                    │       Client        │
                    │   React Frontend    │
                    └──────────┬──────────┘
                               │
                        REST / WebSocket
                               │
                               ▼
              ┌────────────────────────────────┐
              │        Spring Boot API         │
              │                                │
              │  Controllers                   │
              │       ↓                        │
              │  Services                      │
              │       ↓                        │
              │  Repositories                  │
              │       ↓                        │
              │  JPA / Hibernate               │
              └───────────────┬────────────────┘
                              │
                              ▼
                       ┌─────────────┐
                       │    MySQL    │
                       └─────────────┘

        External / Supporting Services

        ┌────────────┐  ┌────────────┐
        │ Cloudinary │  │ Google Maps│
        │   Images   │  │  Location  │
        └────────────┘  └────────────┘

        Advanced Infrastructure

              ┌──────────┐
              │  Redis   │
              │ Cache /  │
              │  Locks   │
              └──────────┘

              ┌──────────┐
              │  Kafka   │
              │  Events  │
              └──────────┘
```

---

# Technology Stack

## Backend

| Technology         | Purpose                        |
| ------------------ | ------------------------------ |
| Java 21            | Programming language           |
| Spring Boot        | Backend framework              |
| Spring Web         | REST APIs                      |
| Spring Security    | Authentication & authorization |
| JWT                | Stateless authentication       |
| BCrypt             | Password hashing               |
| Spring Data JPA    | Data access                    |
| Hibernate          | ORM                            |
| MySQL              | Relational database            |
| Jakarta Validation | Request validation             |
| WebSocket / STOMP  | Real-time communication        |
| Maven              | Dependency management          |
| Swagger / OpenAPI  | API documentation              |
| JUnit              | Testing                        |
| Mockito            | Unit testing                   |

## External Services

| Service                 | Purpose           |
| ----------------------- | ----------------- |
| Cloudinary              | Image storage     |
| Google Maps / Geocoding | Location services |

## Advanced Infrastructure

| Technology   | Purpose                                                  |
| ------------ | -------------------------------------------------------- |
| Redis        | Caching, rate limiting, temporary data and booking locks |
| Apache Kafka | Event-driven communication and asynchronous processing   |

Redis and Kafka will be introduced after the core application is stable rather than adding unnecessary infrastructure during initial development.

---

# Backend Architecture

The backend follows a layered structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```

Recommended package structure:

```text
com.rentloop
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── security
├── config
├── exception
└── util
```

### Architectural Principles

* Separation of concerns
* SOLID principles
* DTO-based API design
* Centralized exception handling
* Validation at API boundaries
* Transaction management
* Role-based authorization
* Database constraints
* Meaningful HTTP status codes
* Modular and maintainable code

---

# Core Domain Model

The backend is centered around the following entities:

```text
User
Category
Item
ItemAttribute
Listing
Availability
RentalRequest
Booking
Conversation
Message
Rating
Notification
```

High-level relationship:

```text
User
 │
 ├───────────────┐
 │               │
 ▼               ▼
Item          RentalRequest
 │
 ▼
Listing
 │
 ▼
Booking
 │
 ├───────────────┐
 ▼               ▼
Conversation    Rating
 │
 ▼
Message
```

---

# Project Development Roadmap

The backend is being developed incrementally.

### Phase 1 — Project Setup

* Spring Boot setup
* Maven configuration
* Java 21
* MySQL configuration
* Package structure
* Basic API health check

### Phase 2 — Database Foundation

* User
* Category
* Item
* ItemAttribute
* JPA relationships

### Phase 3 — Authentication & Security

* Registration
* Login
* JWT
* BCrypt
* Spring Security
* Roles

### Phase 4 — User & Category APIs

* User profiles
* Category management
* Admin authorization

### Phase 5 — Items & Listings

* Item management
* Item attributes
* Listing management
* Images
* Ownership validation

### Phase 6 — Availability & Search

* Availability management
* Date-overlap validation
* Search
* Filtering
* Pagination
* Sorting

### Phase 7 — Rental Requests

* Create requests
* Update requests
* Cancel requests
* Request status management

### Phase 8 — Matching Engine

* Listing/request matching
* Category matching
* Location matching
* Budget matching
* Availability matching

### Phase 9 — Booking

* Booking requests
* Confirmation
* Cancellation
* Rental activation
* Return workflow
* Booking history

### Phase 10 — Real-Time Chat

* Conversations
* Messages
* WebSocket
* STOMP
* Message persistence

### Phase 11 — Notifications

* Booking notifications
* Matching notifications
* Rental notifications
* User notifications

### Phase 12 — Ratings & Reviews

* Owner ratings
* Renter ratings
* Reviews
* Reputation

### Phase 13 — Administration

* User management
* Listing moderation
* Category management
* Basic statistics

### Phase 14 — Testing & Documentation

* Unit tests
* Integration tests
* Security tests
* Booking tests
* Matching tests
* Swagger/OpenAPI

### Phase 15 — Redis

* Listing caching
* Category caching
* Rate limiting
* Temporary data
* Booking locks

### Phase 16 — Kafka

* Domain events
* Booking events
* Matching events
* Notification events
* Asynchronous processing

---

# API Structure

The REST API is organized by domain:

```text
/api/auth
/api/users
/api/categories
/api/items
/api/listings
/api/availability
/api/rental-requests
/api/bookings
/api/conversations
/api/messages
/api/ratings
/api/notifications
/api/admin
```

API documentation will be available through Swagger/OpenAPI.

---

# Security

RentLoop uses Spring Security with JWT-based authentication.

Security principles include:

* Passwords are never stored in plain text.
* Passwords are hashed using BCrypt.
* JWT secrets are stored outside source code.
* Protected APIs require authentication.
* Administrative APIs require ADMIN authorization.
* Users can only modify resources they are authorized to modify.
* Input validation is applied to API requests.

---

# Project Status

> **Status: In Development**

The project is being developed incrementally, with each backend module implemented, tested, reviewed, and integrated before moving to the next phase.

### Current Development

```text
[ ] Phase 1 — Project Setup
[ ] Phase 2 — Database Foundation
[ ] Phase 3 — Authentication & Security
[ ] Phase 4 — User & Category APIs
[ ] Phase 5 — Items & Listings
[ ] Phase 6 — Availability & Search
[ ] Phase 7 — Rental Requests
[ ] Phase 8 — Matching Engine
[ ] Phase 9 — Booking
[ ] Phase 10 — WebSocket Chat
[ ] Phase 11 — Notifications
[ ] Phase 12 — Ratings & Reviews
[ ] Phase 13 — Administration
[ ] Phase 14 — Testing & Documentation
[ ] Phase 15 — Redis
[ ] Phase 16 — Kafka
```

This checklist should be updated as development progresses.

---

# Getting Started

## Prerequisites

Install the following:

* JDK 21
* Maven
* MySQL
* Git

Verify installations:

```bash
java -version
mvn -version
mysql --version
git --version
```

---

## Clone the Repository

```bash
git clone https://github.com/<your-username>/rentloop.git
cd rentloop
```

---

## Database Setup

Create the database:

```sql
CREATE DATABASE rentloop_db;
```

Configure the application using environment variables or local configuration.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rentloop_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Do not commit real credentials to GitHub.

---

# Environment Variables

Example:

```text
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
GOOGLE_MAPS_API_KEY=
```

Keep secrets outside version control.

Add local environment/configuration files to `.gitignore`.

---

# Running the Backend

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Or build the application:

```bash
./mvnw clean package
```

---

# Testing

The project uses:

* JUnit
* Mockito
* Spring Boot Test

Run tests:

```bash
./mvnw test
```

API testing can be performed using Postman.

---

# API Documentation

Swagger/OpenAPI will be used to document and test the REST API.

Once enabled, the Swagger UI will be available through the application's configured Swagger endpoint.

---

# Git Workflow

Recommended development workflow:

```text
main
 │
 ├── develop
 │     │
 │     ├── feature/auth
 │     ├── feature/listings
 │     ├── feature/bookings
 │     └── feature/chat
```

Use meaningful commit messages.

Examples:

```text
feat: add user registration
feat: implement JWT authentication
feat: add item and category entities
feat: implement listing creation
feat: add rental request matching
feat: implement booking workflow
feat: add websocket chat
fix: prevent overlapping bookings
test: add booking service tests
docs: update API documentation
```

---

# Future Enhancements

Potential future features include:

* Integrated payment gateway
* Escrow-style security deposits
* AI-assisted search
* Personalized recommendations
* Computer-vision-based item categorization
* Delivery/logistics integration
* Rental insurance
* Dynamic pricing
* Native mobile application

These features are outside the initial MVP and will only be considered after the core platform is stable.

---

# Project Objectives

The project aims to demonstrate practical implementation of:

* REST API development
* Spring Boot
* Spring Security
* JWT authentication
* Relational database design
* JPA/Hibernate
* Real-time communication
* Business-rule implementation
* Search and matching
* Booking and availability management
* Event-driven architecture
* Caching
* Automated testing
* API documentation
* Software engineering practices

---

# Contributors

**RentLoop — RBU Project**

* Manthan Sharma
* Kartikeya Trivedi
* Aryan Rajendrakumar Kanade
* Devesh Vijay Kahar

Department of Computer Science and Engineering
Ramdeobaba University

---

# License

This project is developed for academic and educational purposes.

A production deployment license and usage policy can be defined separately if the platform is released publicly.
