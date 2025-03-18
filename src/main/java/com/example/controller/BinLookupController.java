package com.example.controller;

import com.example.facade.BinLookupFacade;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import com.example.dto.BinResponse;

@ApplicationScoped
@Path("/api/bin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "BearerAuth")
public class BinLookupController {

    @Inject
    BinLookupFacade binLookupFacade;

    @GET
    @Path("/{binNumber}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Retrieve BIN details",
            description = "Fetches BIN details for the provided BIN number. Requires 'admin' role.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successful response",
                    content = @Content(schema = @Schema(implementation = BinResponse.class))),
            @APIResponse(responseCode = "400", description = "Invalid BIN format"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getBinDetails(@PathParam("binNumber")
                                  @Pattern(regexp = "\\d{6,11}", message = "BIN must be 6 to 11 digits") String binNumber) {
        return binLookupFacade.getBinDetails(binNumber);
    }
}
