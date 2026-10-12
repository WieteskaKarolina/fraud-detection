package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinRequest;
import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BinLookupService {

    private static final Logger LOGGER = Logger.getLogger(BinLookupService.class);

    private static final String AUTHENTICATION_FAILED =
            "MASTERCARD_AUTHENTICATION_FAILED";
    private static final String PROVIDER_UNAVAILABLE =
            "MASTERCARD_UNAVAILABLE";
    private static final String REQUEST_REJECTED =
            "MASTERCARD_INTEGRATION_ERROR";
    private static final String PROVIDER_TIMEOUT =
            "MASTERCARD_TIMEOUT";

    private static final String AUTHENTICATION_FAILED_MESSAGE =
            "Authentication with the BIN provider failed";
    private static final String PROVIDER_UNAVAILABLE_MESSAGE =
            "BIN provider is temporarily unavailable";
    private static final String REQUEST_REJECTED_MESSAGE =
            "BIN provider rejected the lookup request";
    private static final String PROVIDER_TIMEOUT_MESSAGE =
            "BIN provider timed out";
    private static final String LOOKUP_FAILED_MESSAGE =
            "Could not retrieve BIN details from provider";

    private final BinLookupRestClient binLookupRestClient;

    @Inject
    public BinLookupService(@RestClient BinLookupRestClient binLookupRestClient) {
        this.binLookupRestClient = binLookupRestClient;
    }

    @CacheResult(cacheName = "binsCache")
    public Optional<BinResponse> lookupBin(String bin) {
        LOGGER.infov("BIN cache miss; calling Mastercard API for: {0}", bin);
        return callMastercardApi(bin);
    }

    private Optional<BinResponse> callMastercardApi(String bin) {
        try {
            List<BinResponse> responses =
                    binLookupRestClient.lookupBin(new BinRequest(bin));

            if (responses == null || responses.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(responses.get(0));
        } catch (WebApplicationException e) {
            return handleHttpError(e, bin);
        } catch (Exception e) {
            return handleUnexpectedError(e, bin);
        }
    }

    private Optional<BinResponse> handleHttpError(
            WebApplicationException exception,
            String bin
    ) {
        int status = getStatus(exception);
        LOGGER.errorf(exception, "Mastercard API returned HTTP status %d", status);

        if (status == 404) {
            return Optional.empty();
        }

        if (status == 401 || status == 403) {
            throw new ApiException(
                    Response.Status.BAD_GATEWAY,
                    AUTHENTICATION_FAILED,
                    AUTHENTICATION_FAILED_MESSAGE,
                    bin
            );
        }

        if (status == 429 || status >= 500) {
            throw new ApiException(
                    Response.Status.SERVICE_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE_MESSAGE,
                    bin
            );
        }

        throw new ApiException(
                Response.Status.BAD_GATEWAY,
                REQUEST_REJECTED,
                REQUEST_REJECTED_MESSAGE,
                bin
        );
    }

    private Optional<BinResponse> handleUnexpectedError(Exception exception, String bin) {
        LOGGER.error("Unexpected error calling Mastercard API", exception);

        if (hasCause(exception, SocketTimeoutException.class)) {
            throw new ApiException(
                    Response.Status.SERVICE_UNAVAILABLE,
                    PROVIDER_TIMEOUT,
                    PROVIDER_TIMEOUT_MESSAGE,
                    bin
            );
        }

        throw new ApiException(
                Response.Status.BAD_GATEWAY,
                REQUEST_REJECTED,
                LOOKUP_FAILED_MESSAGE,
                bin
        );
    }

    private int getStatus(WebApplicationException exception) {
        Response response = exception.getResponse();
        return response == null ? -1 : response.getStatus();
    }

    private static boolean hasCause(
            Throwable error,
            Class<? extends Throwable> type
    ) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return true;
            }
        }

        return false;
    }
}