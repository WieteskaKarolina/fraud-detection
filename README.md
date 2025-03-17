# Quarkus Fraud Detection Service
Fraud Detection API integrated with Mastercard bin-lookup API

## Prerequisites
- Docker installed on your machine
- Java 17+ installed (for local builds)
- Maven installed (for building the project)

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


