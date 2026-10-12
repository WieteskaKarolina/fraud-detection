package com.example.exception;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class SecurityErrorResponseTest {

    @Test
    void missingTokenReturnsCommonUnauthorizedResponseAndBearerChallenge() {
        given()
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .header("WWW-Authenticate", startsWith("Bearer"))
                .body("code", equalTo("UNAUTHORIZED"))
                .body("message", equalTo("Authentication is required"));
    }

    @Test
    void invalidTokenReturnsCommonUnauthorizedResponseAndBearerChallenge() {
        given()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .header("WWW-Authenticate", startsWith("Bearer"))
                .body("code", equalTo("UNAUTHORIZED"))
                .body("message", equalTo("Authentication is required"));
    }

    @Test
    void insufficientRoleReturnsCommonForbiddenResponse() {
        String userToken = createToken("user");

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/bin/585240")
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("code", equalTo("FORBIDDEN"))
                .body("message", equalTo("You are not allowed to access this resource"));
    }

    private String createToken(String role) {
        return given()
                .queryParam("username", "security-test-user")
                .queryParam("role", role)
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}
