package com.example.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Disabled
@QuarkusTest
public class BinLookupControllerIT {

    private final String VALID_TOKEN = "Bearer <valid-jwt-token>";

    @Test
    void testGetBinDetails_Success() {
        given()
                .header("Authorization", VALID_TOKEN)
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(200)
                .body("consumerType", notNullValue());
    }

    @Test
    void testGetBinDetails_Unauthorized() {
        given()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetBinDetails_NotFound() {
        given()
                .header("Authorization", VALID_TOKEN)
                .when()
                .get("/api/bin/999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("BIN details not found"));
    }
}

