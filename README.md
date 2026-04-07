## Project Overview

ShoesRetail is a full-stack e-commerce platform for shoe retail built as a microservices architecture. The backend uses Spring Boot microservices with Netflix Eureka for service discovery and Spring Cloud Gateway for routing. The frontend is a Next.js application.

## Build Commands

```bash
# Build all modules (run from root)
mvn clean install

# Build a specific module
mvn -pl auth-service clean install

# Run a service (after building)
mvn -pl auth-service spring-boot:run

# Build frontend
cd frontend && npm install && npm run build
```

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   API Gateway   │────▶│  Eureka Server  │
│   (Next.js)     │     │   (Port 8080)   │     │   (Port 8761)   │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │  Auth    │  │  Order   │  │ Product  │
              │ Service  │  │ Service  │  │ Catalog  │
              └──────────┘  └──────────┘  └──────────┘
                    │             │             │
                    └─────────────┴─────────────┘
                                  │
                        ┌─────────┴─────────┐
                        ▼                   ▼
                  ┌──────────┐        ┌──────────┐
                  │PostgreSQL│        │  Redis   │
                  └──────────┘        └──────────┘
```

## Microservices

| Service | Port | Description |
|---------|------|-------------|
| `eureka-server` | 8761 | Service discovery (must start first) |
| `api-gateway` | 8080 | JWT validation, routing, circuit breakers, CORS |
| `auth-service` | env | Authentication, email verification, user profiles |
| `order-service` | env | Order management (in development) |
| `product-catalog-service` | env | Product catalog (in development) |
| `common-library` | - | Shared DTOs, constants, exceptions, utilities |

## Key Technologies

**Backend:**
- Java 21, Spring Boot 3.4.4, Spring Cloud 2024.0.0
- Lombok + MapStruct (annotation processors configured in root POM)
- PostgreSQL (database), Redis (token blacklist + caching)
- JWT authentication (jjwt 0.12.5)
- Resilience4j circuit breakers at gateway level
- SpringDoc OpenAPI for Swagger documentation

**Frontend:**
- Next.js 16, React 19, TypeScript
- Tailwind CSS 4, shadcn/ui components
- Zustand for state management
- React Hook Form + Zod for validation

## Configuration

All services use environment variables configured in `.env` file at project root. The `spring-dotenv` dependency loads these at startup.

## API Gateway Routing

Routes are defined in `GatewayConfig.java` using constants from `AppConstants.java`:

- `/api/v1/auth/admin/**` → auth-service (requires ROLE_ADMIN)
- `/api/v1/auth/user-profile/**` → auth-service (authenticated)
- `/api/v1/auth/register`, `/login`, etc. → auth-service (public)
- `/api/v1/products/**` → product-catalog-service (public)
- `/api/v1/order/**` → order-service (authenticated)

## Common Library Usage

`common-library` is shared across all services. Key classes:
- `AppConstants` — API paths, service names, cache keys, JWT claims
- `ApiResponse` — Standardized API response wrapper
- `JwtClaimsPayload` — JWT payload DTO
- Exception classes: `UnauthorizedException`, `ForbiddenException`, `ResourceNotFoundException`, etc.
- `TokenUtils`, `RoleValidator` — Utility classes

## Development Patterns

**MapStruct Mappers:** Use `@Mapper(componentModel = "spring")` and inject via constructor.

**JWT Flow:**
1. Gateway validates JWT on protected routes using `JwtAuthenticationFilter`
2. Token blacklist checked in Redis
3. User ID/roles extracted and passed via headers to downstream services

**Circuit Breakers:** Configured per service in `application.yml` under `resilience4j.circuitbreaker.instances`.

## Frontend Development

```bash
cd frontend
npm run dev      # Development server on localhost:3000
npm run build    # Production build
npm run lint     # Run ESLint
```

The frontend uses route groups: `(auth)` for authentication pages (login, register).