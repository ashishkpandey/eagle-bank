# 🦅 Eagle Bank API – Modular Spring Boot Project

## 🧭 Overview
Eagle Bank is a modular **Spring Boot 3.3.x** application that simulates a lightweight banking platform featuring **user**, **account**, and **transaction** management — secured via **JWT authentication** and built with clean layered architecture.

The application is designed for educational, test, and interview use cases to demonstrate enterprise-grade Java practices.

## 🏗️ Project Structure
```
eagle-bank/
├── eagle-bank-openapi-models    # OpenAPI-generated models
├── eagle-bank-inmem              # Main Spring Boot service (in-memory DB)
```

## ⚙️ Technology Stack
| Layer | Technology |
|-------|-------------|
| **Framework** | Spring Boot 3.3.4 |
| **Language** | Java 17 |
| **Persistence** | In-memory (ConcurrentHashMap) |
| **API Models** | OpenAPI-generated classes |
| **Validation** | Jakarta Bean Validation |
| **Security** | JWT Auth (Spring Security) |
| **Testing** | JUnit 5, Mockito, Spring MockMvc |

## 🚀 How to Build and Run
```bash
mvn clean install
mvn spring-boot:run -pl eagle-bank-inmem
```

## 🧪 Run Tests
```bash
# Run all tests
mvn test

# Run integration test specifically
mvn -pl eagle-bank-inmem -Dtest=EagleBankIntegrationTest test
```

## 🌐 REST Endpoints Summary
| HTTP Method | Endpoint | Description |
|--------------|-----------|-------------|
| `POST` | `/v1/users` | Create new user |
| `GET` | `/v1/users/{id}` | Fetch user details |
| `POST` | `/v1/auth/login` | Generate JWT token |
| `POST` | `/v1/accounts` | Create new account |
| `GET` | `/v1/accounts` | List user accounts |
| `POST` | `/v1/accounts/{accountNumber}/transactions` | Perform deposit/withdrawal |
| `GET` | `/v1/accounts/{accountNumber}/transactions` | List transactions |

## 🧩 Integration Flow
End-to-end test covered in `EagleBankIntegrationTest` validates:
1. User registration
2. JWT login and token validation
3. Account creation
4. Transactions (deposit & withdraw)
5. Field validation (400 error handling)

## 💻 Example curl
```bash
curl -X POST http://localhost:8080/v1/users   -H "Content-Type: application/json"   -d '{
    "name":"Test User",
    "email":"test@example.com",
    "password":"secret123",
    "phoneNumber":"+447700900123",
    "address":{
      "line1":"221B Baker Street",
      "line2":"Flat B",
      "town":"London",
      "county":"Greater London",
      "postcode":"NW1 6XE"
    }
  }'
```

