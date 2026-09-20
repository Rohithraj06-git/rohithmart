# RohithMart REST API Contract Specification

All JSON API endpoints are versioned under `/api/v1/...` (with AI proxy at `/api/chat`).

## Standard Response Format

### Success Envelope (HTTP 200 / 201)
```json
{
  "success": true,
  "data": {
    "key": "value"
  },
  "error": null
}
```

### Error Envelope (HTTP 400 / 401 / 403 / 404 / 409 / 500)
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable explanation of error."
  }
}
```

## Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/api/v1/health` | Service & DB Health Probe | No |
| POST | `/api/chat` | AI Chatbot Domain Query | No (Session rate-limited) |
| GET | `/api/v1/products` | Paginated product search & filter | No |
| GET | `/api/v1/products/{id}` | Product details & reviews | No |
| GET | `/api/v1/cart` | Retrieve user cart items | Yes (Buyer) |
| POST | `/api/v1/cart/items` | Add item to cart | Yes (Buyer) |
| PUT | `/api/v1/cart/items/{id}`| Update cart item quantity | Yes (Buyer) |
| DELETE | `/api/v1/cart/items/{id}`| Remove cart item | Yes (Buyer) |
| POST | `/api/v1/checkout` | Process order checkout | Yes (Buyer) |
| GET | `/api/v1/orders` | User order history | Yes (Buyer) |
