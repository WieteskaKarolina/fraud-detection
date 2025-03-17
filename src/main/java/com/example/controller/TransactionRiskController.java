package com.example.controller;

import com.example.model.BinResponse;
import com.example.service.BinLookupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;



@ApplicationScoped
@Path("/api/transactions")
public class TransactionRiskController {

    private static final Logger LOGGER = Logger.getLogger(TransactionRiskController.class);

    @Inject
    BinLookupService binLookupService;

    @GET
    @Path("/bin/{binNumber}")
    @Produces("application/json")
    @Operation(summary = "Retrieve BIN details", description = "Fetches BIN details for the provided BIN number.")
    public Response getBinDetails(@PathParam("binNumber")
                                  @Pattern(regexp = "\\d{6,11}", message = "BIN must be 6 to 11 digits") String binNumber) {
        LOGGER.info("Fetching BIN details for: " + binNumber);

        try {
            BinResponse binResponse = binLookupService.lookupBin(binNumber)
                    .orElseThrow(() -> new WebApplicationException("BIN details not found", Response.Status.NOT_FOUND));

            return Response.ok(binResponse).build();
        } catch (Exception e) {
            LOGGER.error("Error fetching BIN details: {}", e.getMessage(), e);
            throw new WebApplicationException("Error fetching BIN details", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
