
# 🦅 Eagle Bank API – Modular Spring Boot Project

## 🧭 Overview
Eagle Bank is a modular **Spring Boot 3.3.x** application that simulates a lightweight banking platform featuring **user**, **account**, and **transaction** management — secured via **JWT authentication** and built with clean layered architecture.

This project implements the take-home exercise specification for Barclays' “Eagle Bank” API using Java and Spring Boot.  
The application supports full CRUD for users and accounts, as well as deposits and withdrawals recorded as transactions.

---

## 🏗️ Project Structure
```
eagle-bank/
├── eagle-bank-openapi-models    # OpenAPI-generated models
├── eagle-bank-inmem              # Main Spring Boot service (in-memory DB)
```
---

## ⚙️ Technology Stack
| Layer | Technology |
|-------|-------------|
| **Framework** | Spring Boot 3.3.4 |
| **Language** | Java 17 |
| **Persistence** | In-memory (ConcurrentHashMap) |
| **API Models** | OpenAPI-generated classes |
| **Validation** | Jakarta Bean Validation |
| **Security** | JWT Auth (Spring Security) |
| **Testing** | JUnit 5, Mockito, Spring MockMvc, JaCoCo |

---

## 🚀 How to Build and Run

```bash
# Build the project
mvn clean install

# Run main app
mvn spring-boot:run -pl eagle-bank-inmem
# → http://localhost:8080
```

> Default profile uses in-memory repositories.  
> JWT secret and expiration can be set in `application.yml` or via env vars.

---

## 🔐 Auth Flow (Signup → Login → Bearer Token)

```bash
# Create user
curl -s POST http://localhost:8080/v1/users   -H "Content-Type: application/json"   -d '{
    "email":"alice@example.com",
    "password":"P@ssw0rd!",
    "firstName":"Alice",
    "lastName":"Doe"
  }'

# Login and get JWT
TOKEN=$(curl -s POST http://localhost:8080/v1/auth/login   -H "Content-Type: application/json"   -d '{"email":"alice@example.com","password":"P@ssw0rd!"}' | jq -r '.token')

echo "JWT: $TOKEN"
```

Use `$TOKEN` for all subsequent calls:
```bash
-H "Authorization: Bearer $TOKEN"
```

---

## 🌐 REST Endpoints Summary

| HTTP | Endpoint | Description |
|-------|-----------|-------------|
| `POST` | `/v1/users` | Create user |
| `GET` | `/v1/users/{id}` | Fetch user details |
| `PATCH` | `/v1/users/{id}` | Update user details |
| `DELETE` | `/v1/users/{id}` | Delete user |
| `POST` | `/v1/auth/login` | Authenticate user & issue JWT |
| `POST` | `/v1/accounts` | Create bank account |
| `GET` | `/v1/accounts` | List accounts |
| `GET` | `/v1/accounts/{id}` | Fetch account |
| `PATCH` | `/v1/accounts/{id}` | Update account |
| `DELETE` | `/v1/accounts/{id}` | Delete account |
| `POST` | `/v1/accounts/{accountId}/transactions` | Deposit / Withdraw |
| `GET` | `/v1/accounts/{accountId}/transactions` | List transactions |
| `GET` | `/v1/accounts/{accountId}/transactions/{txId}` | Fetch one transaction |

---

## 💻 Example cURL

### Create account
```bash
curl -X POST http://localhost:8080/v1/accounts   -H "Authorization: Bearer $TOKEN"   -H "Content-Type: application/json"   -d '{"name":"Main Account","currency":"GBP"}'
```

### Deposit
```bash
curl -X POST http://localhost:8080/v1/accounts/{ACCOUNT_ID}/transactions   -H "Authorization: Bearer $TOKEN"   -H "Content-Type: application/json"   -d '{"type":"deposit","amount":100.00,"description":"Initial deposit"}'
```

### Withdraw (insufficient funds → 422)
```bash
curl -X POST http://localhost:8080/v1/accounts/{ACCOUNT_ID}/transactions   -H "Authorization: Bearer $TOKEN"   -H "Content-Type: application/json"   -d '{"type":"withdrawal","amount":500.00}'
```

### List transactions
```bash
curl -s http://localhost:8080/v1/accounts/{ACCOUNT_ID}/transactions   -H "Authorization: Bearer $TOKEN"
```

---

## 📘 OpenAPI / Swagger

If **Springdoc** is enabled:

- JSON: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)  
- Swagger UI: [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)

Export to file:
```bash
curl -s http://localhost:8080/v3/api-docs > openapi.json
```

If manually maintained, include the `openapi.yaml` in repo root.

---

## 🧪 Test & Coverage

```bash
# Run all tests
mvn clean test

# Generate coverage
mvn verify
# View report:
# eagle-bank-inmem/target/site/jacoco/index.html
```

> Integration tests validate user registration → auth → account creation → deposit & withdraw → balance consistency.

---

## ⚙️ HTTP Status Mapping

| Status | Meaning |
|--------|----------|
| `400` | Bad request (missing/invalid fields) |
| `401` | Unauthorized (missing/invalid JWT) |
| `403` | Forbidden (accessing another user's data) |
| `404` | Not found (user/account/transaction not found) |
| `409` | Conflict (delete user with accounts) |
| `422` | Unprocessable Entity (insufficient funds) |

---

## 🧩 Integration Flow
End-to-end test (`EagleBankIntegrationTest`) covers:
1. User registration  
2. Authentication and JWT validation  
3. Account creation  
4. Transactions (deposit/withdrawal)  
5. Validation error handling  

---

## 🧠 Design Highlights
- Stateless JWT auth filter (`JwtAuthFilter`)
- Layered architecture (Controller → Service → Repository)
- Custom exception handlers
- Validation via `jakarta.validation`
- Simple balance mutation logic with overdraw prevention

---

## ✅ Ready for Submission
Implements all **minimum and extended** endpoints required by the take-home PDF.  
Fully testable, self-contained, and documented for quick review.

