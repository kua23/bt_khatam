# API Gateway

This Spring Cloud Gateway routes requests to all microservices through a single entry point on port 8080.

## Services Routing

| Service | Port | Context Path | Gateway Route |
|---------|------|--------------|---------------|
| FD Account Service | 8086 | /api/fd-accounts | http://localhost:8080/api/fd-accounts/** |
| Product Pricing Service | 8084 | /api/products | http://localhost:8080/api/products/** |
| Customer Service | 8083 | /api/customer | http://localhost:8080/api/customer/** |
| FD Calculator Service | 8085 | /api/calculator | http://localhost:8080/api/calculator/** |
| Login Service | 8087 | /api/login, /api/auth | http://localhost:8080/api/login/**, /api/auth/** |

## Running the Gateway

```bash
cd gateway
mvn spring-boot:run
```

Gateway will start on: http://localhost:8080

## Example Requests Through Gateway

### FD Account Service
```bash
# Create account
POST http://localhost:8080/api/fd-accounts/accounts/create/standard

# Get account details
GET http://localhost:8080/api/fd-accounts/accounts/0011000025
```

### Product Pricing Service
```bash
# Get product by code
GET http://localhost:8080/api/products/code/FD-STD-6M

# List all products
GET http://localhost:8080/api/products
```

### Customer Service
```bash
# Get customer details
GET http://localhost:8080/api/customer/1
```

### FD Calculator Service
```bash
# Calculate FD
POST http://localhost:8080/api/calculator/calculate/standalone
```

## Health Check

Gateway health endpoint:
```bash
GET http://localhost:8080/actuator/health
```

Gateway routes endpoint:
```bash
GET http://localhost:8080/actuator/gateway/routes
```

## CORS Configuration

Gateway is configured to allow requests from:
- http://localhost:3000 (React default)
- http://localhost:5173 (Vite default)

Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS

## Features

- Request/response logging
- CORS support for frontend integration
- Health monitoring via Actuator
- Route management and discovery
