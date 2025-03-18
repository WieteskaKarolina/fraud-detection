package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinRequest;
import com.example.model.BinResponse;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BinLookupService {

    private static final Logger LOGGER = Logger.getLogger(BinLookupService.class);

    private final BinLookupRestClient binLookupRestClient;

    @Inject
    public BinLookupService(@RestClient BinLookupRestClient binLookupRestClient) {
        this.binLookupRestClient = binLookupRestClient;
    }

    @CacheResult(cacheName = "binsCache")
    public Optional<BinResponse> lookupBin(String bin) {
        LOGGER.info("BIN not found in cache, calling Mastercard API for: " + bin);
        return callMastercardApi(bin);
    }

    private Optional<BinResponse> callMastercardApi(String bin) {
        try {
            List<BinResponse> responseList = binLookupRestClient.lookupBin(new BinRequest(bin));
            return responseList.isEmpty() ? Optional.empty() : Optional.of(responseList.get(0));

        } catch (jakarta.ws.rs.WebApplicationException e) {
            LOGGER.error("Mastercard API error: " + e.getResponse().getStatus(), e);
            return Optional.empty();

        } catch (Exception e) {
            LOGGER.error("Unexpected error calling Mastercard API", e);
            return Optional.empty();
        }
    }

}
