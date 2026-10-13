package com.example.facade;

import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import com.example.service.BinLookupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BinLookupFacade {

    private static final Logger LOGGER = Logger.getLogger(BinLookupFacade.class);

    private final BinLookupService binLookupService;

    @Inject
    public BinLookupFacade(BinLookupService binLookupService) {
        this.binLookupService = binLookupService;
    }

    public Response getBinDetails(String binNumber, String requestId, String correlationId) {
        LOGGER.infof("Fetching BIN details for %s", binNumber);

        BinResponse binResponse = binLookupService.lookupBin(binNumber)
                .orElseThrow(() -> {
                    LOGGER.warnf("BIN details not found for %s", binNumber);
                    return new ApiException(Response.Status.NOT_FOUND, "BIN_NOT_FOUND",
                            "BIN details not found", binNumber);
                });
        return Response.ok(binResponse).build();
    }
}
