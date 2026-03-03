# NerdMarket API Documentation

Base URL (local): `http://localhost:8080`
Base URL (production): `http://coms-3090-022.class.las.iastate.edu:8080`

---

## Users

### POST /users/signup
Creates a new user account.

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| firstName | String | No | |
| lastName | String | No | |
| username | String | Yes | Must be unique |
| email | String | Yes | Must be unique |
| phoneNumber | String | No | |
| password | String | Yes | Stored as BCrypt hash |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | User object | Account created |
| 400 | `"Username is already taken"` | Duplicate username |
| 400 | `"Email is already in use"` | Duplicate email |

**Example Request**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "phoneNumber": "515-555-1234",
  "password": "password123"
}
```

**Example Response**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "phoneNumber": "515-555-1234",
  "active": true,
  "admin": false,
  "loginAttempts": 0,
  "locked": false,
  "createdAt": "2025-01-01T12:00:00"
}
```

---

### POST /users/login
Authenticates a user by username or email and password.

**Request Body**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| usernameOrEmail | String | Yes | Accepts either username or email |
| password | String | Yes | |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | User object | Valid credentials |
| 400 | `"Invalid username or email"` | No matching account |
| 400 | `"Account Deactivated"` | Account is inactive |
| 400 | `"Account is locked due to too many failed attempts. Reset your password to unlock."` | Account locked after 3 failed attempts |
| 400 | `"Wrong password. Attempt N of 3"` | Wrong password (N = current attempt count) |

**Notes:**
- After 3 consecutive failed login attempts the account is automatically locked.
- Call `POST /users/reset-password` to unlock the account and set a new password.
- A successful login resets the failed attempt counter to 0.

**Example Request**
```json
{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

---

### GET /users/{id}
Returns a single user by their database ID.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | User's database ID |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | User object | Found |
| 400 | `"User not found"` | No user with that ID |

---

### PUT /users/{id}/change-password
Changes a user's password. Requires the current password for verification.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | User's database ID |

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| oldPassword | String | Yes |
| newPassword | String | Yes |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object | Success |
| 400 | `"User not found"` | Invalid ID |
| 400 | `"Old password is incorrect"` | Wrong current password |

**Example Request**
```json
{
  "oldPassword": "password123",
  "newPassword": "newpassword456"
}
```

---

### PUT /users/{id}/change-email
Changes a user's email address. Requires the current password for verification.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | User's database ID |

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| password | String | Yes |
| newEmail | String | Yes |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object | Success |
| 400 | `"User not found"` | Invalid ID |
| 400 | `"Password is incorrect"` | Wrong password |
| 400 | `"Email is already in use"` | Email taken by another account |

**Example Request**
```json
{
  "password": "password123",
  "newEmail": "newemail@example.com"
}
```

---

### POST /users/reset-password
Resets a user's password by email. Also unlocks a locked account and resets the login attempt counter to 0.

**Request Body**
| Field | Type | Required |
|-------|------|----------|
| email | String | Yes |
| oldPassword | String | Yes |
| newPassword | String | Yes |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object (`locked=false`, `loginAttempts=0`) | Success |
| 400 | `"No account with that email"` | Email not found |
| 400 | `"Old password is incorrect"` | Wrong current password |

**Example Request**
```json
{
  "email": "john@example.com",
  "oldPassword": "password123",
  "newPassword": "freshpassword789"
}
```

---

### DELETE /users/{id}/delete-account
Permanently deletes the user's own account. Password is passed via a custom request header.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | User's database ID |

**Request Headers**
| Header | Required | Notes |
|--------|----------|-------|
| X-Password | Yes | User's plaintext password |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 204 | (none) | Account deleted |
| 400 | `"User not found"` | Invalid ID |
| 400 | `"Incorrect password"` | Wrong password |

---

## Admin

All admin endpoints require a `?userId=` query parameter set to the ID of a user with `admin = true`. Passing a non-admin user ID returns `400 "Unauthorized - admin access required"`.

---

### GET /admin/users?userId={adminId}
Returns a list of all users in the system.

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Array of user objects | Success |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |

---

### DELETE /admin/users/{targetId}?userId={adminId}
Permanently deletes a user account. An admin cannot delete their own account.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| targetId | Long | ID of the user to delete |

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | `"User deleted successfully"` | Deleted |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |
| 400 | `"Can't delete your own account"` | userId == targetId |
| 400 | `"User not found"` | Invalid targetId |

---

### PUT /admin/users/{targetId}/promote?userId={adminId}
Grants admin privileges to the target user (`admin = true`).

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| targetId | Long | ID of user to promote |

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object (`admin=true`) | Success |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |

---

### PUT /admin/users/{targetId}/demote?userId={adminId}
Removes admin privileges from the target user (`admin = false`). An admin cannot demote themselves.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| targetId | Long | ID of user to demote |

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object (`admin=false`) | Success |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |
| 400 | `"Can't remove your own admin status"` | userId == targetId |

---

### PUT /admin/users/{targetId}/activate?userId={adminId}
Reactivates a deactivated user account, allowing them to log in again.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| targetId | Long | ID of user to activate |

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object (`active=true`) | Success |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |

---

### PUT /admin/users/{targetId}/deactivate?userId={adminId}
Deactivates a user account. The user will be blocked from logging in with `"Account Deactivated"`.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| targetId | Long | ID of user to deactivate |

**Query Parameters**
| Param | Type | Required |
|-------|------|----------|
| userId | Long | Yes — must belong to an admin |

**Responses**
| Code | Body | Condition |
|------|------|-----------|
| 200 | Updated user object (`active=false`) | Success |
| 400 | `"Unauthorized - admin access required"` | Non-admin userId |

---

## Market (Cards)

### GET /api/cards
Returns all cards in the database.

**Responses**
| Code | Body |
|------|------|
| 200 | Array of card objects |

---

### GET /api/cards/{id}
Returns a single card by its database ID.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | Card ID |

**Responses**
| Code | Body |
|------|------|
| 200 | Card object or `null` if not found |

---

### GET /api/cards/type/{cardType}
Returns all cards matching the given card type (e.g. `Pokemon`, `MTG`, `YuGiOh`, `Baseball`). Case-sensitive.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| cardType | String | Card type string — case-sensitive |

**Responses**
| Code | Body |
|------|------|
| 200 | Array of card objects (empty array if none found) |

---

### GET /api/cards/top10
Returns the 10 most expensive cards across all types, ordered by price descending.

**Responses**
| Code | Body |
|------|------|
| 200 | Array of up to 10 card objects |

---

### GET /api/cards/top10/{cardType}
Returns the 10 most expensive cards for a specific card type, ordered by price descending.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| cardType | String | Card type to filter — case-sensitive |

**Responses**
| Code | Body |
|------|------|
| 200 | Array of up to 10 card objects |

---

### POST /api/cards
Creates a new card listing.

**Request Body**
| Field | Type | Notes |
|-------|------|-------|
| cardType | String | e.g. `"Pokemon"`, `"MTG"`, `"YuGiOh"` |
| cardName | String | Card name (e.g. `"Charizard EX"`) |
| cardSet | String | Set or expansion (e.g. `"Surging Sparks"`) |
| cardRarity | String | Rarity tier (e.g. `"Illustration Rare"`) |
| price | double | Current market value |
| imageUrl | String | URL to card image |

**Responses**
| Code | Body |
|------|------|
| 200 | `{"message":"success"}` |

**Example Request**
```json
{
  "cardType": "Pokemon",
  "cardName": "Charizard EX",
  "cardSet": "Surging Sparks",
  "cardRarity": "Illustration Rare",
  "price": 450.00,
  "imageUrl": "https://example.com/charizard.png"
}
```

---

### PUT /api/cards/{id}
Fully updates a card by ID. All fields are replaced with the request body values.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | Card ID |

**Request Body** — same fields as `POST /api/cards`

**Responses**
| Code | Body |
|------|------|
| 200 | Updated card object |
| 200 | `null` if card not found |

---

### DELETE /api/cards/{id}
Deletes a single card by ID.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | Card ID |

**Responses**
| Code | Body |
|------|------|
| 200 | `{"message":"success"}` |
| 200 | `{"message":"error"}` (card not found) |

---

### DELETE /api/cards/type/{cardType}
Deletes all cards of a given type.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| cardType | String | Card type to delete — case-sensitive |

**Responses**
| Code | Body |
|------|------|
| 200 | `{"message":"success"}` |
| 200 | `{"message":"No cards found with type: {cardType}"}` |

---

### GET /api/cards/pokemon/fetch-all
Fetches and imports all Pokemon card data from the external TCGDex API into the database.

**Responses**
| Code | Body |
|------|------|
| 200 | Status or summary string |

---

### GET /api/cards/mtg/fetch-all
Fetches and imports all Magic: The Gathering card data from the Scryfall API.

**Responses**
| Code | Body |
|------|------|
| 200 | Status or summary string |

---

### GET /api/cards/yugioh/fetch-all
Fetches and imports all Yu-Gi-Oh card data from the external API.

**Responses**
| Code | Body |
|------|------|
| 200 | Status or summary string |

---

## Price Tracking

Price records track historical price snapshots for individual cards. Each record links to a card and stores the price and timestamp.

**PriceTracking Object**
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Auto-generated |
| price | double | Recorded price |
| recordedAt | LocalDateTime | Timestamp of the snapshot |

---

### GET /api/prices
Returns all price records across all cards.

**Responses**
| Code | Body |
|------|------|
| 200 | Array of PriceTracking objects |

---

### GET /api/prices/card/{cardId}
Returns the full price history for a specific card, ordered newest first.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| cardId | Long | The card's database ID |

**Responses**
| Code | Body |
|------|------|
| 200 | Array of PriceTracking objects (empty if no records found) |

---

### GET /api/prices/card/{cardId}/latest
Returns the single most recent price record for a card.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| cardId | Long | The card's database ID |

**Responses**
| Code | Body |
|------|------|
| 200 | PriceTracking object or `null` if no records exist |

---

### POST /api/prices
Creates a new price record.

**Request Body**
| Field | Type | Notes |
|-------|------|-------|
| price | double | Price to record |
| recordedAt | String (ISO datetime) | e.g. `"2025-01-01T12:00:00"` |

**Responses**
| Code | Body |
|------|------|
| 200 | `{"message":"success"}` |

---

### PUT /api/prices/{id}
Updates an existing price record by ID.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | Price record ID |

**Request Body** — same fields as `POST /api/prices`

**Responses**
| Code | Body |
|------|------|
| 200 | Updated PriceTracking object |
| 200 | `null` if record not found |

---

### DELETE /api/prices/{id}
Deletes a price record by ID.

**Path Parameters**
| Param | Type | Notes |
|-------|------|-------|
| id | Long | Price record ID |

**Responses**
| Code | Body |
|------|------|
| 200 | `{"message":"success"}` |
| 200 | `{"message":"error"}` (record not found) |
