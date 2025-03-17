package com.example.controller;

import com.example.model.BinResponse;
import com.example.service.BinLookupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;


import java.util.Optional;

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
    public Response getBinDetails(@PathParam("binNumber") String binNumber) {
        try {
            LOGGER.info("Fetching BIN details for: " + binNumber);

            if (!isValidBin(binNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid BIN format: Must be 6 to 11 digits long.")
                        .build();
            }

            Optional<BinResponse> binResponseOpt = binLookupService.lookupBin(binNumber);

            if (binResponseOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("BIN details not found").build();
            }

            return Response.ok(binResponseOpt.get()).build();

        } catch (Exception e) {
            LOGGER.error("Error fetching BIN details", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching BIN details").build();
        }
    }

    /**
     * Check, if BIN is valid.
     */
    private boolean isValidBin(String bin) {
        return bin != null && bin.matches("\\d{6,11}");
    }
}
