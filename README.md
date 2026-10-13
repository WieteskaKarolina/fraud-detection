# Quarkus Fraud Detection Service
Fraud Detection API integrated with Mastercard bin-lookup API

## Prerequisites
- Docker installed on your machine
- Java 17+ installed (for local builds)
- Maven is not required; use the included Maven Wrapper (`mvnw` / `mvnw.cmd`)

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

## 🚀 OpenAPI Documentation
This project uses **OpenAPI 3.0** for API documentation.

### 👉 How to View the API Docs?
You can load the OpenAPI spec (`openapi.yaml`) in an online editor:

### 1️⃣ **Option 1: Swagger Editor (Online)**
- Open [Swagger Editor](https://editor.swagger.io/)
- Click **File → Import URL**
- Enter the raw GitHub link to `openapi.yaml`, e.g.:
  ```
  https://raw.githubusercontent.com/user/repo/main/openapi.yaml
  ```
- Now you can **view and test the API!**

### 2️⃣ **Option 2: Run Swagger UI Locally**
- Start the Quarkus server:
  ```sh
  # Windows PowerShell
  .\mvnw.cmd quarkus:dev

  # Linux/macOS
  ./mvnw quarkus:dev
  ```
- Open Swagger UI in your browser:
  ```sh
  http://localhost:8080/swagger-ui
  ```

### 🔒 **Authentication**
Some endpoints require authentication (`BearerAuth`).
- Use a valid JWT in Swagger UI (`Authorize` button). The `/api/auth/generate` helper is available only in a test-profile build and is not included in the normal Docker image.


## Build the Project
Before building the Docker image, package the Quarkus application (this also runs the tests):

```powershell
.\mvnw.cmd clean package
```

## Build the Docker Image
Use the following command to build the Docker image:

```sh
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-fraud-detection .
```

## Run the Docker Container
To run the service directly, use:

```powershell
docker run --rm -p 8080:8080 `
  -e "JAVA_OPTS_APPEND=-Dmastercard.private.key.path=/deployments/my-project-key-sandbox.p12 -Dsmallrye.jwt.sign.key.location=/deployments/privateKey.pem -Dmp.jwt.verify.publickey.location=/deployments/publicKey.pem" `
  quarkus-fraud-detection
```

The Dockerfile currently copies the configured key files into the image. Keep this image local; do not publish it to a registry.

## Run with Docker Compose
After packaging the application, start it with:

```powershell
docker compose up --build
```

Stop it with:

```powershell
docker compose down
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




