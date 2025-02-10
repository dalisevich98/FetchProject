# FetchProject
To run the code, execute the following commands:

```sh
mvn clean package
mvn exec:java -Dexec.mainClass="ReceiptProcessorServer"

Sample POST request:

curl -X POST "http://localhost:8080/receipts/process" \                                 
-H "Content-Type: application/json" \
-d '{
  "retailer": "Target",
  "purchaseDate": "2022-01-01",
  "purchaseTime": "13:01",
  "items": [
    { "shortDescription": "Mountain Dew 12PK", "price": "6.49" },
    { "shortDescription": "Emils Cheese Pizza", "price": "12.25" },
    { "shortDescription": "Knorr Creamy Chicken", "price": "1.26" },
    { "shortDescription": "Doritos Nacho Cheese", "price": "3.35" },
    { "shortDescription": "   Klarbrunn 12-PK 12 FL OZ  ", "price": "12.00" }
  ],
  "total": "35.35"
}'

Sample GET request:
curl -X GET "http://localhost:8080/receipts/9bdbc122-7a61-42ea-a78f-2c07c71981e4/points"
