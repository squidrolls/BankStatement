### API Endpoints


| Endpoint                    | Description                                |
|-----------------------------|--------------------------------------------|
| **Users**| |
| `GET /api/v1/users`         | Retrieves all users with their associated accounts |
| `POST /api/v1/users`        | Creates a new user                         |
| `PUT /api/v1/users/{userId}`| Updates the user with the specified ID     |
| `DELETE /api/v1/users/{userId}` | Deletes the user with the specified ID     |
|**Accounts**| |
| `GET /api/v1/users/{userId}/accounts`               | Retrieves all accounts for the specified user (excluding transactions) |
| `POST /api/v1/users/{userId}/accounts`              | Creates a new account for the specified user |
| `DELETE /api/v1/users/{userId}/accounts/{accountNumber}` | Soft deletes (closes) the account with the specified account number for the specified user |
|**Transaction**| |
| `GET /api/v1/users/{userId}/accounts/{accountNumber}/transactions`                         | Retrieves a paginated list of transactions for the specified account, with optional filtering by date range and transaction type |
| `GET /api/v1/users/{userId}/accounts/{accountNumber}/transactions/{transactionId}`         | Retrieves the details of a specific transaction for the specified account   |
| `POST /api/v1/users/{userId}/accounts/{accountNumber}/transactions`                        | Creates a new transaction for the specified account, modifying the account balance |

### Endpoint Details
**Endpoint:** `GET /api/v1/users/{userId}/accounts/{accountNumber}/transactions`

**Description:** Retrieves a paginated list of transactions for the specified account, with optional filtering by date range and transaction type.

**Query Parameters:**
- `startDate` (LocalDate, optional): The start date for filtering transactions.
- `endDate` (LocalDate, optional): The end date for filtering transactions.
- `type` (TransactionType, optional): The type of transactions to filter (DEPOSIT, WITHDRAWAL).
- `page` (int, optional): The page number to retrieve (default is 0).
- `size` (int, optional): The number of records per page (default is 20).