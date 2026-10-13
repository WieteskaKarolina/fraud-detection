package com.example.exception;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

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

    @Test
    void validAdminTokenPassesJwtAndRoleChecksBeforeBinValidation() {
        given()
                .header("Authorization", "Bearer " + createToken("admin"))
                .when()
                .get("/api/bin/not-a-bin")
                .then()
                .statusCode(400);
    }

    @Test
    void validUserTokenPassesJwtAndRoleChecksBeforeRequestValidation() {
        given()
                .header("Authorization", "Bearer " + createToken("user"))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "bin": "585240",
                          "amount": 100.00,
                          "location": "New York",
                          "countryCode": "US"
                        }
                        """)
                .when()
                .post("/api/transactions/evaluate")
                .then()
                .statusCode(400);
    }

    private String createToken(String role) {
        return Jwt.issuer("https://localhost")
                .subject("security-test-user")
                .groups(Set.of(role))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .sign();
    }
}
