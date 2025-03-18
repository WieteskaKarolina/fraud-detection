package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.facade.TransactionFacade;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/api/transactions")
@Produces("application/json")
@Consumes("application/json")
public class TransactionRiskController {

    @Inject
    TransactionFacade transactionFacade;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/evaluate")
    @Operation(summary = "Evaluate transaction risk", description = "Evaluates transaction risk based on given details")
    @RolesAllowed({"admin", "user"})
    public Response evaluateTransactionRisk(@Valid @RequestBody TransactionRequest transactionRequest,
                                            @HeaderParam("X-Request-Id") String requestId,
                                            @Context SecurityContext securityContext) {
        String username = jwt.getName();
        return transactionFacade.evaluateTransaction(transactionRequest, requestId, username);
    }
}
