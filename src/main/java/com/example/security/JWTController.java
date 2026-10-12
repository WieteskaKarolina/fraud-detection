package com.example.security;

import com.example.exception.ApiException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class JWTController {

    private final String issuer;

    @Inject
    public JWTController(@ConfigProperty(name = "mp.jwt.verify.issuer") String issuer) {
        this.issuer = issuer;
    }

    @POST
    @Path("/generate")
    @Operation(summary = "Generate JWT token",
            description = "Generates a JWT token for authentication based on a given username and role.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "JWT generated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid input parameters"),
            @APIResponse(responseCode = "500", description = "Internal server error while generating JWT")
    })
    public Response generateToken(
            @QueryParam("username")
            @NotBlank(message = "Username is required")
            @Parameter(description = "Username for whom the JWT token is generated", required = true,
                    example = "john_doe") String username,
            @QueryParam("role")
            @DefaultValue("user")
            @Pattern(regexp = "user|admin", message = "Invalid role. Allowed roles: user, admin")
            @Parameter(description = "User role (either 'user' or 'admin')", required = false,
                    example = "admin") String role
    ) {
        if (username == null || username.trim().isEmpty()) {
            throw new ApiException(Response.Status.BAD_REQUEST, "INVALID_REQUEST", "Username is required", null);
        }

        String token = Jwt.issuer(issuer)
                .subject(username.trim())
                .groups(Set.of(role))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofHours(2)))
                .sign();
        return Response.ok(Map.of("token", token)).build();
    }
}
