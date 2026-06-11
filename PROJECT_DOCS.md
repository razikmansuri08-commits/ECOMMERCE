# Ecom — Spring Boot REST API Documentation

## Overview

A RESTful e-commerce backend built with **Spring Boot 3.3.5** and **Java 21**. It supports user registration, JWT-based authentication, product management, shopping cart, and order processing. Role-based access control separates `USER` and `ADMIN` capabilities.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.5 |
| Language | Java 21 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL |
| Build | Maven |
| Utilities | Lombok |

---

## Project Structure

```
com.rmtech.ecom
├── Config/           # Security, JWT filter, JWT utility, entry point
├── Controllers/      # REST controllers (Auth, Admin, User, Cart, Order, Public)
├── DTOS/             # Data Transfer Objects
├── Entities/         # JPA entities
├── Exception/        # Custom exceptions + global handler
├── Repositories/     # Spring Data JPA repositories
├── Service/          # Business logic services
├── AddToCartRequest  # Request body model
└── EcomApplication   # Entry point
```

---

## Database Configuration

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.jpa.hibernate.ddl-auto=update
spring.main.allow-circular-references=true
```

---

## Entity Model

### User
| Field | Type | Notes |
|---|---|---|
| userId | Long | PK, auto-generated |
| name | String | Used as username |
| email | String | — |
| password | String | BCrypt encoded |
| roles | List\<Roles\> | `USER` or `ADMIN` |
| cart | Cart | One-to-one |
| order | List\<Orders\> | One-to-many |

### Product
| Field | Type |
|---|---|
| id | Long |
| name | String |
| price | double |

### Cart
- One-to-one with `User`
- One-to-many with `Cart_Items`

### Cart_Items
- Many-to-one with `Cart` and `Product`
- Holds `quantity`

### Orders
- `orderId` is a UUID string (auto-generated in constructor)
- Many-to-one with `User`
- One-to-many with `Order_items`

### Order_items
- Many-to-one with `Orders` and `Product`
- Holds `quantity`

### BlackListedTokens
- Stores invalidated JWT tokens with `expiryDate`
- Cleaned up hourly via `@Scheduled`

---

## Security

### JWT Flow
1. Client sends credentials to `POST /auth/login`
2. Server validates and returns a signed JWT (10-minute expiry)
3. Client includes `Authorization: Bearer <token>` on subsequent requests
4. `JwtFilter` intercepts every request, validates the token, and sets the `SecurityContext`
5. On logout (`POST /auth/logout`), the token is added to the `BlackListedTokens` table

### Route Access Rules
| Pattern | Access |
|---|---|
| `/public/**`, `/auth/login` | Public |
| `/auth/logout` | Authenticated |
| `/admin/**` | `ADMIN` role only |
| All others | Authenticated |

### Key Components
- **JwtUtil** — generates/validates tokens, extracts claims. Secret key is hardcoded (see issues below).
- **JwtFilter** — `OncePerRequestFilter` that checks blacklist, extracts username, sets auth context.
- **JwtAuthEntryPoint** — returns JSON `401` response for unauthenticated requests.
- **TokenBlacklist_Service** — persists invalidated tokens; scheduled cleanup every hour.

---

## API Reference

### Auth — `/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Returns JWT token |
| POST | `/auth/logout` | Bearer token | Blacklists current token |

**Login request body:**
```json
{ "username": "john", "password": "secret" }
```

---

### Public — `/public`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/public/create` | Public | Register new user |
| GET | `/public/products/getallproducts` | Public | List all products |
| GET | `/public/products/get/{id}` | Public | Get product by ID |

---

### User — `/user`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/user/getuser` | USER | Get own profile |
| PATCH | `/user/update` | USER | Update own profile |
| DELETE | `/user/delete` | USER | Delete own account |

---

### Cart — `/user/cart`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/user/cart/add/{productId}` | USER | Add item to cart |
| GET | `/user/cart/get` | USER | View cart |
| DELETE | `/user/cart/deleteitem/{productId}` | USER | Remove item from cart |
| DELETE | `/user/cart/clearcart` | USER | Clear entire cart |

**Add to cart request body:**
```json
{ "quantity": 2 }
```

---

### Orders — `/user/orders`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/user/orders/placeorder` | USER | Place order from cart |
| GET | `/user/orders/getallorders` | USER | Get all orders |
| GET | `/user/orders/getorder/{orderId}` | USER | Get specific order |
| DELETE | `/user/orders/cancelorder/{orderId}` | USER | Cancel specific order |
| DELETE | `/user/orders/cancelall` | USER | Cancel all orders |

---

### Admin — `/admin`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/admin/getuser/{userId}` | ADMIN | Get any user by ID |
| DELETE | `/admin/deleteuser/{userId}` | ADMIN | Delete any user |
| POST | `/admin/products/create` | ADMIN | Create product |
| PATCH | `/admin/products/update/{id}` | ADMIN | Update product |
| DELETE | `/admin/products/delete/{id}` | ADMIN | Delete product |
| PATCH | `/admin/update` | ADMIN | Update own admin profile |
| DELETE | `/admin/delete` | ADMIN | Delete own admin account |

---

## Exception Handling

All exceptions return a consistent `Error_ResponseDto`:
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "message": "...",
  "error": "NOT_FOUND",
  "status": 404
}
```

| Exception | HTTP Status |
|---|---|
| UserNotFoundException | 404 |
| ProductNotFoundException | 404 |
| CartNotFoundException | 404 |
| OrderNotFoundException | 404 |
| JwtTokenExpiredException | 403 |
| JwtTokenInvalidException | 404 |
| Exception (fallback) | 500 |

---

## Bugs & Issues — Full Analysis

> Total findings: **17** across 9 files.
> Severities: 1 Critical · 6 High · 5 Medium · 5 Low

---

### CRITICAL

---

#### BUG-01 — `ClassCastException` on invalid/expired token

- **File:** `Config/JwtFilter.java` — line 56–58
- **Severity:** Critical
- **Impact:** Every request with a blacklisted or invalid token crashes the filter chain with an unhandled `ClassCastException` instead of returning a `401` response.

**Root cause:**
`JwtTokenExpiredException` and `JwtTokenInvalidException` both extend `RuntimeException`, not `AuthenticationException`. The catch block catches them but then blindly casts them to `AuthenticationException`, which fails at runtime.

```java
// BUGGY CODE
catch (AuthenticationException | JwtTokenExpiredException | JwtTokenInvalidException ex) {
    authenticationEntryPoint.commence(request, response, (AuthenticationException) ex); // ClassCastException here
}
```

**Fix:** Handle the custom exceptions in a separate catch block and write the error response directly without casting.

```java
// FIXED CODE
catch (AuthenticationException ex) {
    authenticationEntryPoint.commence(request, response, ex);
} catch (JwtTokenExpiredException | JwtTokenInvalidException ex) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
}
```

---

### HIGH

---

#### BUG-02 — `validateToken()` always returns `true`

- **File:** `Config/JwtUtil.java` — line 36–44
- **Severity:** High
- **Impact:** Any structurally valid JWT passes token validation regardless of whether the username inside the token matches the authenticated user. A token issued for user A can be used to authenticate as user B.

**Root cause:**
The result of `.equals()` is evaluated but never returned. The method falls through to `return true` unconditionally.

```java
// BUGGY CODE
public boolean validateToken(String token, String username) {
    try {
        getUsernameFromToken(token).equals(username); // result discarded!
    } catch (Exception e) {
        return false;
    }
    return true; // always true
}
```

**Fix:** Return the result of the comparison.

```java
// FIXED CODE
public boolean validateToken(String token, String username) {
    try {
        return getUsernameFromToken(token).equals(username);
    } catch (Exception e) {
        return false;
    }
}
```

---

#### BUG-03 — Hardcoded JWT secret key

- **File:** `Config/JwtUtil.java` — line 16
- **Severity:** High
- **Impact:** Anyone with access to the source code or repository can extract the secret and forge valid JWT tokens, completely bypassing authentication.

```java
// BUGGY CODE
private final SecretKey key = Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey17".getBytes());
```

**Fix:** Externalize the secret via `@Value` and store it in `application.properties` or an environment variable.

```java
// FIXED CODE — JwtUtil.java
@Value("${jwt.secret}")
private String secret;

private SecretKey key;

@PostConstruct
public void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
}
```

```properties
# application.properties
jwt.secret=${JWT_SECRET}
```

---

#### BUG-04 — `Long` compared with `==` instead of `.equals()` in `get_ord()`

- **File:** `Service/Order_Service.java` — line 62
- **Severity:** High
- **Impact:** For any user with an ID greater than 127 (outside Java's `Long` cache range), this comparison always returns `false`. Every valid order fetch will throw `OrderNotFoundException` for real users in production.

```java
// BUGGY CODE
else if (order.getUser().getUserId() == user.getUserId())
```

**Fix:**

```java
// FIXED CODE
else if (order.getUser().getUserId().equals(user.getUserId()))
```

---

#### BUG-05 — `Long` compared with `==` instead of `.equals()` in `cancel_ord()`

- **File:** `Service/Order_Service.java` — line 76
- **Severity:** High
- **Impact:** Same as BUG-04. Valid order cancellation requests will always fail for users with IDs > 127.

```java
// BUGGY CODE
if (order.getUser().getUserId() == user.getUserId())
```

**Fix:**

```java
// FIXED CODE
if (order.getUser().getUserId().equals(user.getUserId()))
```

---

#### BUG-06 — Password not re-encoded on update

- **File:** `Service/User_Service.java` — line 52
- **Severity:** High
- **Impact:** After a user updates their password, the new password is stored as plaintext. The next login attempt will fail because BCrypt's `matches()` will compare a plaintext string against a BCrypt hash and return `false`, locking the user out.

```java
// BUGGY CODE
if (dto.getPassword() != null)
    us.setPassword(dto.getPassword()); // stored as plaintext
```

**Fix:**

```java
// FIXED CODE
if (dto.getPassword() != null)
    us.setPassword(passwordEncoder.encode(dto.getPassword()));
```

---

#### BUG-07 — `handleInvalidJwtException` catches the wrong exception type

- **File:** `Exception/GlobalExceptionHandler.java` — line 52–60
- **Severity:** High
- **Impact:** The handler is annotated with `@ExceptionHandler(JwtTokenInvalidException.class)` but its method parameter is `JwtTokenExpiredException`. Spring MVC matches handlers by parameter type, so `JwtTokenInvalidException` is never routed here — it falls through to the generic `Exception` handler and returns a `500` instead of the intended response.

```java
// BUGGY CODE
@ExceptionHandler(JwtTokenInvalidException.class)
public ResponseEntity<Error_ResponseDto> handleInvalidJwtException(JwtTokenExpiredException ex) // wrong type!
```

**Fix:**

```java
// FIXED CODE
@ExceptionHandler(JwtTokenInvalidException.class)
public ResponseEntity<Error_ResponseDto> handleInvalidJwtException(JwtTokenInvalidException ex)
```

---

#### BUG-08 — Database password stored in plaintext

- **File:** `resources/application.properties` — line 4
- **Severity:** High
- **Impact:** If this file is committed to version control, the database credentials are fully exposed to anyone with repository access.

```properties
# BUGGY
spring.datasource.password=Razik@1712
```

**Fix:** Use an environment variable reference.

```properties
# FIXED
spring.datasource.password=${DB_PASSWORD}
```

Set `DB_PASSWORD` in your OS environment or a `.env` file that is listed in `.gitignore`.

---

### MEDIUM

---

#### BUG-09 — `GlobalExceptionHandler` extends `RuntimeException`

- **File:** `Exception/GlobalExceptionHandler.java` — line 11
- **Severity:** Medium
- **Impact:** A `@ControllerAdvice` class has no business extending `RuntimeException`. If Spring or any other component ever catches a broad `Exception` or `RuntimeException`, the handler class itself could be caught and swallowed, causing silent failures.

```java
// BUGGY CODE
public class GlobalExceptionHandler extends RuntimeException
```

**Fix:**

```java
// FIXED CODE
public class GlobalExceptionHandler
```

---

#### BUG-10 — Duplicate constructor parameter for `JwtAuthEntryPoint` in `SecurityConfig`

- **File:** `Config/SecurityConfig.java` — line 27–30
- **Severity:** Medium
- **Impact:** The constructor declares `JwtAuthEntryPoint` twice under different parameter names (`jwtAuth` and `jwtauth`). Only `jwtauth` is assigned. The parameter `jwtAuth` is dead code and can confuse Spring's dependency injection.

```java
// BUGGY CODE
public SecurityConfig(JwtFilter jwtFilter, JwtAuthEntryPoint jwtAuth, JwtAuthEntryPoint jwtauth) {
    this.jwtauth = jwtauth;
    this.jwtFilter = jwtFilter;
    // jwtAuth is never used
}
```

**Fix:**

```java
// FIXED CODE
public SecurityConfig(JwtFilter jwtFilter, JwtAuthEntryPoint jwtauth) {
    this.jwtauth = jwtauth;
    this.jwtFilter = jwtFilter;
}
```

---

#### BUG-11 — `Product_Repo` and `User_Repo` not marked `final` in `Cart_Service`

- **File:** `Service/Cart_Service.java` — line 19–24
- **Severity:** Medium
- **Impact:** Only `cr` is declared `final`. `pr` and `ur` are mutable after construction, which is inconsistent and could lead to accidental reassignment in a multi-threaded context.

```java
// BUGGY CODE
private final Cart_Repo cr;
Product_Repo pr;   // not final
User_Repo ur;      // not final
```

**Fix:**

```java
// FIXED CODE
private final Cart_Repo cr;
private final Product_Repo pr;
private final User_Repo ur;
```

---

#### BUG-12 — `pr`, `ur`, `cs` fields not marked `final` in `Order_Service`

- **File:** `Service/Order_Service.java` — line 14–19
- **Severity:** Medium
- **Impact:** Same as BUG-11. Only `or` is `final`. The other three dependencies are mutable after construction.

```java
// BUGGY CODE
private final Order_Repo or;
Product_Repo pr;
User_Repo ur;
Cart_Service cs;
```

**Fix:**

```java
// FIXED CODE
private final Order_Repo or;
private final Product_Repo pr;
private final User_Repo ur;
private final Cart_Service cs;
```

---

#### BUG-13 — `spring.main.allow-circular-references=true` masks a real circular dependency

- **File:** `resources/application.properties` — line 9
- **Severity:** Medium
- **Impact:** This flag suppresses a Spring startup error caused by a circular bean dependency. It does not fix the underlying problem. Circular dependencies indicate a design flaw and can cause partially initialized beans to be injected.

**Fix:** Identify the cycle (likely between `Cart_Service` and `Order_Service` both depending on `User_Repo`) and break it by adding `@Lazy` to one injection point:

```java
public Order_Service(Order_Repo or, Product_Repo pr, User_Repo ur, @Lazy Cart_Service cs) {
```

Then remove the flag from `application.properties`.

---

### LOW

---

#### BUG-14 — `@Autowired` field injection in `Public_Controllers`

- **File:** `Controllers/Public_Controllers.java` — line 24–31
- **Severity:** Low
- **Impact:** Field injection hides dependencies, makes the class impossible to unit test without a Spring context, and is inconsistent with every other controller in the project.

```java
// BUGGY CODE
@Autowired private User_Service us;
@Autowired private Cart_Service cs;
@Autowired private Product_Service ps;
@Autowired private Order_Service os;
```

**Fix:**

```java
// FIXED CODE
public Public_Controllers(User_Service us, Cart_Service cs, Product_Service ps, Order_Service os) {
    this.us = us;
    this.cs = cs;
    this.ps = ps;
    this.os = os;
}
```

---

#### BUG-15 — `@Autowired` field injection for `PasswordEncoder` in `User_Service`

- **File:** `Service/User_Service.java` — line 22–23
- **Severity:** Low
- **Impact:** Mixed injection styles in the same class. `User_Repo` uses constructor injection while `PasswordEncoder` uses field injection.

```java
// BUGGY CODE
@Autowired
private PasswordEncoder passwordEncoder;
```

**Fix:** Add it to the constructor.

```java
// FIXED CODE
public User_Service(User_Repo ur, PasswordEncoder passwordEncoder) {
    this.ur = ur;
    this.passwordEncoder = passwordEncoder;
}
```

---

#### BUG-16 — `@Autowired` field injection in `TokenBlacklist_Service`

- **File:** `Service/TokenBlacklist_Service.java` — line 13–14
- **Severity:** Low

```java
// BUGGY CODE
@Autowired
private BlackListedTokens_Repo repository;
```

**Fix:**

```java
// FIXED CODE
public TokenBlacklist_Service(BlackListedTokens_Repo repository) {
    this.repository = repository;
}
```

---

#### BUG-17 — `@Autowired` field injection in `UserDetailsServiceImpl`

- **File:** `Service/UserDetailsServiceImpl.java` — line 18–19
- **Severity:** Low

```java
// BUGGY CODE
@Autowired
private User_Repo ur;
```

**Fix:**

```java
// FIXED CODE
public UserDetailsServiceImpl(User_Repo ur) {
    this.ur = ur;
}
```

---

### Summary Table

| # | File | Issue | Severity |
|---|---|---|---|
| BUG-01 | `Config/JwtFilter.java` | `ClassCastException` when casting custom exceptions to `AuthenticationException` | Critical |
| BUG-02 | `Config/JwtUtil.java` | `validateToken()` always returns `true` — `.equals()` result discarded | High |
| BUG-03 | `Config/JwtUtil.java` | JWT secret key hardcoded in source | High |
| BUG-04 | `Service/Order_Service.java` | `Long` compared with `==` in `get_ord()` | High |
| BUG-05 | `Service/Order_Service.java` | `Long` compared with `==` in `cancel_ord()` | High |
| BUG-06 | `Service/User_Service.java` | Password not re-encoded on update | High |
| BUG-07 | `Exception/GlobalExceptionHandler.java` | `handleInvalidJwtException` has wrong parameter type | High |
| BUG-08 | `resources/application.properties` | Database password stored in plaintext | High |
| BUG-09 | `Exception/GlobalExceptionHandler.java` | `GlobalExceptionHandler` extends `RuntimeException` | Medium |
| BUG-10 | `Config/SecurityConfig.java` | Duplicate `JwtAuthEntryPoint` constructor parameter | Medium |
| BUG-11 | `Service/Cart_Service.java` | `Product_Repo` and `User_Repo` not marked `final` | Medium |
| BUG-12 | `Service/Order_Service.java` | `pr`, `ur`, `cs` fields not marked `final` | Medium |
| BUG-13 | `resources/application.properties` | `allow-circular-references=true` masks real circular dependency | Medium |
| BUG-14 | `Controllers/Public_Controllers.java` | `@Autowired` field injection instead of constructor injection | Low |
| BUG-15 | `Service/User_Service.java` | `@Autowired` field injection for `PasswordEncoder` | Low |
| BUG-16 | `Service/TokenBlacklist_Service.java` | `@Autowired` field injection for repository | Low |
| BUG-17 | `Service/UserDetailsServiceImpl.java` | `@Autowired` field injection for `User_Repo` | Low |

---

## How to Run

```bash
# 1. Create MySQL database
CREATE DATABASE ecommerce;

# 2. Update credentials in application.properties

# 3. Build and run
mvn spring-boot:run
```

Default port: `8080`
