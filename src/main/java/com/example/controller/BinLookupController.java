package com.example.controller;

import com.example.facade.BinLookupFacade;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/api/bin")
@Produces("application/json")
@Consumes("application/json")
public class BinLookupController {

    @Inject
    BinLookupFacade binLookupFacade;

    @GET
    @Path("/{binNumber}")
    @RolesAllowed({"admin"})
    @Operation(summary = "Retrieve BIN details", description = "Fetches BIN details for the provided BIN number.")
    public Response getBinDetails(@PathParam("binNumber")
                                  @Pattern(regexp = "\\d{6,11}", message = "BIN must be 6 to 11 digits") String binNumber) {
        return binLookupFacade.getBinDetails(binNumber);
    }
}
