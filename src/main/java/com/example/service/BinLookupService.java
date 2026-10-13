package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinRequest;
import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BinLookupService {

    private static final Logger LOGGER =
            Logger.getLogger(BinLookupService.class);

    private static final String AUTHENTICATION_FAILED =
            "MASTERCARD_AUTHENTICATION_FAILED";
    private static final String PERMISSION_DENIED =
            "MASTERCARD_PERMISSION_DENIED";
    private static final String PROVIDER_QUOTA_EXCEEDED =
            "MASTERCARD_QUOTA_EXCEEDED";
    private static final String PROVIDER_UNAVAILABLE =
            "MASTERCARD_UNAVAILABLE";
    private static final String INTEGRATION_ERROR =
            "MASTERCARD_INTEGRATION_ERROR";
    private static final String PROVIDER_TIMEOUT =
            "MASTERCARD_TIMEOUT";
    private static final String PROVIDER_QUOTA_REASON_CODE =
            "VOLUME_THRESHOLD_EXCEEDED";

    private static final String AUTHENTICATION_FAILED_MESSAGE =
            "Authentication with the BIN provider failed";
    private static final String PERMISSION_DENIED_MESSAGE =
            "The BIN provider credentials do not have permission for this operation";
    private static final String PROVIDER_QUOTA_EXCEEDED_MESSAGE =
            "The BIN provider call quota has been exceeded";
    private static final String PROVIDER_UNAVAILABLE_MESSAGE =
            "BIN provider is temporarily unavailable";
    private static final String INTEGRATION_ERROR_MESSAGE =
            "Could not retrieve BIN details from provider";
    private static final String PROVIDER_TIMEOUT_MESSAGE =
            "BIN provider timed out";

    private final BinLookupRestClient binLookupRestClient;

    @Inject
    public BinLookupService(
            @RestClient BinLookupRestClient binLookupRestClient) {
        this.binLookupRestClient = binLookupRestClient;
    }

    @CacheResult(cacheName = "binsCache")
    public Optional<BinResponse> lookupBin(String bin) {
        return callMastercardApi(bin);
    }

    private Optional<BinResponse> callMastercardApi(String bin) {
        try {
            List<BinResponse> responses =
                    binLookupRestClient.lookupBin(new BinRequest(bin));

            if (responses == null || responses.isEmpty()) {
                return Optional.empty();
            }

            BinResponse response = responses.get(0);
            if (response == null) {
                throw integrationError(bin);
            }
            return Optional.of(response);

        } catch (WebApplicationException e) {
            throw handleHttpError(e, bin);

        } catch (ProcessingException e) {
            throw handleClientError(e, bin);
        }
    }

    private ApiException handleHttpError(
            WebApplicationException exception,
            String bin) {

        int status = getStatus(exception);
        String reasonCode = readProviderReasonCode(exception);
        ProviderFailure failure = mapProviderFailure(status, reasonCode);

        if (failure.warning()) {
            LOGGER.warnf(
                    "Mastercard request failed; HTTP status %d, ReasonCode: %s",
                    status,
                    reasonCode);
        } else {
            LOGGER.errorf(
                    "Mastercard request failed; HTTP status %d, ReasonCode: %s",
                    status,
                    reasonCode);
        }

        return new ApiException(
                failure.apiStatus(),
                failure.code(),
                failure.message(),
                bin);
    }

    private ProviderFailure mapProviderFailure(int status, String reasonCode) {
        return switch (status) {
            case 401 -> new ProviderFailure(
                    Response.Status.BAD_GATEWAY,
                    AUTHENTICATION_FAILED,
                    AUTHENTICATION_FAILED_MESSAGE,
                    false);
            case 403 -> PROVIDER_QUOTA_REASON_CODE.equals(reasonCode)
                    ? new ProviderFailure(
                            Response.Status.SERVICE_UNAVAILABLE,
                            PROVIDER_QUOTA_EXCEEDED,
                            PROVIDER_QUOTA_EXCEEDED_MESSAGE,
                            true)
                    : new ProviderFailure(
                            Response.Status.BAD_GATEWAY,
                            PERMISSION_DENIED,
                            PERMISSION_DENIED_MESSAGE,
                            false);
            case 429, 500 -> new ProviderFailure(
                    Response.Status.SERVICE_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE_MESSAGE,
                    true);
            default -> new ProviderFailure(
                    Response.Status.BAD_GATEWAY,
                    INTEGRATION_ERROR,
                    INTEGRATION_ERROR_MESSAGE,
                    false);
        };
    }

    private ApiException handleClientError(
            ProcessingException exception,
            String bin) {

        if (hasCause(exception, SocketTimeoutException.class)) {
            LOGGER.warn("Timeout while calling Mastercard API");

            return new ApiException(
                    Response.Status.GATEWAY_TIMEOUT,
                    PROVIDER_TIMEOUT,
                    PROVIDER_TIMEOUT_MESSAGE,
                    bin);
        }

        if (hasCause(exception, ConnectException.class)
                || hasCause(exception, UnknownHostException.class)) {

            LOGGER.warn("Could not connect to Mastercard API");

            return new ApiException(
                    Response.Status.SERVICE_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE,
                    PROVIDER_UNAVAILABLE_MESSAGE,
                    bin);
        }

        LOGGER.error(
                "Error while communicating with Mastercard API",
                exception);

        return new ApiException(
                Response.Status.BAD_GATEWAY,
                INTEGRATION_ERROR,
                INTEGRATION_ERROR_MESSAGE,
                bin);
    }

    private int getStatus(WebApplicationException exception) {
        Response response = exception.getResponse();
        return response == null ? -1 : response.getStatus();
    }

    private String readProviderReasonCode(WebApplicationException exception) {
        Response response = exception.getResponse();
        if (response == null) {
            return "unknown";
        }

        try {
            if (!response.hasEntity()) {
                return "unknown";
            }

            JSONObject errorsObject = new JSONObject(response.readEntity(String.class))
                    .optJSONObject("Errors");
            JSONArray errors = errorsObject == null
                    ? null
                    : errorsObject.optJSONArray("Error");
            JSONObject firstError = errors == null ? null : errors.optJSONObject(0);
            if (firstError == null) {
                return "unknown";
            }

            return firstError.optString("ReasonCode", "unknown");
        } catch (RuntimeException ignored) {
            // Gateway errors may be HTML or otherwise not match the documented JSON structure.
            return "unknown";
        } finally {
            response.close();
        }
    }

    private ApiException integrationError(String bin) {
        return new ApiException(
                Response.Status.BAD_GATEWAY,
                INTEGRATION_ERROR,
                INTEGRATION_ERROR_MESSAGE,
                bin);
    }

    private record ProviderFailure(
            Response.Status apiStatus,
            String code,
            String message,
            boolean warning) {
    }

    private static boolean hasCause(
            Throwable error,
            Class<? extends Throwable> type) {

        for (Throwable cause = error;
             cause != null;
             cause = cause.getCause()) {

            if (type.isInstance(cause)) {
                return true;
            }
        }

        return false;
    }
}