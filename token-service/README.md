# TOKEN SERVICE

A Kotlin + Quarkus + Apache Camel microservice that handles user authentication through Keycloak, retrieves user transactions from an external service, and publishes them to Kafka.

## OVERVIEW

This service performs the following sequence:

Receives an OAuth2 authorization code from the client.

Exchanges it for a JWT token via Keycloak.

Uses the token to fetch transactions for the user.

Publishes the transaction data to a Kafka topic (user-transactions).

## ARCHITECTURE

Browser/Client → Token Service → Keycloak → Transaction Service → Kafka (user-transactions)

## TECHNOLOGIES USED

Quarkus — high-performance Java/Kotlin runtime
Apache Camel — integration and routing framework
Keycloak — identity and access management
Kafka — message streaming platform
PostgreSQL — database for Keycloak
Docker Compose — container orchestration for local setup

## SETUP INSTRUCTIONS

Clone the repository
git clone https://github.com/Hellmick/inteview_kc.git
cd token-service

Start dependencies
docker-compose up -d

Verify containers are running:
docker ps

You should see services for:

keycloak

keycloak-postgres

zookeeper

kafka

Run the token service:
./gradlew :token-service:quarkusDev

Note: for the token service to work correctly the transaction service needs to be running

By default, it runs on http://localhost:8081

## REST API

GET /token

Description:
Accepts an OAuth2 authorization code, fetches a JWT token from Keycloak, retrieves the user’s transactions, and publishes them to Kafka.

Example request:
curl "http://localhost:8081/token?code=abc123"

## CHECKING MESSAGES IN KAFKA

To read messages from Kafka, run:

docker-compose exec kafka kafka-console-consumer
--bootstrap-server localhost:9092
--topic user-transactions
--from-beginning

Stop consuming with Ctrl + C.

## ENVIRONMENT VARIABLES

KC_TOKEN_SERVICE_URL — Keycloak token endpoint

TRANSACTION_SERVICE_URL — Transaction API endpoint

KAFKA_BROKER — Kafka broker address

QUARKUS_HTTP_PORT — HTTP port for the service