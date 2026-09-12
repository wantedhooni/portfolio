


```curl

curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1001,
    "productName": "Kafka CDC Book",
    "quantity": 2
  }' | jq

```