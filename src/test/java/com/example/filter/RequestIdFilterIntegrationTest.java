package com.example.filter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusTest
class RequestIdFilterIntegrationTest {

    @Test
    void unauthorizedResponse_shouldIncludeGeneratedRequestIdAndClientCorrelationId() {
        given()
                .header("X-Correlation-Id", "client-order-123")
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(401)
                .header("X-Request-Id", matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
                ))
                .header("X-Correlation-Id", equalTo("client-order-123"));
    }

    @Test
    void invalidCorrelationIdResponse_shouldIncludeGeneratedRequestId() {
        given()
                .header("X-Correlation-Id", "client id with spaces")
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(400)
                .header("X-Request-Id", matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
                ));
    }

}
