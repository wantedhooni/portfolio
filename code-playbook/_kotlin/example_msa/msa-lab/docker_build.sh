#bash

docker build -t discovery-server:1.0 ./discovery-server

docker build -t product-service:1.0 ./product-service

docker build -t order-service:1.0   ./order-service

docker build -t api-gateway:1.0      ./api-gateway