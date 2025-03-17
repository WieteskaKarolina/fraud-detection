# Quarkus Fraud Detection Service
Fraud Detection API integrated with Mastercard bin-lookup API

## Prerequisites
- Docker installed on your machine
- Java 17+ installed (for local builds)
- Maven installed (for building the project)

## Configuration
To properly configure the application, ensure the following environment variables and properties are set:

### **Mastercard API Configuration**
```properties
mastercard.api.url=https://sandbox.api.mastercard.com/bin-resources/bin-ranges/account-searches
mastercard.consumer.key=<consumer-key>
mastercard.private.key.path=<private-key-path>
mastercard.private.key.alias=<private-key-alias>
mastercard.private.key.password=<private-key-password>
```

### **Database Configuration**
```properties
quarkus.datasource.username=<your-db-username>
quarkus.datasource.password=<your-db-password>
quarkus.datasource.jdbc.url=<your-db-url>
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=true
```

### **Logging Configuration**
```properties
quarkus.log.category."io.quarkus.cache".level=DEBUG
quarkus.log.category."com.example".level=DEBUG
quarkus.cache.caffeine.binCache.expire-after-write=10m
```

### **JWT Authentication Configuration**
```properties
mp.jwt.verify.issuer=<your-issuer>
smallrye.jwt.sign.key.location=src/main/resources/META-INF/resources/privateKey.pem
mp.jwt.verify.publickey.location=src/main/resources/META-INF/resources/publicKey.pem
smallrye.jwt.sign.algorithm=RS256
```

## Generate RSA Keys for JWT Authentication
To generate the necessary RSA keys for JWT authentication, run the following commands:

```sh
openssl genpkey -algorithm RSA -out src/main/resources/META-INF/resources/privateKey.pem
openssl rsa -pubout -in src/main/resources/META-INF/resources/privateKey.pem -out src/main/resources/META-INF/resources/publicKey.pem
```

## API Endpoints
The application exposes the following REST endpoints:

### **Authentication API** (`/api/auth`)
- **`POST /api/auth/generate`** - Generates a JWT token for a given username and role.
  - **Query Parameters:**
    - `username` (required) - The username for which the JWT will be generated.
    - `role` (optional, default: `user`) - The role of the user (`user` or `admin`).
  - **Response:**
    - JSON containing the generated JWT token.

### **Transaction Risk API** (`/api/transactions`)
- **`POST /api/transactions/evaluate`** - Evaluates the fraud risk for a given transaction.
  - **Request Body:**
    - `bin` - The BIN number of the card.
    - `amount` - The transaction amount.
    - `location` - The location of the transaction.
  - **Headers:**
    - `X-Request-Id` (optional) - Custom trace ID.
  - **Response:**
    - JSON containing `riskScore` and `explanation`.
  - **Authorization:**
    - Requires JWT token with role `user` or `admin`.

- **`GET /api/transactions/bin/{binNumber}`** - Retrieves BIN details for a given card BIN.
  - **Path Parameters:**
    - `binNumber` - A numeric BIN (6 to 11 digits).
  - **Response:**
    - JSON containing BIN details.
  - **Authorization:**
    - Requires JWT token with role `admin`.

## Build the Project
Before running the Docker container, build the Quarkus project and run all tests using:

```sh
mvn clean package
```

## Build the Docker Image
Use the following command to build the Docker image:

```sh
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-fraud-detection .
```

## Run the Docker Container
To run the service inside a Docker container, use:

```sh
docker run -p 8080:8080 \
  -e "JAVA_OPTS=-Dmastercard.private.key.path=/deployments/my-project-key-sandbox.p12 \
  -Dsmallrye.jwt.sign.key.location=/deployments/privateKey.pem \
  -Dmp.jwt.verify.publickey.location=/deployments/publicKey.pem" \
  quarkus-fraud-detection
```

## Stopping the Container
To stop the running container, use:

```sh
docker ps # Find the container ID

docker stop <container_id>
```

## Cleaning Up
To remove the container and image:

```sh
docker rm <container_id>
docker rmi quarkus-fraud-detection
```

---

Now your Quarkus Fraud Detection Service is up and running inside Docker! 🚀




