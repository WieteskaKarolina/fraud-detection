package com.example.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class JWTController {

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @POST
    @Path("/generate")
    @Operation(
            summary = "Generate JWT token",
            description = "Generates a JWT token for authentication based on a given username and role."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "JWT generated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid input parameters"),
            @APIResponse(responseCode = "500", description = "Internal server error while generating JWT")
    })
    public Response generateToken(
            @QueryParam("username")
            @NotBlank(message = "Username is required")
            @Parameter(
                    description = "Username for whom the JWT token is generated",
                    required = true,
                    example = "john_doe"
            )
                    String username,

            @QueryParam("role")
            @DefaultValue("user")
            @Pattern(regexp = "user|admin", message = "Invalid role. Allowed roles: user, admin")
            @Parameter(
                    description = "User role (either 'user' or 'admin')",
                    required = false,
                    example = "admin"
            )
                    String role
    ) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Username is required and cannot be blank."))
                        .build();
            }

            Set<String> roles = Set.of(role);

            String token = Jwt.issuer(issuer)
                    .subject(username.trim())
                    .groups(roles)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(Duration.ofHours(2)))
                    .sign();

            return Response.ok(Map.of("token", token)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "An error occurred while generating the token."))
                    .build();
        }
    }
}
