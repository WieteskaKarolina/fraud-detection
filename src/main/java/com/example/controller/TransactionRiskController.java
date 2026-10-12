package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.facade.TransactionFacade;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@ApplicationScoped
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityScheme(
        securitySchemeName = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityRequirement(name = "BearerAuth")
public class TransactionRiskController {

    private final TransactionFacade transactionFacade;
    private final JsonWebToken jwt;

    @Inject
    public TransactionRiskController(TransactionFacade transactionFacade, JsonWebToken jwt) {
        this.transactionFacade = transactionFacade;
        this.jwt = jwt;
    }

    @POST
    @Path("/evaluate")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Evaluate transaction risk",
            description = "Evaluates the risk associated with a transaction based on provided details. Requires authentication."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Risk evaluation successful"),
            @APIResponse(responseCode = "400", description = "Invalid request format"),
            @APIResponse(responseCode = "401", description = "Unauthorized request"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "404", description = "BIN not found"),
            @APIResponse(responseCode = "502", description = "BIN provider integration error"),
            @APIResponse(responseCode = "503", description = "BIN provider unavailable"),
            @APIResponse(responseCode = "504", description = "BIN provider timeout"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RolesAllowed({"admin", "user"})
    public Response evaluateTransactionRisk(
            @Valid
            @RequestBody(
                    description = "Transaction request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionRequest.class))
            ) TransactionRequest transactionRequest,

            @HeaderParam("X-Request-Id")
            @Schema(description = "Unique request identifier for tracing", example = "123e4567-e89b-12d3-a456-426614174000")
                    String requestId,

            @Context SecurityContext securityContext
    ) {
        String username = jwt.getName();
        return transactionFacade.evaluateTransaction(transactionRequest, requestId, username);
    }
}
