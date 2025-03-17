package com.example.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Disabled
@QuarkusTest
public class TransactionRiskControllerIT {

    private final String VALID_TOKEN = "Bearer <valid-jwt-token>";

    @Test
    void testEvaluateTransactionRisk_Success() {
        given()
                .header("Authorization", VALID_TOKEN)
                .contentType(ContentType.JSON)
                .body("{\"bin\": \"585240\", \"amount\": 100.0, \"location\": \"New York\"}")
                .when()
                .post("/api/transactions/evaluate")
                .then()
                .statusCode(200)
                .body("riskScore", notNullValue());
    }

    @Test
    void testEvaluateTransactionRisk_Unauthorized() {
        given()
                .header("Authorization", "Bearer invalid-token")
                .header("X-Request-Id", "req-123")
                .contentType(ContentType.JSON)
                .body("{\"bin\": \"123456\", \"amount\": 100.0, \"location\": \"New York\"}")
                .when()
                .post("/api/transactions/evaluate")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetBinDetails_Success() {
        given()
                .header("Authorization", VALID_TOKEN)
                .when()
                .get("/api/transactions/bin/585240")
                .then()
                .statusCode(200)
                .body("consumerType", notNullValue());
    }
}
