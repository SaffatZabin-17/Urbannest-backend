# UrbanNest Backend

A real estate platform backend built with Spring Boot, providing RESTful APIs for property listings, blogs, user management, and media storage. Deployed on AWS using a fully automated CI/CD pipeline.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| ORM | Hibernate 7.2 / Spring Data JPA |
| Database | PostgreSQL 17 (AWS RDS) |
| Migrations | Flyway |
| Authentication | Firebase Admin SDK |
| File Storage | AWS S3 (Presigned URLs) |
| Containerization | Docker (multi-stage build) |
| CI/CD | GitHub Actions |
| Container Registry | AWS ECR |
| Hosting | AWS EC2 (t2.micro) |
| Reverse Proxy | Nginx |
| SSL | Let's Encrypt (Certbot) |
| DNS | Porkbun |

## Architecture

```
Client (HTTPS)
    │
    ▼
Nginx (port 443, SSL termination)
    │
    ▼
Spring Boot (port 8080)
    │
    ├──▶ PostgreSQL (AWS RDS)
    ├──▶ AWS S3 (file storage)
    └──▶ Firebase Auth (token verification)
```

### Request Flow

```
HTTP Request
    │
    ▼
FirebaseAuthFilter ──▶ Verify JWT token
    │
    ▼
SecurityConfig ──▶ Check route authorization
    │
    ▼
Controller ──▶ Service ──▶ Repository ──▶ PostgreSQL
```

## Project Structure

```
src/main/java/com/example/urbannest/
├── config/             # Spring configuration (Security, Firebase, S3)
├── controller/         # REST API endpoints
├── dto/
│   ├── Requests/       # Incoming request DTOs
│   └── Responses/      # Outgoing response DTOs
├── exception/          # Custom exceptions and global handler
├── model/
│   ├── compositekeys/  # JPA composite key classes
│   └── enums/          # Domain enums
├── repository/         # Spring Data JPA repositories
├── security/           # Firebase auth filter
├── service/            # Business logic
└── util/               # Utility classes (hashing)
```

## API Endpoints

All endpoints are prefixed with `/api`.

### Public Endpoints (no authentication required)

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Health check |
| POST | `/users` | Register a new user |
| GET | `/listings/**` | Browse property listings |
| GET | `/blogs/**` | Browse blog posts |

### Protected Endpoints (Firebase JWT required)

| Method | Path | Description |
|---|---|---|
| GET | `/users/me` | Get authenticated user profile |
| PATCH | `/users/me` | Update user profile |
| POST | `/s3/upload-request` | Get presigned URL for file upload |
| GET | `/s3/download-url?key=` | Get presigned URL for file download |
| DELETE | `/s3?key=` | Delete file from S3 |

## Database Schema

### Entities

**User** - Platform users with Firebase authentication
- Fields: userId, firebaseId, name, email, phone, nidHash (SHA-512), profilePictureUrl, roleName, timestamps

**Listing** - Property listings
- Fields: listingId, user (FK), propertyType, propertyStatus, title, description, pricing, timestamps
- Related: ListingDetails (1:1), ListingLocation (1:1), ListingCounters (1:1), ListingPriceHistory (1:N), ListingMedia (M:N with MediaAsset)

**ListingDetails** - Property specifications
- Fields: yearBuilt, listingCondition, facingDirection, bedroomsCount, bathroomsCount, balconiesCount, floorLevel, furnished, parkingArea, petFriendly, lotArea, livingArea

**ListingLocation** - Geolocation data
- Fields: addressLine, area, district, zipCode, latitude, longitude

**ListingCounters** - Engagement metrics
- Fields: viewCount, favoriteCount, saveCount

**Blog** - Blog posts with JSONB content
- Fields: blogId, author (FK), title, content (JSONB), status, timestamps
- Related: BlogMedia (M:N with MediaAsset), BlogVotes, Comments

**Comment** - Nested comments on blogs
- Fields: commentId, author (FK), blog (FK), body (JSONB), parentComment (self-referencing FK), timestamps

**MediaAsset** - S3 file references
- Fields: mediaId, ownerUser (FK), s3Location, contentType, byteSize, caption, metadata (JSONB), timestamps

**Notification** - User notifications
- Fields: notificationId, recipientUser (FK), type, payload (JSONB), entityType, entityId, readAt, timestamps

**Junction Tables**: FavoriteListing, SavedListing, FavoriteBlog, SavedBlog, ListingMedia, BlogMedia, BlogVote, CommentVote

### Enums

| Enum | Values |
|---|---|
| PropertyType | house, apartment, studio, condo, penthouse |
| PropertyStatus | draft, published, pending, sold, archived |
| ListingCondition | brand_new, used, renovated, under_construction |
| FacingDirection | north, south, east, west |
| MediaContentType | img, video |
| BlogStatus | draft, published, hidden, deleted |
| NotificationType | NEW_MESSAGE, LISTING_APPROVED, NEW_COMMENT, PRICE_DROP, SYSTEM |
| NotificationEntityType | LISTING, BLOG, COMMENT, MESSAGE, DEAL, NONE |

### Flyway Migrations

| Version | Description |
|---|---|
| V1 | Initial schema — all tables, enums, indexes, foreign keys |
| V2 | Add CASCADE constraints on foreign keys |
| V3 | Add updated_at to media_assets |
| V4 | Add DEFAULT now() to timestamp columns |
| V5 | Rename enum value `new` → `brand_new` |
| V6 | Rename `cognito_sub` → `firebase_uid` (Cognito to Firebase migration) |
| V7 | Add profile_picture_url to users |

## Authentication

Authentication uses **Firebase Admin SDK**. The `FirebaseAuthFilter` intercepts all requests, extracts the Bearer token from the `Authorization` header, and verifies it with Firebase. The decoded `FirebaseToken` is stored in the Spring Security context and passed to controllers/services.

Google login is supported — users authenticating via Google are registered with their Google profile data (name, email, profile picture).

## File Storage (S3)

File uploads use **presigned URLs** — the client requests a presigned PUT URL from the backend, then uploads directly to S3 without the file passing through the server. Downloads work similarly with presigned GET URLs that expire after 60 minutes.

## CI/CD Pipeline

### Build Check (Pull Requests)

```
PR to main → Checkout → Setup Java 21 → Gradle Build (skip tests)
```

### Deploy (Push to Main)

```
Push to main → Checkout → Configure AWS → Login to ECR
    → Build Docker Image → Tag (SHA + latest) → Push to ECR
    → SSH into EC2 → Pull Image → Stop Old Container → Run New Container
```

### Docker Build (Multi-Stage)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk
# Copy gradle config, download dependencies, copy source, build JAR

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
# Non-root user (appuser), copy JAR, expose 8080
```

## AWS Infrastructure

| Service | Purpose |
|---|---|
| EC2 (t2.micro) | Application hosting |
| RDS (PostgreSQL 17) | Managed database |
| S3 | File/media storage |
| ECR | Docker image registry |
| Elastic IP | Static IP for EC2 |

### Nginx + SSL

Nginx runs on EC2 as a reverse proxy, terminating HTTPS (Let's Encrypt certificate) on port 443 and forwarding to Spring Boot on port 8080. HTTP requests on port 80 are redirected to HTTPS.

## Configuration

### Profiles

| Profile | Database | Usage |
|---|---|---|
| `local` | localhost PostgreSQL | Local development |
| `dev` | AWS RDS | Production (EC2) |

### Environment Variables

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `AWS_ACCESS_KEY_ID` | S3 access key |
| `AWS_ACCESS_KEY_SECRET` | S3 secret key |
| `AWS_S3_BUCKET_NAME` | S3 bucket name |
| `AWS_REGION` | AWS region |
| `FIREBASE_SERVICE_ACCOUNT_FILE` | Path to Firebase credentials |

## Local Development

### Prerequisites

- Java 21
- PostgreSQL 16+
- Firebase service account JSON in `src/main/resources/`
- `.env` file with AWS S3 credentials

### Run Locally

```bash
./gradlew bootRun
```

### Run with Docker Compose

```bash
docker compose up
```

The app will be available at `http://localhost:8080/api`.
