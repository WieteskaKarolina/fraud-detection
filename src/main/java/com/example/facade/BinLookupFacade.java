package com.example.facade;

import com.example.dto.BinResponse;
import com.example.service.BinLookupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class BinLookupFacade {

    private static final Logger LOGGER = Logger.getLogger(BinLookupFacade.class);

    @Inject
    BinLookupService binLookupService;

    public Response getBinDetails(String binNumber) {
        LOGGER.info("Fetching BIN details for: " + binNumber);

        try {
            BinResponse binResponse = binLookupService.lookupBin(binNumber)
                    .orElseThrow(() -> {
                        LOGGER.warn("BIN details not found for: " + binNumber);
                        return new WebApplicationException(
                                Response.status(Response.Status.NOT_FOUND)
                                        .entity(Map.of("error", "BIN details not found", "bin", binNumber))
                                        .build()
                        );
                    });

            return Response.ok(binResponse).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while fetching BIN details for: " + binNumber, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error fetching BIN details", "message", e.getMessage()))
                    .build();
        }
    }
}
