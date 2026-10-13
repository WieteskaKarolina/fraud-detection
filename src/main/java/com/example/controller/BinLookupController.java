package com.example.controller;

import com.example.dto.BinResponse;
import com.example.facade.BinLookupFacade;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@ApplicationScoped
@Path("/api/bin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "BearerAuth")
public class BinLookupController {

    private final BinLookupFacade binLookupFacade;

    @Inject
    public BinLookupController(BinLookupFacade binLookupFacade) {
        this.binLookupFacade = binLookupFacade;
    }

    @GET
    @Path("/{binNumber}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Retrieve BIN details",
            description = "Fetches BIN details, generates an X-Request-Id, and accepts an optional X-Correlation-Id. Requires 'admin' role.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful response",
                    content = @Content(schema = @Schema(implementation = BinResponse.class))),
            @APIResponse(responseCode = "400", description = "Invalid BIN format"),
            @APIResponse(responseCode = "404", description = "BIN not found"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "502", description = "BIN provider integration error"),
            @APIResponse(responseCode = "503", description = "BIN provider unavailable"),
            @APIResponse(responseCode = "504", description = "BIN provider timeout"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getBinDetails(
            @PathParam("binNumber")
            @Pattern(regexp = "\\d{6,11}", message = "BIN must be 6 to 11 digits") String binNumber,
            @HeaderParam("X-Request-Id") @Parameter(hidden = true) String requestId,
            @HeaderParam("X-Correlation-Id")
            @Parameter(description = "Optional client identifier for correlating this request with client logs")
                    String correlationId
    ) {
        return binLookupFacade.getBinDetails(binNumber, requestId, correlationId);
    }
}
