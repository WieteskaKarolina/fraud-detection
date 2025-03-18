package com.example.security;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class JWTControllerTest {

    @Test
    void testGenerateToken_Success_UserRole() {
        given()
                .queryParam("username", "testuser")
                .queryParam("role", "user")
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("token", notNullValue());
    }

    @Test
    void testGenerateToken_Success_AdminRole() {
        given()
                .queryParam("username", "adminuser")
                .queryParam("role", "admin")
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("token", notNullValue());
    }

    @Test
    void testGenerateToken_MissingUsername_ShouldFail() {
        given()
                .queryParam("username", "")
                .queryParam("role", "user")
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(ContentType.JSON)
                .body("parameterViolations[0].message", equalTo("Username is required"))
                .body("parameterViolations[0].value", equalTo(""));
    }

    @Test
    void testGenerateToken_InvalidRole_ShouldFail() {
        given()
                .queryParam("username", "testuser")
                .queryParam("role", "hacker")
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(ContentType.JSON)
                .body("parameterViolations[0].message", containsString("Invalid role. Allowed roles: user, admin"))
                .body("parameterViolations[0].value", equalTo("hacker"));
    }

    @Test
    void testGenerateToken_NoRole_ShouldUseDefaultUserRole() {
        given()
                .queryParam("username", "testuser")
                .when()
                .post("/api/auth/generate")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("token", notNullValue());
    }
}
